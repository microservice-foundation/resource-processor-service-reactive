package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "topic.name")
@ConstructorBinding
public final class TopicProperties {
  private final String resource;

  public TopicProperties(String resource) {
    this.resource = resource;
  }

  public String getResource() {
    return resource;
  }
}
