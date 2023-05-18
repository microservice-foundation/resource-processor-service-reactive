package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import com.epam.training.microservicefoundation.resourceprocessor.client.ResourceServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.client.SongServiceClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@TestConfiguration
@EnableConfigurationProperties(value = RetryProperties.class)
public class ClientConfiguration {

  @Value("${api-gateway.endpoint}")
  private String apiGatewayEndpoint;
  @Value("${web-client.connection.timeout}")
  private String connectionTimeout;
  @Value("${web-client.response.timeout}")
  private String responseTimeout;
  @Value("${web-client.read.timeout}")
  private String readTimeout;
  @Value("${web-client.write.timeout}")
  private String writeTimeout;

  private HttpClient httpClient() {
    return HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Integer.parseInt(connectionTimeout))
        .responseTimeout(Duration.ofMillis(Long.parseLong(responseTimeout)))
        .doOnConnected(connection ->
            connection.addHandlerFirst(new ReadTimeoutHandler(Long.parseLong(readTimeout),
                    TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(Long.parseLong(writeTimeout),
                    TimeUnit.MILLISECONDS)));
  }

  @Bean
  public WebClient webClient(ReactorLoadBalancerExchangeFilterFunction loadBalancerExchangeFilterFunction) {
    return WebClient.builder()
        .baseUrl(apiGatewayEndpoint)
        .filter(loadBalancerExchangeFilterFunction)
        .clientConnector(new ReactorClientHttpConnector(httpClient()))
        .build();
  }

  @Bean
  public ResourceServiceClient resourceServiceClient(WebClient webClient, RetryProperties retryProperties) {
    return new ResourceServiceClient(webClient, retryProperties);
  }

  @Bean
  public SongServiceClient songServiceClient(WebClient webClient, RetryProperties retryProperties) {
    return new SongServiceClient(webClient, retryProperties);
  }
}
