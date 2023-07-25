package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import com.epam.training.microservicefoundation.resourceprocessor.client.ResourceServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.client.SongServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.configuration.properties.WebClientProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.config.client.RetryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@TestConfiguration
@EnableConfigurationProperties({WebClientProperties.class, RetryProperties.class})
public class ClientConfiguration {

  private HttpClient httpClient(WebClientProperties properties) {
    return HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectionTimeout())
        .responseTimeout(Duration.ofMillis(properties.getResponseTimeout()))
        .doOnConnected(connection -> connection
            .addHandlerFirst(new ReadTimeoutHandler(properties.getReadTimeout(), TimeUnit.MILLISECONDS))
            .addHandlerLast(new WriteTimeoutHandler(properties.getWriteTimeout(), TimeUnit.MILLISECONDS)));
  }

  @Bean
  public WebClient webClient(WebClientProperties properties) {
    return WebClient.builder()
        .baseUrl(properties.getBaseUrl())
        .clientConnector(new ReactorClientHttpConnector(httpClient(properties)))
        .build();
  }

  @Bean
  public ResourceServiceClient resourceServiceClient(WebClient webClient, RetryProperties retryProperties,
      ReactiveCircuitBreaker reactiveCircuitBreaker) {
    return new ResourceServiceClient(webClient, retryProperties, reactiveCircuitBreaker);
  }

  @Bean
  public SongServiceClient songServiceClient(WebClient webClient, RetryProperties retryProperties,
      ReactiveCircuitBreaker reactiveCircuitBreaker) {
    return new SongServiceClient(webClient, retryProperties, reactiveCircuitBreaker);
  }

  @Bean
  public ReactiveCircuitBreaker circuitBreaker(ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory) {
    return circuitBreakerFactory.create("default");
  }
}
