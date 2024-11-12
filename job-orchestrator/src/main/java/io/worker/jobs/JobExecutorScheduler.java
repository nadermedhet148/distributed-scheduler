package io.worker.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.worker.database.cassandra.JobsData;
import io.worker.producer.JobsProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class JobExecutorScheduler {
    private static final Logger log = Logger.getLogger(JobExecutorScheduler.class);

    @ConfigProperty(name = "partitions.count", defaultValue = "100")
    Integer partitionsCount;

    @Inject
    JobsData jobsData;

    @Inject
    JobsProducer jobsProducer;

    @Inject
    ObjectMapper objectMapper;


    public void exec() {
        try {
            var jobs = jobsData.getJobs();

            log.info("jobs: " + jobs.size());

            for (var ele : jobs) {
                try {
                    var job = objectMapper.writeValueAsString(ele);
                    log.info("to: " + ele.segment() % partitionsCount);
                    jobsProducer.publish(job, ele.segment() % partitionsCount);
                } catch (Exception e) {
                    log.error("e", e);
                }
            }
        } catch (Exception e) {
            log.error("e", e);
        }
    }
}
