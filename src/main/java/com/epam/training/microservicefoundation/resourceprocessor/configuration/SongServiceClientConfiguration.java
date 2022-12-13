package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import com.epam.training.microservicefoundation.resourceprocessor.client.SongServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SongServiceClientConfiguration {

    private Map<String, String> songServiceClientHeader() {
        Map<String, String> header = new HashMap<>();
        header.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return header;
    }

    @Bean
    public SongServiceClient songServiceClient(RetryTemplate retryTemplate, WebClient webClient) {
        return new SongServiceClient(songServiceClientHeader(), retryTemplate, webClient);
    }
}
