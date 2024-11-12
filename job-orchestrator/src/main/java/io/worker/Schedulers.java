package io.worker;

import static io.worker.util.Common.TRACE_ID;

import io.worker.jobs.JobExecutorScheduler;
import io.quarkus.scheduler.Scheduled;
import io.worker.util.TraceIdGen;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@ApplicationScoped
public final class Schedulers {

  private static final Logger log = Logger.getLogger(Schedulers.class);

  @Inject
  private JobExecutorScheduler jobExecutorScheduler;

  @Scheduled(every = "1m")
  void job() {
    String traceId = TraceIdGen.hexId();
    MDC.put(TRACE_ID, traceId);

    log.info("clear ws zombies");
    jobExecutorScheduler.exec();
  }
}
