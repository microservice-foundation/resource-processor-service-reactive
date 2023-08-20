package com.epam.training.microservicefoundation.resourceprocessor.service.implementation;

import com.epam.training.microservicefoundation.resourceprocessor.common.FileUtils;
import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceType;
import com.epam.training.microservicefoundation.resourceprocessor.service.Convertor;
import java.io.File;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public final class SongFileConverter implements Convertor<Mono<Path>, Flux<DataBuffer>> {
  private static final Logger log = LoggerFactory.getLogger(SongFileConverter.class);
  @Override
  public Mono<Path> covert(Flux<DataBuffer> input) {
    log.info("Converting data buffer '{}' to file", input);
    return FileUtils.writeDataBuffer(input, ResourceType.MP3);
  }
}
