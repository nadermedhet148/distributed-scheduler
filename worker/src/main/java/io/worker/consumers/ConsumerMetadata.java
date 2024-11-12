package io.worker.consumers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConsumerMetadata {
    private static final Map<String, Long> ConsumerLagMetadata = new ConcurrentHashMap<>();


    public static void addConsumerLag(String partition, Long lag) {
        ConsumerLagMetadata.put(partition, lag);
    }

    public static Long getTotalLag() {
        return ConsumerLagMetadata.values().stream().reduce(Long::sum).orElse(0L);
    }
}
