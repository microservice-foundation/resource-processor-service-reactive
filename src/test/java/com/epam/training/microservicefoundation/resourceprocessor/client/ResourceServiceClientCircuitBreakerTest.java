package com.epam.training.microservicefoundation.resourceprocessor.client;

import static com.epam.training.microservicefoundation.resourceprocessor.client.Server.Service.RESOURCE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import kotlin.jvm.functions.Function1;
import okio.Buffer;
import okio.Okio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;
import reactor.core.Exceptions;
import reactor.test.StepVerifier;

class ResourceServiceClientCircuitBreakerTest extends BaseClientTest {
  @Autowired
  private ResourceServiceClient resourceServiceClient;

  @Test
  void shouldChangeToOpenStateOfCircuitBreakerWhenGetByIdAfterRetries(@Server(service = RESOURCE) MockServer server) {
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.NOT_FOUND);
    server.response(HttpStatus.INTERNAL_SERVER_ERROR);

    StepVerifier.create(resourceServiceClient.getById(123L))
        .consumeErrorWith(Exceptions::isRetryExhausted)
        .verify();
  }

  @Test
  void shouldChangeFromHalfOpenToClosedStateOfCircuitBreakerWhenGetByIdAfterRetries(@Server(service = RESOURCE) MockServer server)
      throws IOException {
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);
    server.response(HttpStatus.SERVICE_UNAVAILABLE);

    StepVerifier.create(resourceServiceClient.getById(123L))
        .consumeErrorWith(Exceptions::isRetryExhausted)
        .verify();

    server.response(HttpStatus.INTERNAL_SERVER_ERROR);
    server.response(HttpStatus.OK, fileBuffer(),
        Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE));
    StepVerifier.create(resourceServiceClient.getById(124L))
        .thenConsumeWhile(dataBuffer -> dataBuffer != null && dataBuffer.readableByteCount() > 0)
        .verifyComplete();

    server.response(HttpStatus.CREATED, fileBuffer(), Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
    StepVerifier.create(resourceServiceClient.getById(125L))
        .thenConsumeWhile(dataBuffer -> dataBuffer != null && dataBuffer.readableByteCount() > 0)
        .verifyComplete();
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
