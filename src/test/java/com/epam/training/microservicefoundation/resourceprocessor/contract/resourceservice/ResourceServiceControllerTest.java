//package com.epam.training.microservicefoundation.resourceprocessor.contract.resourceservice;
//
//import com.epam.training.microservicefoundation.resourceprocessor.client.ResourceServiceClient;
//import com.epam.training.microservicefoundation.resourceprocessor.configuration.ClientConfiguration;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
//import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
//import org.springframework.core.io.buffer.DataBuffer;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import reactor.core.publisher.Flux;
//import reactor.test.StepVerifier;
//
//@ExtendWith(SpringExtension.class)
//@AutoConfigureStubRunner(stubsMode = StubRunnerProperties.StubsMode.LOCAL,
//        ids = "com.epam.training:resource-service-reactive-r2dbc:+:stubs:1001")
//@ContextConfiguration(classes = ClientConfiguration.class)
//@TestPropertySource(locations = "classpath:application.properties")
//@DirtiesContext
//class ResourceServiceControllerTest {
//
//    @Autowired
//    private ResourceServiceClient resourceServiceClient;
//
//    @BeforeAll
//    static void initialize() {
//        System.setProperty("resource-service.endpoint", "http://localhost:1001/api/v1");
//    }
//
//    @Test
//    void shouldGetResourceById() {
//      Flux<DataBuffer> dataBuffer = resourceServiceClient.getById(123L);
//      StepVerifier.create(dataBuffer)
//          .assertNext(Assertions::assertNotNull)
//          .verifyComplete();
//    }
//
//    @Test
//    void shouldReturnEmptyWhenGetNonexistentResourceById() {
//        Flux<DataBuffer> dataBufferFlux = resourceServiceClient.getById(1999L);
//        StepVerifier.create(dataBufferFlux)
//            .expectNextCount(0)
//            .verifyComplete();
//    }
//}
