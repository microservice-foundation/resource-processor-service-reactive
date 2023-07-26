package com.epam.training.microservicefoundation.resourceprocessor.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.training.microservicefoundation.resourceprocessor.configuration.KafkaConfiguration;
import com.epam.training.microservicefoundation.resourceprocessor.configuration.TopicConfiguration;
import com.epam.training.microservicefoundation.resourceprocessor.configuration.properties.TopicProperties;
import com.epam.training.microservicefoundation.resourceprocessor.kafka.producer.KafkaProducer;
import com.epam.training.microservicefoundation.resourceprocessor.model.event.ResourceProcessedEvent;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.client.RetryProperties;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

@ExtendWith(value = {SpringExtension.class, KafkaExtension.class})
@EnableConfigurationProperties(value = {TopicProperties.class, KafkaProperties.class, RetryProperties.class})
@ContextConfiguration(classes = { KafkaConfiguration.class, TopicConfiguration.class})
@TestPropertySource(locations = "classpath:application.properties")
class KafkaProducerTest {
  @Autowired
  private KafkaProducer kafkaProducer;
  @Autowired
  private ReactiveKafkaConsumerTemplate<String, ResourceProcessedEvent> resourceProcessedEventConsumer;

  @Test
  void shouldPublishMessage() {
    ResourceProcessedEvent message = new ResourceProcessedEvent(1L);
    Mono<SenderResult<Void>> senderResultMono = kafkaProducer.publish(message);

    StepVerifier.create(senderResultMono)
        .assertNext(result -> {
          assertNull(result.exception());
          assertNotNull(result.recordMetadata());
          assertTrue(result.recordMetadata().hasOffset());
        }).verifyComplete();

    StepVerifier.create(resourceProcessedEventConsumer.receive().doOnNext(result -> result.receiverOffset().acknowledge()))
        .assertNext(receiverRecord -> assertThat(receiverRecord.value()).isEqualTo(message))
        .thenCancel()
        .verify(Duration.ofSeconds(10));
  }

  @Test
  void shouldNotPublishNonexistentMessageType() {
    StepVerifier.create(kafkaProducer.publish(new Object()))
        .expectError(IllegalStateException.class)
        .verify();
  }
}
