package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import com.epam.training.microservicefoundation.resourceprocessor.common.Pair;
import com.epam.training.microservicefoundation.resourceprocessor.configuration.properties.TopicProperties;
import com.epam.training.microservicefoundation.resourceprocessor.kafka.producer.KafkaProducer;
import com.epam.training.microservicefoundation.resourceprocessor.domain.event.ResourceProcessedEvent;
import io.micrometer.observation.ObservationRegistry;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.config.client.RetryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

@TestConfiguration
@EnableKafka
@EnableConfigurationProperties(value = {TopicProperties.class, KafkaProperties.class, RetryProperties.class})
public class KafkaConfiguration {

  @Bean
  public ReactiveKafkaProducerTemplate<String, Object> kafkaProducerTemplate(KafkaProperties properties) {
    return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(properties.buildProducerProperties()));
  }

  @Bean
  public KafkaProducer kafkaProducer(ReactiveKafkaProducerTemplate<String, Object> kafkaProducerTemplate,
      Map<Class<?>, Pair<String, Function<Object, ProducerRecord<String, Object>>>> publicationTopics, ObservationRegistry registry) {
    return new KafkaProducer(kafkaProducerTemplate, publicationTopics, registry);
  }

  @Bean
  public ReactiveKafkaConsumerTemplate<String, ResourceProcessedEvent> resourceProcessedEventConsumer(KafkaProperties kafkaProperties,
      TopicProperties topicProperties) {
    ReceiverOptions<String, ResourceProcessedEvent> basicReceiverOptions = ReceiverOptions.create(kafkaProperties.buildConsumerProperties());
    ReceiverOptions<String, ResourceProcessedEvent> receiverOptions =
        basicReceiverOptions.subscription(Collections.singletonList(topicProperties.getResourcePermanent()));
    return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
  }

  @Bean
  public ObservationRegistry observationRegistry() {
    return ObservationRegistry.create();
  }
}
