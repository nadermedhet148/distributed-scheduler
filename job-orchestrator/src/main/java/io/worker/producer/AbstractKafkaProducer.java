package io.worker.producer;


import static io.worker.util.Common.generateRand;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

public sealed abstract class AbstractKafkaProducer<T extends AbstractKafkaProducer<T>> permits JobsProducer {

    private static final Logger log = Logger.getLogger(AbstractKafkaProducer.class);
    @ConfigProperty(name = "kafka.broker")
    String broker;
    private String topicName;
    private Producer<String, String> producer;

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
    }


    public void publish(String msg, Integer partition) {
        boolean success = false;
        try {
            var record = new ProducerRecord<>(this.topicName, partition, generateRand(), msg);

            this.producer.send(record);

        } catch (Exception e) {
            log.error("error publishing kafka event", e);
        }
    }
}
