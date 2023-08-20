package com.epam.training.microservicefoundation.resourceprocessor.client;

import static com.epam.training.microservicefoundation.resourceprocessor.client.Server.Service.SONG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.training.microservicefoundation.resourceprocessor.model.dto.GetSongDTO;
import com.epam.training.microservicefoundation.resourceprocessor.model.dto.SaveSongDTO;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SongServiceClientTest extends BaseClientTest {
  @Autowired
  private SongServiceClient songServiceClient;
  private final GetSongDTO getSongDTO = new GetSongDTO(1L, 123L, "Sound", "Sounds", "Sato", 10, 2012);
  private final SaveSongDTO saveSongDTO = new SaveSongDTO(123L, "Sound", "Sounds", "Sato", 10, 2012);

  @Test
  void shouldPostSongMetadata(@Server(service = SONG) MockServer server) {
    server.response(HttpStatus.CREATED, getSongDTO, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    Mono<GetSongDTO> post = songServiceClient.post(saveSongDTO);

    StepVerifier.create(post)
        .assertNext(result -> assertEquals(getSongDTO, result))
        .verifyComplete();
  }

  @Test
  void shouldPostSongMetadataAfterRetries(@Server(service = SONG) MockServer server) {
    // After one retry
    server.response(HttpStatus.NOT_FOUND);
    server.response(HttpStatus.CREATED, getSongDTO, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    Mono<GetSongDTO> post1 = songServiceClient.post(saveSongDTO);

    StepVerifier.create(post1)
        .assertNext(result -> assertEquals(getSongDTO, result))
        .verifyComplete();

    // After two retries
    server.response(HttpStatus.BAD_REQUEST);
    server.response(HttpStatus.BAD_REQUEST);
    server.response(HttpStatus.CREATED, getSongDTO, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    Mono<GetSongDTO> post2 = songServiceClient.post(saveSongDTO);

    StepVerifier.create(post2)
        .assertNext(result -> assertEquals(getSongDTO, result))
        .verifyComplete();

    // After three retries
    server.response(HttpStatus.NOT_FOUND);
    server.response(HttpStatus.BAD_REQUEST);
    server.response(HttpStatus.NOT_FOUND);
    server.response(HttpStatus.CREATED, getSongDTO, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    Mono<GetSongDTO> post3 = songServiceClient.post(saveSongDTO);

    StepVerifier.create(post3)
        .assertNext(result -> assertEquals(getSongDTO, result))
        .verifyComplete();
  }

  @Test
  void shouldFailRetryWhenPostSongMetadata(@Server(service = SONG) MockServer server) {
    server.response(HttpStatus.BAD_REQUEST);
    server.response(HttpStatus.BAD_REQUEST);
    server.response(HttpStatus.BAD_REQUEST);
    Mono<GetSongDTO> post = songServiceClient.post(saveSongDTO);

    StepVerifier.create(post)
        .consumeErrorWith(Exceptions::isRetryExhausted)
        .verify();
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
  void shouldChangeToOpenStateOfCircuitBreakerWhenPostSongMetadataAfterRetries(@Server(service = SONG) MockServer server) {
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);

    StepVerifier.create(songServiceClient.post(saveSongDTO))
        .consumeErrorWith(Exceptions::isRetryExhausted)
        .verify();
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
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
