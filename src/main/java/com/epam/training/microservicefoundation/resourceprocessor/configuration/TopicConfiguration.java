package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import com.epam.training.microservicefoundation.resourceprocessor.common.Pair;
import com.epam.training.microservicefoundation.resourceprocessor.configuration.properties.TopicProperties;
import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceProcessedEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RefreshScope
@EnableConfigurationProperties(TopicProperties.class)
public class TopicConfiguration {
  @Bean
  NewTopic resourcePermanent(TopicProperties properties) {
    return TopicBuilder
        .name(properties.getResourcePermanent())
        .partitions(properties.getProperties().getPartitionCount())
        .replicas(properties.getProperties().getReplicationFactor())
        .build();
  }

  @Bean
  public Map<Class<?>, Pair<String, Function<Object, ProducerRecord<String, Object>>>> publicationTopics(TopicProperties properties) {
    Map<Class<?>, Pair<String, Function<Object, ProducerRecord<String, Object>>>> map = new HashMap<>();

    map.put(ResourceProcessedEvent.class,
        Pair.of(properties.getResourcePermanent(), message -> new ProducerRecord<>(properties.getResourcePermanent(),
            String.valueOf(((ResourceProcessedEvent) message).getId()), message)));

    return map;
  }
}
