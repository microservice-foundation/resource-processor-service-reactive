package com.epam.training.microservicefoundation.resourceprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ResourceProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResourceProcessorApplication.class, args);
    }
}
