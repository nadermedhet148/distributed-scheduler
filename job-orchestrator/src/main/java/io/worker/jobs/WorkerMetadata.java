package io.worker.jobs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WorkerMetadata {
    private static final Map<Integer, Long> WorkersLag = new ConcurrentHashMap<>();


    public static void addWorkerLag(Integer workerId, Long lag) {
        WorkersLag.put(workerId, lag);
    }

    public static Long getWorkerLag(Integer workerId) {
        return WorkersLag.getOrDefault(workerId, null);
    }
    public static void clearMetaData() {
        WorkersLag.forEach((k, v) -> WorkersLag.remove(k));
        System.out.printf("WorkerMetadata cleared", WorkersLag);
    }


}
