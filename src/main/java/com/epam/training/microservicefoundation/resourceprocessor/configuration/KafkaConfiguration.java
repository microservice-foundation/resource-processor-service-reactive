package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import com.epam.training.microservicefoundation.resourceprocessor.common.Pair;
import com.epam.training.microservicefoundation.resourceprocessor.configuration.properties.TopicProperties;
import com.epam.training.microservicefoundation.resourceprocessor.kafka.consumer.KafkaConsumer;
import com.epam.training.microservicefoundation.resourceprocessor.kafka.producer.KafkaProducer;
import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceStagedEvent;
import com.epam.training.microservicefoundation.resourceprocessor.service.ReactiveKafkaEventListener;
import com.epam.training.microservicefoundation.resourceprocessor.service.implementation.ResourceProcessorService;
import com.epam.training.microservicefoundation.resourceprocessor.service.implementation.ResourceStagedEventListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.cloud.config.client.RetryProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

@Configuration
@EnableKafka
@RefreshScope
public class KafkaConfiguration {
  @Bean
  public ReactiveKafkaProducerTemplate<String, Object> kafkaProducerTemplate(KafkaProperties properties) {
    return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(properties.buildProducerProperties()));
  }

  @Bean
  public KafkaProducer kafkaProducer(ReactiveKafkaProducerTemplate<String, Object> kafkaProducerTemplate,
      Map<Class<?>, Pair<String, Function<Object, ProducerRecord<String, Object>>>> publicationTopics) {
    return new KafkaProducer(kafkaProducerTemplate, publicationTopics);
  }

  @Bean
  public KafkaConsumer kafkaConsumer(
      List<Pair<ReactiveKafkaConsumerTemplate<String, Object>, ReactiveKafkaEventListener<Object>>> consumerAndListeners,
      DeadLetterPublishingRecoverer deadLetterPublishingRecoverer, RetryProperties retryProperties) {
    return new KafkaConsumer(deadLetterPublishingRecoverer, retryProperties, consumerAndListeners);
  }

  @Bean
  public List<Pair<ReactiveKafkaConsumerTemplate<String, ?>, ReactiveKafkaEventListener<?>>> consumerAndListeners(
      KafkaProperties kafkaProperties, TopicProperties topicProperties, ResourceProcessorService resourceProcessorService) {
    return List.of(Pair.of(resourceStagedEventConsumer(kafkaProperties, topicProperties), resourceStagedEventListener(resourceProcessorService)));
  }

  private ResourceStagedEventListener resourceStagedEventListener(ResourceProcessorService service) {
    return new ResourceStagedEventListener(service);
  }

  private ReactiveKafkaConsumerTemplate<String, ResourceStagedEvent> resourceStagedEventConsumer(KafkaProperties kafkaProperties,
      TopicProperties topicProperties) {
    ReceiverOptions<String, ResourceStagedEvent> basicReceiverOptions =
        ReceiverOptions.create(kafkaProperties.buildConsumerProperties());

    ReceiverOptions<String, ResourceStagedEvent> receiverOptions =
        basicReceiverOptions.subscription(Collections.singletonList(topicProperties.getResourcePermanent()));

    return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
  }

  @Bean
  public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaProperties kafkaProperties) {
    return new DeadLetterPublishingRecoverer(getEventKafkaTemplate(kafkaProperties));
  }

  private KafkaOperations<String, Object> getEventKafkaTemplate(KafkaProperties properties) {
    return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(properties.buildProducerProperties()));
  }
}
