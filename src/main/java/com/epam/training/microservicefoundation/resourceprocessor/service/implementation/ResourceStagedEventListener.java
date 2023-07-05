package com.epam.training.microservicefoundation.resourceprocessor.service.implementation;

import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceStagedEvent;
import com.epam.training.microservicefoundation.resourceprocessor.service.ReactiveKafkaEventListener;
import reactor.core.publisher.Mono;

public class ResourceStagedEventListener implements ReactiveKafkaEventListener<ResourceStagedEvent> {
  private final ResourceProcessorService service;

  public ResourceStagedEventListener(ResourceProcessorService service) {
    this.service = service;
  }

  @Override
  public Mono<Void> eventListened(ResourceStagedEvent event) {
    return service.processResource(event);
  }
}
