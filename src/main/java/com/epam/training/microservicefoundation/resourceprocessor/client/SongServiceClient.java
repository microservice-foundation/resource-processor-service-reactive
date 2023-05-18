package com.epam.training.microservicefoundation.resourceprocessor.client;

import com.epam.training.microservicefoundation.resourceprocessor.configuration.RetryProperties;
import com.epam.training.microservicefoundation.resourceprocessor.model.SongMetadata;
import com.epam.training.microservicefoundation.resourceprocessor.model.SongRecord;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class SongServiceClient {
  private static final Logger log = LoggerFactory.getLogger(SongServiceClient.class);
  private static final String SONGS = "/songs";
  private final WebClient webClient;
  private final RetryProperties retryProperties;

  public SongServiceClient(WebClient webClient, RetryProperties retryProperties) {
    this.webClient = webClient;
    this.retryProperties = retryProperties;
  }

  public Mono<SongRecord> post(SongMetadata songMetadata) {
    log.info("Sending a post request with song metadata '{}' to song service", songMetadata);
    return webClient.post().uri(uriBuilder -> uriBuilder.path(SONGS).build())
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(songMetadata)
        .retrieve()
        .bodyToMono(SongRecord.class)
        .retryWhen(Retry.backoff(retryProperties.getMaxRetries(),
            Duration.ofSeconds(retryProperties.getInterval())));

  }

}
