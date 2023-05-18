package com.epam.training.microservicefoundation.resourceprocessor.service.implementation;

import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceRecord;
import com.epam.training.microservicefoundation.resourceprocessor.service.ReactiveKafkaEventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ResourceRecordEventListener implements ReactiveKafkaEventListener<ResourceRecord> {
  private final ResourceProcessorService service;

  public ResourceRecordEventListener(ResourceProcessorService service) {
    this.service = service;
  }

  @Override
  public Mono<Void> eventListened(ResourceRecord event) {
    return service.processResource(event);
  }
}
