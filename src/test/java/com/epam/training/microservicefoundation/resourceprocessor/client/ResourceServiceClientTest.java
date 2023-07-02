package com.epam.training.microservicefoundation.resourceprocessor.client;

import static com.epam.training.microservicefoundation.resourceprocessor.client.Server.Service.RESOURCE;

import com.epam.training.microservicefoundation.resourceprocessor.configuration.ClientConfiguration;
import com.epam.training.microservicefoundation.resourceprocessor.configuration.properties.WebClientProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import kotlin.jvm.functions.Function1;
import okio.Buffer;
import okio.Okio;
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
import org.springframework.util.ResourceUtils;
import reactor.core.Exceptions;
import reactor.test.StepVerifier;

@ExtendWith({MockServerExtension.class, SpringExtension.class})
@EnableConfigurationProperties({WebClientProperties.class, RetryProperties.class})
@ContextConfiguration(classes = ClientConfiguration.class)
@TestPropertySource(locations = "classpath:application.properties")
class ResourceServiceClientTest {
  @Autowired
  private ResourceServiceClient resourceServiceClient;

  @Test
  void shouldGetById(@Server(service = RESOURCE) MockServer server) throws IOException {
    server.response(HttpStatus.OK, fileBuffer(),
        Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE));

    StepVerifier.create(resourceServiceClient.getById(123L))
        .expectNextCount(26)
        .verifyComplete();
  }

  @Test
  void shouldGetByIdAfterRetries(@Server(service = RESOURCE) MockServer server) throws IOException {
    // After one retry
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.OK, fileBuffer(),
        Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE));

    StepVerifier.create(resourceServiceClient.getById(123L))
        .expectNextCount(26)
        .verifyComplete();

    // After two retries
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.NOT_FOUND);
    server.response(HttpStatus.OK, fileBuffer(),
        Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE));

    StepVerifier.create(resourceServiceClient.getById(123L))
        .expectNextCount(26)
        .verifyComplete();

    // After three retries
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.NOT_FOUND);
    server.response(HttpStatus.INTERNAL_SERVER_ERROR);
    server.response(HttpStatus.OK, fileBuffer(),
        Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE));

    StepVerifier.create(resourceServiceClient.getById(123L))
        .expectNextCount(26)
        .verifyComplete();

    // Retries exhausted after three retries
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.NOT_FOUND);
    server.response(HttpStatus.INTERNAL_SERVER_ERROR);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.OK, fileBuffer(),
        Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE));

    StepVerifier.create(resourceServiceClient.getById(123L))
        .consumeErrorWith(Exceptions::isRetryExhausted)
        .verify();
  }

  @Test
  void shouldFailRetryWhenGetById(@Server(service = RESOURCE) MockServer server) {
    // Read timeout and retry exhausted after three retries
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.NOT_FOUND);
    server.response(HttpStatus.INTERNAL_SERVER_ERROR);

    StepVerifier.create(resourceServiceClient.getById(123L))
        .consumeErrorWith(Exceptions::isRetryExhausted)
        .verify();
  }

  private Buffer fileBuffer() throws IOException {
    File file = testFile();
    Buffer buffer = Okio.buffer(Okio.source(file)).getBuffer();
    Okio.use(buffer, (Function1<Buffer, Object>) buffer1 -> {
      try {
        return buffer.writeAll(Okio.source(file));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    return buffer;
  }

  private File testFile() throws IOException {
    File file = ResourceUtils.getFile("src/test/resources/files/mpthreetest.mp3");
    File testFile = ResourceUtils.getFile("src/test/resources/files/test.mp3");
    if (!testFile.exists()) {
      Files.copy(file.toPath(), testFile.toPath());
    }
    return testFile;
  }
}
