package com.epam.training.microservicefoundation.resourceprocessor.client;

import static com.epam.training.microservicefoundation.resourceprocessor.client.Server.Service.SONG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.training.microservicefoundation.resourceprocessor.configuration.ClientConfiguration;
import com.epam.training.microservicefoundation.resourceprocessor.configuration.properties.WebClientProperties;
import com.epam.training.microservicefoundation.resourceprocessor.model.SongDTO;
import com.epam.training.microservicefoundation.resourceprocessor.model.SongMetadata;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.client.RetryProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith({MockServerExtension.class, SpringExtension.class})
@EnableConfigurationProperties({WebClientProperties.class, RetryProperties.class})
@ContextConfiguration(classes = ClientConfiguration.class)
@TestPropertySource(locations = "classpath:application.properties")
class SongServiceClientTest {
  @Autowired
  private SongServiceClient songServiceClient;

  @Test
  void shouldPostSongMetadata(@Server(service = SONG) MockServer server) {
    SongDTO songDTO = new SongDTO(2L);
    server.response(HttpStatus.CREATED, songDTO, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    Mono<SongDTO> post = songServiceClient.post(SongMetadata.builder().resourceId(123L).album("Different").year(2023).name("Ubiquitous")
        .artist("Hoshim").length("3:56").build());

    StepVerifier.create(post)
        .assertNext(result -> assertEquals(songDTO.getId(), result.getId()))
        .verifyComplete();
  }

  @Test
  void shouldPostSongMetadataAfterRetries(@Server(service = SONG) MockServer server) {
    // After one retry
    SongDTO songDTO1 = new SongDTO(2L);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.CREATED, songDTO1, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    Mono<SongDTO> post1 = songServiceClient.post(SongMetadata.builder().resourceId(123L).album("Different").year(2023).name("Ubiquitous")
        .artist("Hoshim").length("3:56").build());

    StepVerifier.create(post1)
        .assertNext(result -> assertEquals(songDTO1.getId(), result.getId()))
        .verifyComplete();

    // After two retries
    SongDTO songDTO2 = new SongDTO(2L);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.BAD_REQUEST);
    server.response(HttpStatus.CREATED, songDTO2, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    Mono<SongDTO> post2 = songServiceClient.post(SongMetadata.builder().resourceId(123L).album("Different").year(2023).name("Ubiquitous")
        .artist("Hoshim").length("3:56").build());

    StepVerifier.create(post2)
        .assertNext(result -> assertEquals(songDTO2.getId(), result.getId()))
        .verifyComplete();

    // After three retries
    SongDTO songDTO3 = new SongDTO(2L);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.BAD_REQUEST);
    server.response(HttpStatus.INTERNAL_SERVER_ERROR);
    server.response(HttpStatus.CREATED, songDTO3, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    Mono<SongDTO> post3 = songServiceClient.post(SongMetadata.builder().resourceId(123L).album("Different").year(2023).name("Ubiquitous")
        .artist("Hoshim").length("3:56").build());

    StepVerifier.create(post3)
        .assertNext(result -> assertEquals(songDTO3.getId(), result.getId()))
        .verifyComplete();

    // Retries exhausted after three retries
    SongDTO songDTO4 = new SongDTO(2L);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.BAD_REQUEST);
    server.response(HttpStatus.INTERNAL_SERVER_ERROR);
    server.response(HttpStatus.BAD_REQUEST);
    server.response(HttpStatus.CREATED, songDTO4, Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    Mono<SongDTO> post4 = songServiceClient.post(SongMetadata.builder().resourceId(123L).album("Different").year(2023).name("Ubiquitous")
        .artist("Hoshim").length("3:56").build());

    StepVerifier.create(post4)
        .consumeErrorWith(Exceptions::isRetryExhausted)
        .verify();
  }

  @Test
  void shouldFailRetryWhenPostSongMetadata(@Server(service = SONG) MockServer server) {
    SongDTO songDTO = new SongDTO(2L);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.BAD_REQUEST);
    server.response(HttpStatus.INTERNAL_SERVER_ERROR);
    Mono<SongDTO> post = songServiceClient.post(SongMetadata.builder().resourceId(123L).album("Different").year(2023).name("Ubiquitous")
        .artist("Hoshim").length("3:56").build());

    StepVerifier.create(post)
        .consumeErrorWith(Exceptions::isRetryExhausted)
        .verify();
  }
}
