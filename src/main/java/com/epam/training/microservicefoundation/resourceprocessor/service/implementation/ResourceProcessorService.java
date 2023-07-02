package com.epam.training.microservicefoundation.resourceprocessor.service.implementation;

import com.epam.training.microservicefoundation.resourceprocessor.client.ResourceServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.client.SongServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.common.FileUtils;
import com.epam.training.microservicefoundation.resourceprocessor.kafka.producer.KafkaProducer;
import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceProcessedEvent;
import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceProcessingContext;
import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceStagedEvent;
import com.epam.training.microservicefoundation.resourceprocessor.model.SongMetadata;
import com.epam.training.microservicefoundation.resourceprocessor.service.Convertor;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ResourceProcessorService {
  private static final Logger log = LoggerFactory.getLogger(ResourceProcessorService.class);
  private final ResourceServiceClient resourceServiceClient;
  private final SongServiceClient songServiceClient;
  private final KafkaProducer kafkaProducer;
  private final Convertor<Mono<File>, Flux<DataBuffer>> convertor;

  @Autowired
  public ResourceProcessorService(ResourceServiceClient resourceServiceClient, SongServiceClient songServiceClient,
      KafkaProducer kafkaProducer, Convertor<Mono<File>, Flux<DataBuffer>> songFileConverter) {

    this.resourceServiceClient = resourceServiceClient;
    this.songServiceClient = songServiceClient;
    this.kafkaProducer = kafkaProducer;
    this.convertor = songFileConverter;
  }

  public Mono<Void> processResource(ResourceStagedEvent resourceStagedEvent) {
    log.info("Processing resource record with resource id '{}'", resourceStagedEvent.getId());
    ResourceProcessingContext context = new ResourceProcessingContext().withResourceId(resourceStagedEvent.getId());
    return convertor.covert(resourceServiceClient.getById(context.getResourceId()))
        .map(context::withResourceFile)
        .flatMap(this::processFile)
        .flatMap(this::postSongMetadata)
        .flatMap(this::publishResourceProcessedEvent);
  }

  private Mono<Void> publishResourceProcessedEvent(ResourceProcessingContext context) {
    log.info("Publishing resource processed event for resource id '{}'", context.getResourceId());
    return kafkaProducer.publish(new ResourceProcessedEvent(context.getResourceId())).then();
  }

  private Mono<ResourceProcessingContext> postSongMetadata(ResourceProcessingContext context) {
    log.info("Sending song metadata via song service client: {}", context.getSongMetadata());
    return songServiceClient.post(context.getSongMetadata()).map(result -> context);
  }

  private Mono<ResourceProcessingContext> processFile(ResourceProcessingContext context) {
    log.info("Processing file '{}' related to resource id '{}'", context.getResourceFile().getName(), context.getResourceId());
    try {
      ResourceProcessingContext ctx = buildSongMetadata(context);
      return FileUtils.delete(ctx.getResourceFile()).thenReturn(ctx);
    } catch (IOException | UnsupportedTagException | InvalidDataException e) {
      log.error("Processing file '{}' with resource id '{}' failed",context.getResourceFile().getName(), context.getResourceId(), e);
      return Mono.error(e);
    }
  }

  private ResourceProcessingContext buildSongMetadata(ResourceProcessingContext context)
      throws InvalidDataException, UnsupportedTagException, IOException {
    Mp3File mp3File = new Mp3File(context.getResourceFile());
    String duration = String.format("%1d:%2d", mp3File.getLengthInSeconds() / 60, mp3File.getLengthInSeconds() % 60);
    SongMetadata.SongMetadataBuilder builder = SongMetadata.builder().resourceId(context.getResourceId()).length(duration);
    ID3v1 tag = mp3File.hasId3v1Tag() ? mp3File.getId3v1Tag() : mp3File.getId3v2Tag();
    if (tag != null) {
      builder
          .name(tag.getTitle())
          .artist(tag.getArtist())
          .album(tag.getAlbum())
          .year(Integer.parseInt(tag.getYear()));
    }
    return context.withSongMetadata(builder.build());
  }
}
