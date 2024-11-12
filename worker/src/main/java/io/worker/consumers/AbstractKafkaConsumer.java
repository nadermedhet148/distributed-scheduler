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
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    @Inject
    MeterRegistry registry;

    // The number of milliseconds between the message producing and message consumption.
    // Each consumer needs to report the lag using reportLastConsumedMessageTimestamp() method.
    private AtomicLong consumerLagInMillis;

    protected void initConsumer(String group, String topic, List<Integer> partitions) {
        groupName = group;
        topicName = topic;
        this.partitions = partitions;
        prepareConsumer(group, topic, partitions);
        startKafkaMessageConsumer();

        consumerLagInMillis = registry.gauge("kafka_consumer_lag_millis",
                List.of(Tag.of("topic_name", topicName), Tag.of("group_name", groupName)),
                new AtomicLong(0));
    }

    private void prepareConsumer(String group, String topic, List<Integer> partitions) {
        var properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, group);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "30");
        properties.setProperty(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "10000");

        consumer = new KafkaConsumer<>(properties);
        consumer.assign(partitions.stream().map(partition -> new TopicPartition(topic, partition)).toList());
        KafkaClientMetrics consumerKafkaMetrics = new KafkaClientMetrics(consumer);
        consumerKafkaMetrics.bindTo(registry);
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
                if (records.count() > 0) {
                    for (var record : records) {
                        log.debugf(
                                "group= %s, topic= %s, partition= %s,  offset = %s , records = %s",
                                groupName,
                                topicName,
                                record.partition(),
                                record.offset(),
                                records.count()
                        );
                        handle(record);

                        // Commit the offset for processed message
                        TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                        OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(record.offset());
                        consumer.commitSync(Collections.singletonMap(topicPartition, offsetAndMetadata));

                        consumer.seekToEnd(Collections.singletonList(topicPartition));
                        long endOffset = consumer.position(topicPartition);
                        addConsumerLag(record.topic() + "-" +record.partition(), endOffset - record.offset());
                    }
                }
            }
        } catch (Exception e) {
            log.error(STR."error in kafka base consumer for \{getThis().getClass().getSimpleName()}", e);
            log.debugf("reinit the consumer for group= %s, topic= %s ", groupName, topicName);
            initConsumer(groupName, topicName, this.partitions);
        }
    }

    public void handle(ConsumerRecord<String, String> record) throws Exception {
        long msgProcessingStartTime = System.nanoTime();
        boolean success = false;
        try {


            consume(record.value());

            success = true;
        } catch (Exception e) {
            log.error(STR."error in kafka base consumer for \{getThis().getClass().getSimpleName()}", e);
            // throw e;
        } finally {
            long durationNanos = System.nanoTime() - msgProcessingStartTime;
            registry.timer("kafka_consumer_msg_processing_time",
                            "topic_name", topicName,
                            "group_name", groupName,
                            "success", String.valueOf(success))
                    .record(Duration.of(durationNanos, ChronoUnit.NANOS));
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
