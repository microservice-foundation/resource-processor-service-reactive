package com.epam.training.microservicefoundation.resourceprocessor.service;

import com.epam.training.microservicefoundation.resourceprocessor.client.ResourceServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.client.SongServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.common.FileUtils;
import com.epam.training.microservicefoundation.resourceprocessor.model.ResourceRecord;
import com.epam.training.microservicefoundation.resourceprocessor.model.SongMetadata;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ResourceProcessorService {
    private static final Logger log = LoggerFactory.getLogger(ResourceProcessorService.class);
    private final Validator<ResourceRecord> resourceRecordValidator;
    private final ResourceServiceClient resourceServiceClient;
    private final SongServiceClient songServiceClient;

    @Autowired
    public ResourceProcessorService(Validator<ResourceRecord> resourceRecordValidator,
                                    ResourceServiceClient resourceServiceClient,
                                    SongServiceClient songServiceClient) {
        this.resourceRecordValidator = resourceRecordValidator;
        this.resourceServiceClient = resourceServiceClient;
        this.songServiceClient = songServiceClient;
    }


    public boolean processResource(ResourceRecord resourceRecord) {
        log.info("Processing resource record: {}", resourceRecord);

        if(!resourceRecordValidator.validate(resourceRecord)) {
            IllegalArgumentException ex = new IllegalArgumentException(String.format("Resource record '%s' " +
                    "was not validated, check your required parameters", resourceRecord));

            log.error("Resource record '{}' was not valid to parse metadata\nreason:", resourceRecord, ex);
            throw ex;
        }

        Optional<File> fileOptional = resourceServiceClient.getById(resourceRecord.getId());
        boolean isProcessed = fileOptional.map(file -> {
            try {
                return processFile(resourceRecord.getId(), file);
            } catch (InvalidDataException | UnsupportedTagException | IOException ex) {
                log.error("MP3 file processing failed for resource '{}' ", resourceRecord, ex);
                return null;
            }
        })
        .map(songServiceClient::post)
        .isPresent();

        fileOptional.ifPresent(FileUtils::delete);
        return isProcessed;
    }

    private SongMetadata processFile(long resourceId, File file) throws InvalidDataException, UnsupportedTagException, IOException {
        log.info("Processing file '{}' related to resource id '{}'", file.getName(), resourceId);
        Mp3File mp3File = new Mp3File(file);
        String duration = String.format("%1d:%2d", mp3File.getLengthInSeconds() / 60, mp3File.getLengthInSeconds() % 60);
        SongMetadata.Builder songMetadataBuilder = new SongMetadata.Builder(resourceId, file.getName(), duration);
        if(mp3File.hasId3v1Tag()) {
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
        return songMetadata;
    }
}
