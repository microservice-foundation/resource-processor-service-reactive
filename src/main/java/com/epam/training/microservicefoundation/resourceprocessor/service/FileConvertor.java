package com.epam.training.microservicefoundation.resourceprocessor.service;

import com.epam.training.microservicefoundation.resourceprocessor.common.FileUtils;
import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
public class FileConvertor implements Convertor<File, Flux<DataBuffer>> {
    private static final Logger log = LoggerFactory.getLogger(FileConvertor.class);
    private final ResourceType resourceType;
    public FileConvertor(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public File covert(Flux<DataBuffer> input) {
        log.info("Converting data buffer '{}' to file", input);
        return FileUtils.writeDataBuffer(input, resourceType).block();
    }
}
