package io.worker.consumers;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static io.worker.consumers.ConsumerMetadata.addConsumerLag;


public sealed abstract class AbstractKafkaConsumer<T> permits JobConsumer {

    private static final Logger log = Logger.getLogger(AbstractKafkaConsumer.class);
    @ConfigProperty(name = "kafka.broker")
    String broker;
    private String topicName;
    private String groupName;
    private List<Integer> partitions;
    private KafkaConsumer<String, String> consumer;
    private ExecutorService executorService;

    protected void initConsumer(String group, String topic, List<Integer> partitions) {
        groupName = group;
        topicName = topic;
        this.partitions = partitions;
        var virtualThreadFactory = Thread.ofVirtual().factory();
        this.executorService = new ThreadPoolExecutor(
                50,
                100,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(100),
                virtualThreadFactory
        );
        prepareConsumer(group, topic, partitions);
        startKafkaMessageConsumer();

    }

    private void prepareConsumer(String group, String topic, List<Integer> partitions) {
        var properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, group);
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "3000");
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "40");
        properties.setProperty(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "1000");

        consumer = new KafkaConsumer<>(properties);
        consumer.assign(partitions.stream().map(partition -> new TopicPartition(topic, partition)).toList());
    }

    private void startKafkaMessageConsumer() {
        // [todo] we need to enable multi threaded consumer based on config
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(this::listenTopic);
    }

    private void listenTopic() {
        log.infof("kafka consumer start for topic: %s", consumer.listTopics());
        try {
            while (true) {
                var records = consumer.poll(Duration.ofMillis(1000));
                var offsetPerPartitionMap = new HashMap<Integer, Long>();
                if (records.count() > 0) {
                    for (var record : records) {
                        executorService.execute(() -> {
                            handle(record);
                        });
                        offsetPerPartitionMap.put(record.partition(), record.offset());
                    }

                    // Commit the offset for processed message
                    offsetPerPartitionMap.entrySet().stream().forEach(ele -> {
                        TopicPartition topicPartition = new TopicPartition(this.topicName, ele.getKey());
                        log.debugf("commit offset for group= %s, topic= %s, partition= %s, offset= %s",
                                groupName, topicName, ele.getKey(), ele.getValue());
                        consumer.seekToEnd(Collections.singletonList(topicPartition));
                        long endOffset = consumer.position(topicPartition);
                        addConsumerLag(topicName + "-" + ele.getKey(), endOffset - ele.getValue());
                    });
                }
            }
        } catch (Exception e) {
            log.error(STR."error in kafka base consumer for \{getThis().getClass().getSimpleName()}", e);
            log.debugf("reinit the consumer for group= %s, topic= %s ", groupName, topicName);
            initConsumer(groupName, topicName, this.partitions);
        }
    }

    public void handle(ConsumerRecord<String, String> record) {
        try {
            consume(record.value());
        } catch (Exception e) {
            log.error(STR."error in kafka base consumer for \{getThis().getClass().getSimpleName()}", e);
        }
    }

    abstract void consume(String body) throws Exception;

    abstract T getThis();

    void onStart(@Observes StartupEvent ev) {
        log.infof("Kafka consumer start: %s", getThis().getClass().getSimpleName());
    }

    @PreDestroy
    public void cleanUp() {
        consumer.close();
    }
}
