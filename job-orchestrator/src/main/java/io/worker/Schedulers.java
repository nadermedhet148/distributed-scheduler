package io.worker;

import static io.worker.util.Common.TRACE_ID;

import io.worker.jobs.JobExecutorScheduler;
import io.quarkus.scheduler.Scheduled;
import io.worker.jobs.WorkerStatusScheduler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public final class Schedulers {

  private static final Logger log = Logger.getLogger(Schedulers.class);

  @Inject
  JobExecutorScheduler jobExecutorScheduler;

  @Inject
  WorkerStatusScheduler workerStatusScheduler;

  @Scheduled(every = "1s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
  void job() {
    log.info("run worker status");
    workerStatusScheduler.exec();
    log.info("run job executor");
    jobExecutorScheduler.exec();
  }

}
