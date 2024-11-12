package io.worker.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public final class FullHealthCheck implements HealthCheck {

  @Inject
  @Override
  public HealthCheckResponse call() {
    var hcBuilder = HealthCheckResponse.named("All health checks");
    try {
      // TODO: Add more health checks here
      var allChecked = true;
      return hcBuilder
          .status(allChecked)
          .build();
    } catch (Exception e) {
      return hcBuilder
          .status(false)
          .withData("error", e.getMessage())
          .build();
    }
  }
}
