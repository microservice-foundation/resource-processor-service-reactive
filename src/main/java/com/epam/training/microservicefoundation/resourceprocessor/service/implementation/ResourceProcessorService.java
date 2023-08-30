package com.epam.training.microservicefoundation.resourceprocessor.service.implementation;

import com.epam.training.microservicefoundation.resourceprocessor.client.ResourceServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.client.SongServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.common.FileUtils;
import com.epam.training.microservicefoundation.resourceprocessor.kafka.producer.KafkaProducer;
import com.epam.training.microservicefoundation.resourceprocessor.model.context.ResourceProcessingContext;
import com.epam.training.microservicefoundation.resourceprocessor.model.dto.SaveSongDTO;
import com.epam.training.microservicefoundation.resourceprocessor.model.event.ResourceProcessedEvent;
import com.epam.training.microservicefoundation.resourceprocessor.model.event.ResourceStagedEvent;
import com.epam.training.microservicefoundation.resourceprocessor.service.Convertor;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.IOException;
import java.nio.file.Path;
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
  private final Convertor<Mono<Path>, Flux<DataBuffer>> convertor;

  @Autowired
  public ResourceProcessorService(ResourceServiceClient resourceServiceClient, SongServiceClient songServiceClient,
      KafkaProducer kafkaProducer, Convertor<Mono<Path>, Flux<DataBuffer>> songFileConverter) {

    this.resourceServiceClient = resourceServiceClient;
    this.songServiceClient = songServiceClient;
    this.kafkaProducer = kafkaProducer;
    this.convertor = songFileConverter;
  }

  public Mono<Void> processResource(final ResourceStagedEvent resourceStagedEvent) {
    log.info("Processing resource record with resource id '{}'", resourceStagedEvent.getId());
    final ResourceProcessingContext context = new ResourceProcessingContext().withResourceId(resourceStagedEvent.getId());
    return convertor.covert(resourceServiceClient.getById(context.getResourceId()))
        .map(context::withResourceFilePath)
        .flatMap(this::processFile)
        .flatMap(this::postSongMetadata)
        .flatMap(this::publishResourceProcessedEvent)
        .then();
  }

  private Mono<ResourceProcessingContext> publishResourceProcessedEvent(final ResourceProcessingContext context) {
    return kafkaProducer.publish(new ResourceProcessedEvent(context.getResourceId())).thenReturn(context);
  }

  private Mono<ResourceProcessingContext> postSongMetadata(final ResourceProcessingContext context) {
    return songServiceClient.post(context.getSaveSongDTO()).map(context::withGetSongDTO);
  }

  private Mono<ResourceProcessingContext> processFile(final ResourceProcessingContext context) {
    try {
      final ResourceProcessingContext resourceProcessingContext = buildSongMetadata(context);
      return FileUtils.delete(resourceProcessingContext.getResourceFilePath()).thenReturn(resourceProcessingContext);
    } catch (IOException | UnsupportedTagException | InvalidDataException e) {
      log.error("Processing file '{}' with resource id '{}' failed", context.getResourceFilePath(), context.getResourceId(), e);
      return Mono.error(e);
    }
  }

  private ResourceProcessingContext buildSongMetadata(final ResourceProcessingContext context)
      throws InvalidDataException, UnsupportedTagException, IOException {
    final Mp3File mp3File = new Mp3File(context.getResourceFilePath());
    final SaveSongDTO.SaveSongDTOBuilder saveSongDTOBuilder =
        SaveSongDTO.builder().resourceId(context.getResourceId()).lengthInSeconds(mp3File.getLengthInSeconds());
    final ID3v1 tag = mp3File.hasId3v1Tag() ? mp3File.getId3v1Tag() : mp3File.getId3v2Tag();
    if (tag != null) {
      saveSongDTOBuilder
          .name(tag.getTitle())
          .artist(tag.getArtist())
          .album(tag.getAlbum())
          .year(Integer.parseInt(tag.getYear()));
    }
    return context.withSaveSongDTO(saveSongDTOBuilder.build());
  }
}
