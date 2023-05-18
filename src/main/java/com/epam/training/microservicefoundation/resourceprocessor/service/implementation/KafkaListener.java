package com.epam.training.microservicefoundation.resourceprocessor.service.implementation;

import com.epam.training.microservicefoundation.resourceprocessor.configuration.RetryProperties;
import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceRecord;
import com.epam.training.microservicefoundation.resourceprocessor.service.ReactiveKafkaEventListener;
import java.time.Duration;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

@Component
public class KafkaListener {
  private final Logger log = LoggerFactory.getLogger(KafkaListener.class);
  private final ReactiveKafkaConsumerTemplate<String, ResourceRecord> consumerTemplate;
  private final ReactiveKafkaEventListener<ResourceRecord> resourceRecordEventListener;
  private final RetryProperties retryProperties;

  public KafkaListener(ReactiveKafkaConsumerTemplate<String, ResourceRecord> consumerTemplate,
      ReactiveKafkaEventListener<ResourceRecord> resourceRecordEventListener, RetryProperties retryProperties) {
    this.consumerTemplate = consumerTemplate;
    this.resourceRecordEventListener = resourceRecordEventListener;
    this.retryProperties = retryProperties;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void subscribe() {
    listen(consumerTemplate, resourceRecordEventListener);
  }

  private <T> void listen(ReactiveKafkaConsumerTemplate<String, T> consumerTemplate, ReactiveKafkaEventListener<T> eventListener) {
    listenWithHandler(consumerTemplate, f -> handler(eventListener, f));
  }

  private <T> void listenWithHandler(ReactiveKafkaConsumerTemplate<String,T> consumerTemplate, Function<ReceiverRecord<String, T>,
        Mono<?>> handler) {
    consumerTemplate.receive()
        .doOnNext(receiverRecord -> log.info("Received event message key={}, value={}", receiverRecord.key(), receiverRecord.value()))
        .flatMap(receiverRecord -> handler.apply(receiverRecord)
            .doOnSuccess(r -> receiverRecord.receiverOffset().acknowledge())
            .doOnError(e -> log.error("Exception occurred in Listener", e))
            .retryWhen(Retry.backoff(retryProperties.getMaxRetries(), Duration.ofMillis(retryProperties.getInterval()))
                .transientErrors(true)))
        .repeat()
        .subscribe();
  }

  private <T> Mono<?> handler(ReactiveKafkaEventListener<T> eventListener, ReceiverRecord<String, T> receiverRecord) {
    return eventListener.eventListened(receiverRecord.value());
  }
}
