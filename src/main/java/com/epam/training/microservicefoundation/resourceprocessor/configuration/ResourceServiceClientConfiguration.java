package com.epam.training.microservicefoundation.resourceprocessor.configuration;

import com.epam.training.microservicefoundation.resourceprocessor.client.ResourceServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.client.SongServiceClient;
import com.epam.training.microservicefoundation.resourceprocessor.domain.ResourceType;
import com.epam.training.microservicefoundation.resourceprocessor.service.FileConvertor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.epam.training.microservicefoundation.resourceprocessor.client.BaseClient.*;

@Configuration
public class ResourceServiceClientConfiguration {

    @Value("${resource-service.endpoint}")
    private String resourceServiceEndpoint;
    @Value("${web-client.connection.timeout}")
    private String connectionTimeout;
    @Value("${web-client.response.timeout}")
    private String responseTimeout;
    @Value("${web-client.read.timeout}")
    private String readTimeout;
    @Value("${web-client.write.timeout}")
    private String writeTimeout;
    @Value("${resource-service.accept.mime-type}")
    private String acceptMimeType;

    private Map<String, String> resourceClientHeader() {
        Map<String, String> header = new HashMap<>();
        header.put(URL, resourceServiceEndpoint);
        header.put(CONNECTION_TIMEOUT, connectionTimeout);
        header.put(RESPONSE_TIMEOUT, responseTimeout);
        header.put(READ_TIMEOUT, readTimeout);
        header.put(WRITE_TIMEOUT, writeTimeout);
        header.put(HttpHeaders.ACCEPT, acceptMimeType);
        return header;
    }

    private FileConvertor fileConvertor() {
        return new FileConvertor(ResourceType.getResourceTypeByMimeType(acceptMimeType));
    }

    @Bean
    public ResourceServiceClient resourceServiceClient(RetryTemplate retryTemplate) {
        return new ResourceServiceClient(resourceClientHeader(), fileConvertor(), retryTemplate);
    }
}
