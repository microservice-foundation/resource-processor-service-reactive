package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "retry.backoff")
@ConstructorBinding
public final class RetryProperties {
  private final int interval;
  private final int maxRetries;
  private final int multiplier;


  public RetryProperties(int interval, int maxRetries, int multiplier) {
    this.interval = interval;
    this.maxRetries = maxRetries;
    this.multiplier = multiplier;
  }

  public int getInterval() {
    return interval;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public int getMultiplier() {
    return multiplier;
  }
}
