package io.worker.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import static io.worker.consumers.ConsumerMetadata.getTotalLag;

@Liveness
@ApplicationScoped
public final class FullHealthCheck implements HealthCheck {
    @ConfigProperty(name = "worker.id", defaultValue = "0")
    Integer workerId;
    @Inject
    @Override
    public HealthCheckResponse call() {
        var hcBuilder = HealthCheckResponse.named("All health checks");
        try {
            var allChecked = true;
            return hcBuilder
                    .status(allChecked)
                    .withData("total_lag", getTotalLag())
                    .withData("worker_id", workerId)
                    .build();
        } catch (Exception e) {
            return hcBuilder
                    .status(false)
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
