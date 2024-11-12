package io.worker.producer;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class JobsProducer extends AbstractKafkaProducer<JobsProducer> {

  @ConfigProperty(name = "kafka.topic.jobs", defaultValue = "jobs")
  String topicName;

  @PostConstruct
  void init() {super.prepareProducer(this.topicName);}
}
