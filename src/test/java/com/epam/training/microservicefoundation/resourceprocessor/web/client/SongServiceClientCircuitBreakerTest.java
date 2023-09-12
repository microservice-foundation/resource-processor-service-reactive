package com.epam.training.microservicefoundation.resourceprocessor.web.client;

import static com.epam.training.microservicefoundation.resourceprocessor.web.client.Server.Service.SONG;

import com.epam.training.microservicefoundation.resourceprocessor.domain.dto.GetSongDTO;
import com.epam.training.microservicefoundation.resourceprocessor.domain.dto.SaveSongDTO;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.core.Exceptions;
import reactor.test.StepVerifier;

class SongServiceClientCircuitBreakerTest extends BaseClientTest {
  @Autowired
  private SongServiceClient songServiceClient;
  private final GetSongDTO getSongDTO = new GetSongDTO(1L, 123L, "Sound", "Sounds", "Sato", 10, 2012);
  private final SaveSongDTO saveSongDTO = new SaveSongDTO(123L, "Sound", "Sounds", "Sato", 10, 2012);

  @Test
  void shouldChangeToOpenStateOfCircuitBreakerWhenPostSongMetadataAfterRetries(@Server(service = SONG) MockServer server) {
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);

    StepVerifier.create(songServiceClient.post(saveSongDTO))
        .consumeErrorWith(Exceptions::isRetryExhausted)
        .verify();
  }

  @Test
  void shouldChangeFromHalfOpenToClosedStateOfCircuitBreakerWhenPostSongMetadataAfterRetries(@Server(service = SONG) MockServer server) {
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);

    StepVerifier.create(songServiceClient.post(saveSongDTO))
        .consumeErrorWith(Exceptions::isRetryExhausted)
        .verify();

    server.response(HttpStatus.INTERNAL_SERVER_ERROR);
    server.response(HttpStatus.CREATED, getSongDTO, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    StepVerifier.create(songServiceClient.post(saveSongDTO))
        .expectNext(getSongDTO)
        .verifyComplete();

    server.response(HttpStatus.CREATED, getSongDTO, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    StepVerifier.create(songServiceClient.post(saveSongDTO))
        .expectNext(getSongDTO)
        .verifyComplete();
  }
}
