package com.epam.training.microservicefoundation.resourceprocessor.web.client;

import com.epam.training.microservicefoundation.resourceprocessor.configuration.ClientConfiguration;
import com.epam.training.microservicefoundation.resourceprocessor.configuration.properties.WebClientProperties;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.config.client.RetryProperties;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockServerExtension.class})
@ContextConfiguration(classes = ClientConfiguration.class)
@TestPropertySource(locations = "classpath:application.properties")
public abstract class BaseClientTest {
}
