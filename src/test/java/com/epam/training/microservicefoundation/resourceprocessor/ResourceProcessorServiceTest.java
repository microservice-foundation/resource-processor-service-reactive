package com.epam.training.microservicefoundation.resourceprocessor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.epam.training.microservicefoundation.resourceprocessor.client.ResourceServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.client.SongServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceRecord;
import com.epam.training.microservicefoundation.resourceprocessor.model.SongMetadata;
import com.epam.training.microservicefoundation.resourceprocessor.model.SongRecord;
import com.epam.training.microservicefoundation.resourceprocessor.service.Convertor;
import com.epam.training.microservicefoundation.resourceprocessor.service.implementation.ResourceProcessorService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ResourceProcessorServiceTest {
  @InjectMocks
  private ResourceProcessorService service;
  @Mock
  private ResourceServiceClient resourceServiceClient;
  @Mock
  private SongServiceClient songServiceClient;
  @Mock
  private Convertor<Mono<File>, Flux<DataBuffer>> convertor;

  @Test
  void shouldProcessResource() throws IOException {
    long resourceRecordId = 1L;
    when(resourceServiceClient.getById(resourceRecordId)).thenReturn(Flux.just(new DefaultDataBufferFactory().wrap(new byte[100])));
    when(convertor.covert(any())).thenReturn(Mono.just(testFile()));
    when(songServiceClient.post(any(SongMetadata.class))).thenReturn(Mono.just(new SongRecord(1L)));

    StepVerifier.create(service.processResource(new ResourceRecord(1L)))
        .expectSubscription()
        .verifyComplete();
  }

  @Test
  void shouldReturnEmptyResourceWhenProcessResource() {
    long resourceRecordId = 1L;
    when(resourceServiceClient.getById(resourceRecordId)).thenReturn(Flux.empty());
    when(convertor.covert(Flux.empty())).thenReturn(Mono.empty());

    StepVerifier.create(service.processResource(new ResourceRecord(1L)))
        .expectNextCount(0)
        .expectComplete()
        .verify();
  }

  @Test
  void shouldReturnEmptySongRecordIdWhenProcessResource() throws IOException {
    long resourceRecordId = 1L;
    when(resourceServiceClient.getById(resourceRecordId)).thenReturn(Flux.just(new DefaultDataBufferFactory().wrap(new byte[100])));
    when(convertor.covert(any())).thenReturn(Mono.just(testFile()));
    when(songServiceClient.post(any(SongMetadata.class))).thenReturn(Mono.empty());

    StepVerifier.create(service.processResource(new ResourceRecord(1L)))
        .expectNextCount(0)
        .expectComplete()
        .verify();
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