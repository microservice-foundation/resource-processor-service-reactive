package com.epam.training.microservicefoundation.resourceprocessor.web.client;

import com.epam.training.microservicefoundation.resourceprocessor.domain.dto.GetSongDTO;
import com.epam.training.microservicefoundation.resourceprocessor.domain.dto.SaveSongDTO;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.config.client.RetryProperties;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class SongServiceClient {
  private static final Logger log = LoggerFactory.getLogger(SongServiceClient.class);
  private static final String SONGS = "/songs";
  private final WebClient webClient;
  private final RetryProperties retryProperties;
  private final ReactiveCircuitBreaker reactiveCircuitBreaker;

  public SongServiceClient(WebClient webClient, RetryProperties retryProperties, ReactiveCircuitBreaker reactiveCircuitBreaker) {
    this.webClient = webClient;
    this.retryProperties = retryProperties;
    this.reactiveCircuitBreaker = reactiveCircuitBreaker;
  }

  public Mono<GetSongDTO> post(SaveSongDTO saveSongDTO) {
    log.info("Sending a post request with song metadata '{}' to song service", saveSongDTO);
    return webClient.post().uri(uriBuilder -> uriBuilder.path(SONGS).build())
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(saveSongDTO)
        .retrieve()
        .bodyToMono(GetSongDTO.class)
        .transform(reactiveCircuitBreaker::run)
        .retryWhen(Retry.backoff(retryProperties.getMaxAttempts(), Duration.ofMillis(retryProperties.getInitialInterval()))
            .doBeforeRetry(retrySignal -> log.info("Retrying request: attempt {}", retrySignal.totalRetriesInARow())));
  }
}
