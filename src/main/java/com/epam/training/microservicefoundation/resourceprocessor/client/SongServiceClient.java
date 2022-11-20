package com.epam.training.microservicefoundation.resourceprocessor.client;

import com.epam.training.microservicefoundation.resourceprocessor.domain.SongRecord;
import com.epam.training.microservicefoundation.resourceprocessor.domain.SongRecordId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public class SongServiceClient extends BaseClient {
    private static final Logger log = LoggerFactory.getLogger(SongServiceClient.class);
    private static final String SONGS = "/songs";
    private final String acceptHeader;
    private final RetryTemplate retryTemplate;
    public SongServiceClient(Map<String, String> headers, RetryTemplate retryTemplate) {
        super(headers);
        acceptHeader = headers.get(HttpHeaders.ACCEPT);
        this.retryTemplate = retryTemplate;
    }

    public SongRecordId post(SongRecord songRecord) {
        return retryTemplate.execute(
                context ->
                        post(UriComponentsBuilder.newInstance().path(SONGS).build().toUri())
                            .accept(MediaType.valueOf(acceptHeader))
                            .bodyValue(songRecord)
                            .retrieve()
                            .bodyToMono(SongRecordId.class)
                            .block(),
                context -> {
                    log.error("Sending post request to song service failed with '{}' retry attempts",
                            context.getRetryCount());
                    return null;
                });
    }

}
