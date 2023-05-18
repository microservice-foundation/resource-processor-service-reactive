package com.epam.training.microservicefoundation.resourceprocessor.common;

import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public final class FileUtils {
  private static final Logger log = LoggerFactory.getLogger(FileUtils.class);
  private static final String TARGET_DIRECTORY = "src/main/resources";
  private static final String TEMP_DIRECTORY = "temp";

  private FileUtils() {
  }

  public static Mono<File> writeDataBuffer(Flux<DataBuffer> dataBuffer, ResourceType resourceType) {
    log.info("Writing data buffer '{}' to '{}'", dataBuffer, TARGET_DIRECTORY + TEMP_DIRECTORY);
    Path tempDirectoryPath = Paths.get(TARGET_DIRECTORY, TEMP_DIRECTORY);
    if (!Files.exists(tempDirectoryPath)) {
      createTempDirectory(tempDirectoryPath);
    }
    Path filePath = tempDirectoryPath.resolve(System.currentTimeMillis() + resourceType.getExtension());
    return DataBufferUtils.write(dataBuffer, filePath, StandardOpenOption.CREATE).thenReturn(filePath.toFile());
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

  public static Mono<Void> delete(File file) {
    log.info("Deleting file '{}'", file);
    try {
      return Mono.just(Files.deleteIfExists(file.toPath())).then();
    } catch (NoSuchFileException exception) {
      log.error("File '{}' does not exit", file.toPath(), exception);
      return Mono.error(exception);
    } catch (DirectoryNotEmptyException exception) {
      log.error("Directory '{}' is not empty ", file.toPath(), exception);
      return Mono.error(exception);
    } catch (IOException exception) {
      log.error("File permission might be caught by user", exception);
      return Mono.error(exception);
    }
  }
}
