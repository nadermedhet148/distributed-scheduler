package io.worker.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.worker.database.cassandra.JobsData;
import io.worker.producer.JobsProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class WorkerStatusScheduler {
    private static final Logger log = Logger.getLogger(WorkerStatusScheduler.class);


    @ConfigProperty(name = "worker.urls", defaultValue = "http://localhost:8884/health")
    String workerUrls;


    @Inject
    ObjectMapper objectMapper;


    public void exec() {
        try {
            var workerUrlsArray = workerUrls.split(",");
            for (var workerUrl : workerUrlsArray) {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(workerUrl))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                var body = objectMapper.readTree(response.body());
                var totalLag = body.path("checks").path(0).path("data").get("total_lag");
                var workerId = body.path("checks").path(0).path("data").get("worker_id");

                log.info("Worker status: " + workerId + " " + totalLag);
                WorkerMetadata.clearMetaData();
                WorkerMetadata.addWorkerLag(workerId.intValue(), totalLag.asLong());
            }

        } catch (Exception e) {
            log.error("e", e);
        }
    }
}
