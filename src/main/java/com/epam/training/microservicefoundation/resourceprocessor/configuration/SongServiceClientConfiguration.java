package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import com.epam.training.microservicefoundation.resourceprocessor.client.ResourceServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.client.SongServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.domain.ResourceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.epam.training.microservicefoundation.resourceprocessor.client.BaseClient.*;

@Configuration
public class SongServiceClientConfiguration {

    @Value("${song-service.endpoint}")
    private String songServiceEndpoint;
    @Value("${web-client.connection.timeout}")
    private String connectionTimeout;
    @Value("${web-client.response.timeout}")
    private String responseTimeout;
    @Value("${web-client.read.timeout}")
    private String readTimeout;
    @Value("${web-client.write.timeout}")
    private String writeTimeout;

    private Map<String, String> songServiceClientHeader() {
        Map<String, String> header = new HashMap<>();
        header.put(URL, songServiceEndpoint);
        header.put(CONNECTION_TIMEOUT, connectionTimeout);
        header.put(RESPONSE_TIMEOUT, responseTimeout);
        header.put(READ_TIMEOUT, readTimeout);
        header.put(WRITE_TIMEOUT, writeTimeout);
        header.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return header;
    }

    @Bean
    public SongServiceClient songServiceClient(RetryTemplate retryTemplate) {
        return new SongServiceClient(songServiceClientHeader(), retryTemplate);
    }
}
