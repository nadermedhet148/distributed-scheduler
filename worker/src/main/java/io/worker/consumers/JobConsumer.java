package io.worker.consumers;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.worker.model.Job;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Random;

@ApplicationScoped
public final class JobConsumer extends AbstractKafkaConsumer<JobConsumer> {

    private static final Logger log = Logger.getLogger(AbstractKafkaConsumer.class);

    @ConfigProperty(name = "kafka.jobs.topic", defaultValue = "jobs")
    String jobTopic;
    @ConfigProperty(name = "kafka.jobs.group", defaultValue = "jobs_group")
    String jobGroup;
    @ConfigProperty(name = "worker.id", defaultValue = "2")
    Integer workerId;
    @ConfigProperty(name = "worker.count", defaultValue = "1")
    Integer workersCount;
    @ConfigProperty(name = "partitions.count", defaultValue = "100")
    Integer partitionsCount;

    @Inject
    ObjectMapper objectMapper;


    @PostConstruct
    void init() {
        this.initConsumer(jobGroup, jobTopic, getPartitions());
    }

    private ArrayList<Integer> getPartitions() {
        int partitionsPerWorker = partitionsCount / workersCount;
        int startPartition = workerId * partitionsPerWorker;
        int endPartition = startPartition + partitionsPerWorker;

        var partitions = new ArrayList<Integer>();
        for (int i = startPartition; i < endPartition; i++) {
            partitions.add(i);
        }
        return partitions;
    }

    @Override
    public void consume(String body) throws Exception {
        try {
            var waitingTime = (new Random().nextInt(10) + 1) * 100;
            Thread.sleep(waitingTime);
            var job = objectMapper.readValue(body, Job.class);
//            System.out.println("Consumed: " + job.id() + " waiting time: " + waitingTime);

        } catch (Exception e) {
            log.error("Error consuming message", e);
        }

    }


    @Override
    JobConsumer getThis() {
        return this;
    }

}


