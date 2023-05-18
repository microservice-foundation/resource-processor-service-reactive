package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "topic.name")
public final class TopicProperties {
  private final String resource;

  public TopicProperties(String resource) {
    this.resource = resource;
  }

  public String getResource() {
    return resource;
  }
}
