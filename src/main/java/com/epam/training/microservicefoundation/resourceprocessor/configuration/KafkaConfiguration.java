package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceRecord;
import java.util.Collections;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import reactor.kafka.receiver.ReceiverOptions;

@Configuration
@EnableKafka
@RefreshScope
@EnableConfigurationProperties(TopicProperties.class)
public class KafkaConfiguration {
  @Bean
  public ReactiveKafkaConsumerTemplate<String, ResourceRecord> kafkaConsumerTemplate(KafkaProperties kafkaProperties,
      TopicProperties topicProperties) {
    ReceiverOptions<String, ResourceRecord> basicReceiverOptions =
        ReceiverOptions.create(kafkaProperties.buildConsumerProperties());

    ReceiverOptions<String, ResourceRecord> receiverOptions =
        basicReceiverOptions.subscription(Collections.singletonList(topicProperties.getResource()));

    return new ReactiveKafkaConsumerTemplate<>(receiverOptions);
  }
}
