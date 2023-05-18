package com.epam.training.microservicefoundation.resourceprocessor.service;

import reactor.core.publisher.Mono;

public interface ReactiveKafkaEventListener<E> {
  Mono<Void> eventListened(E event);
}
