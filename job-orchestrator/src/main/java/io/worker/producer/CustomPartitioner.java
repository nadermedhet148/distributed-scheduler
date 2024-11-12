package io.worker.producer;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

import java.util.Map;

public class CustomPartitioner implements Partitioner {
    private static final int PREMIUM_PARTITION = 0;
    private static final int NORMAL_PARTITION = 1;


    @Override
    public void close() {

    }

    private String extractCustomerType(String key) {
        String[] parts = key.split("_");
        return parts.length > 1 ? parts[1] : "normal";
    }

    @Override
    public int partition(String s, Object o, byte[] bytes, Object o1, byte[] bytes1, Cluster cluster) {
//        String customerType = extractCustomerType(key.toString());
//        return "premium".equalsIgnoreCase(customerType) ? PREMIUM_PARTITION : NORMAL_PARTITION;
        return 0;
    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}