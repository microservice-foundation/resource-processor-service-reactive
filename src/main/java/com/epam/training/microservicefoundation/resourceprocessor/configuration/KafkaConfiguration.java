package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import com.epam.training.microservicefoundation.resourceprocessor.common.Pair;
import com.epam.training.microservicefoundation.resourceprocessor.configuration.properties.TopicProperties;
import com.epam.training.microservicefoundation.resourceprocessor.kafka.consumer.KafkaConsumer;
import com.epam.training.microservicefoundation.resourceprocessor.kafka.producer.KafkaProducer;
import com.epam.training.microservicefoundation.resourceprocessor.model.event.ResourceStagedEvent;
import com.epam.training.microservicefoundation.resourceprocessor.service.implementation.ResourceProcessorService;
import com.epam.training.microservicefoundation.resourceprocessor.service.implementation.ResourceStagedEventListener;
import java.util.Collections;
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
  public KafkaConsumer kafkaConsumer(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer, RetryProperties retryProperties,
      KafkaProperties kafkaProperties, TopicProperties topicProperties, ResourceProcessorService resourceProcessorService) {
    return new KafkaConsumer(deadLetterPublishingRecoverer, retryProperties, Pair.of(resourceStagedEventConsumer(kafkaProperties,
        topicProperties), new ResourceStagedEventListener(resourceProcessorService)));
  }

  private ReactiveKafkaConsumerTemplate<String, ResourceStagedEvent> resourceStagedEventConsumer(KafkaProperties kafkaProperties,
      TopicProperties topicProperties) {
    ReceiverOptions<String, ResourceStagedEvent> basicReceiverOptions =
        ReceiverOptions.create(kafkaProperties.buildConsumerProperties());

    ReceiverOptions<String, ResourceStagedEvent> receiverOptions =
        basicReceiverOptions.subscription(Collections.singletonList(topicProperties.getResourceStaging()));

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
