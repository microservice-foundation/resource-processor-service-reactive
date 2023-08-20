package com.epam.training.microservicefoundation.resourceprocessor.common;

import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceType;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class FileUtils {
  private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
  private static final String RESOURCE_DIRECTORY = "src/main/resources";
  private static final Path TEMP_DIRECTORY_PATH = Path.of(RESOURCE_DIRECTORY, "temp");

  private FileUtils() { }

  public static Mono<Path> writeDataBuffer(Flux<DataBuffer> dataBuffer, ResourceType resourceType) {
    log.info("Writing data buffer to '{}'", TEMP_DIRECTORY_PATH);
    if (!Files.exists(TEMP_DIRECTORY_PATH)) {
      createTempDirectory(TEMP_DIRECTORY_PATH);
    }
    final Path filePath = TEMP_DIRECTORY_PATH.resolve(System.currentTimeMillis() + resourceType.getExtension());
    return DataBufferUtils.write(dataBuffer, filePath, StandardOpenOption.CREATE).thenReturn(filePath);
  }

  private static void createTempDirectory(Path path) {
    log.info("Creating temp directory '{}'", path);
    try {
      Files.createDirectories(path).toFile().deleteOnExit();
    } catch (IOException e) {
      log.error("File permission might be caught by user", e);
      throw new RuntimeException(e);
    }
  }

  public static Mono<Void> delete(Path filePath) {
    log.info("Deleting file '{}'", filePath);
    try {
      return Mono.just(Files.deleteIfExists(filePath)).then();
    } catch (NoSuchFileException exception) {
      log.error("File '{}' does not exit", filePath, exception);
      return Mono.error(exception);
    } catch (DirectoryNotEmptyException exception) {
      log.error("Directory '{}' is not empty ", filePath, exception);
      return Mono.error(exception);
    } catch (IOException exception) {
      log.error("File permission might be caught by user", exception);
      return Mono.error(exception);
    }
  }
}
