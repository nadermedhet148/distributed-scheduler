package io.worker.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.worker.database.cassandra.JobsData;
import io.worker.producer.JobsProducer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class JobExecutorScheduler {
    private static final Logger log = Logger.getLogger(JobExecutorScheduler.class);

    @ConfigProperty(name = "partitions.count", defaultValue = "10")
    Integer partitionsCount;
    @ConfigProperty(name = "worker.count", defaultValue = "1")
    Integer workersCount;
    @ConfigProperty(name = "worker.max.capacity", defaultValue = "100")
    Long maxCapacity;

    @Inject
    JobsData jobsData;
    @Inject
    JobsProducer jobsProducer;
    @Inject
    ObjectMapper objectMapper;

    private static final Map<Integer, Integer> workerPartitions = new ConcurrentHashMap<Integer, Integer>();


    @PostConstruct
    private void init() throws Exception {
        for (int i = 0; i < workersCount; i++) {
            var partitions = getPartitions(i);
            for (var partition : partitions) {
                workerPartitions.put(partition, i);
            }
        }
        log.info("workerPartitions: " + objectMapper.writeValueAsString(workerPartitions));
    }


    private ArrayList<Integer> getPartitions(Integer workerId) {
        int partitionsPerWorker = partitionsCount / workersCount;
        int startPartition = workerId * partitionsPerWorker;
        int endPartition = startPartition + partitionsPerWorker;

        var partitions = new ArrayList<Integer>();
        for (int i = startPartition; i < endPartition; i++) {
            partitions.add(i);
        }
        return partitions;
    }


    public void exec() {
        try {
            var jobs = jobsData.getJobs();
            log.info("jobs: " + jobs.size());
            jobs.forEach(ele -> {
                try {
                    var job = objectMapper.writeValueAsString(ele);
                    var partition = getPartition(ele.segment(), maxCapacity);
                    jobsProducer.publish(job, partition);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            });
        } catch (Exception e) {
            log.error("e", e);
        }
        // dummy data creation
        jobsData.dummyData();
    }

    private int getPartition(Integer segment, Long maxCapacity) {
        var partition = segment % partitionsCount;
        var workerId = workerPartitions.get(partition);
        var lag = WorkerMetadata.getWorkerLag(workerId);
        if (lag != null && lag > maxCapacity) {
            getPartition(segment + (partitionsCount / workersCount), maxCapacity * 2);
        }
        log.info("to: " + partition);
        return partition;
    }
}
