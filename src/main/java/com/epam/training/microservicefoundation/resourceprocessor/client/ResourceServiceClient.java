package com.epam.training.microservicefoundation.resourceprocessor.client;

import com.epam.training.microservicefoundation.resourceprocessor.configuration.RetryProperties;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

public class ResourceServiceClient {
  private static final Logger log = LoggerFactory.getLogger(ResourceServiceClient.class);
  private static final String RESOURCES = "/resources";
  private static final String ID = "/{id}";
  private final WebClient webClient;
  private final RetryProperties retryProperties;

  public ResourceServiceClient(WebClient webClient, RetryProperties retryProperties) {
    this.webClient = webClient;
    this.retryProperties = retryProperties;
  }

  public Flux<DataBuffer> getById(long id) {
    log.info("Getting resource file by resource id '{}' from resource service", id);
    return webClient.get().uri(uriBuilder -> uriBuilder.path(RESOURCES).path(ID).build(id))
        .accept(MediaType.APPLICATION_OCTET_STREAM)
        .retrieve().bodyToFlux(DataBuffer.class)
        .retryWhen(Retry.backoff(retryProperties.getMaxRetries(),
            Duration.ofSeconds(retryProperties.getInterval())));
  }

}
