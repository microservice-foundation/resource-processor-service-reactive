package com.epam.training.microservicefoundation.resourceprocessor.service.implementation;

import com.epam.training.microservicefoundation.resourceprocessor.client.ResourceServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.client.SongServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.common.FileUtils;
import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceRecord;
import com.epam.training.microservicefoundation.resourceprocessor.model.SongMetadata;
import com.epam.training.microservicefoundation.resourceprocessor.service.Convertor;
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
  private final Convertor<Mono<File>, Flux<DataBuffer>> convertor;

  @Autowired
  public ResourceProcessorService(ResourceServiceClient resourceServiceClient, SongServiceClient songServiceClient,
      Convertor<Mono<File>, Flux<DataBuffer>> songFileConverter) {

    this.resourceServiceClient = resourceServiceClient;
    this.songServiceClient = songServiceClient;
    this.convertor = songFileConverter;
  }


  public Mono<Void> processResource(ResourceRecord resourceRecord) {
    log.info("Processing resource record");
    return convertor.covert(resourceServiceClient.getById(resourceRecord.getId()))
        .zipWhen(file -> processFile(resourceRecord.getId(), file))
        .flatMap(tuple -> songServiceClient.post(tuple.getT2())
            .then(FileUtils.delete(tuple.getT1())));
  }

  private Mono<SongMetadata> processFile(long resourceId, File file) {
    log.info("Processing file '{}' related to resource id '{}'", file.getName(), resourceId);
    try {
      Mp3File mp3File = new Mp3File(file);
      String duration = String.format("%1d:%2d", mp3File.getLengthInSeconds() / 60, mp3File.getLengthInSeconds() % 60);
      SongMetadata.Builder songMetadataBuilder = new SongMetadata.Builder(resourceId, file.getName(), duration);
      if (mp3File.hasId3v1Tag()) {
        songMetadataBuilder
            .name(mp3File.getId3v1Tag().getTitle())
            .artist(mp3File.getId3v1Tag().getArtist())
            .album(mp3File.getId3v1Tag().getAlbum())
            .year(Integer.parseInt(mp3File.getId3v1Tag().getYear()))
            .build();
      } else if (mp3File.hasId3v2Tag()) {
        songMetadataBuilder
            .name(mp3File.getId3v2Tag().getTitle())
            .artist(mp3File.getId3v2Tag().getArtist())
            .album(mp3File.getId3v2Tag().getAlbum())
            .year(Integer.parseInt(mp3File.getId3v2Tag().getYear()))
            .build();
      }

      SongMetadata songMetadata = songMetadataBuilder.build();
      log.debug("File processed successfully: {}", songMetadata);
      return Mono.just(songMetadata);
    } catch (IOException | UnsupportedTagException | InvalidDataException e) {
      return Mono.error(e);
    }
  }
}
