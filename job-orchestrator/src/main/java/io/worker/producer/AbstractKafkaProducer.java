package io.worker.producer;


import static io.worker.util.Common.TRACE_ID;
import static io.worker.util.Common.generateRand;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.kafka.KafkaClientMetrics;
import jakarta.inject.Inject;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.slf4j.MDC;

public sealed abstract class AbstractKafkaProducer<T extends AbstractKafkaProducer<T>> permits JobsProducer {

    private static final Logger log = Logger.getLogger(AbstractKafkaProducer.class);
    @ConfigProperty(name = "kafka.broker")
    String broker;
    private String topicName;
    private Producer<String, String> producer;

    @Inject
    MeterRegistry registry;

    private Timer kafkaProducerTimer;
    private Counter successCounter;
    private Counter failureCounter;

    void prepareProducer(String topic) {
        this.topicName = topic;
        var properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, 1);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(properties);
        registerMeters();
    }

    private void registerMeters() {
        var metrics = new KafkaClientMetrics(producer);
        metrics.bindTo(registry);
        this.kafkaProducerTimer = registry.timer("kafka_producer_msg", "topic_name", topicName);
        this.successCounter = registry.counter("kafka_producer_msg_success_total", "topic_name", topicName);
        this.failureCounter = registry.counter("kafka_producer_msg_failure_total", "topic_name", topicName);
    }

    public void publish(String msg, Integer partition) {
        Timer.Sample sample = Timer.start(registry);
        boolean success = false;
        try {
            var traceId = (String) MDC.get(TRACE_ID);

            var record = new ProducerRecord<>(this.topicName, partition, generateRand(), msg);
            record.headers().add(new RecordHeader(TRACE_ID, traceId.getBytes()));

            this.producer.send(record);

            success = true;
        } catch (Exception e) {
            log.error("error publishing kafka event", e);
        } finally {
            recordMessageDuration(success, sample);
        }
    }

    private void recordMessageDuration(boolean success, Timer.Sample sample) {
        if (kafkaProducerTimer != null) {
            sample.stop(kafkaProducerTimer);
        }

        var counter = success ? successCounter : failureCounter;
        if (counter != null) {
            counter.increment();
        }
    }
}
