//package com.epam.training.microservicefoundation.resourceprocessor.contract.songservice;
//
//import com.epam.training.microservicefoundation.resourceprocessor.client.SongServiceClient;
//import com.epam.training.microservicefoundation.resourceprocessor.configuration.ClientConfiguration;
//import com.epam.training.microservicefoundation.resourceprocessor.model.SongMetadata;
//import com.epam.training.microservicefoundation.resourceprocessor.model.SongRecord;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
//import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//@ExtendWith(SpringExtension.class)
//@AutoConfigureStubRunner(stubsMode = StubRunnerProperties.StubsMode.LOCAL,
//    ids = "com.epam.training:song-service-reactive-r2dbc:+:stubs:1001")
//@ContextConfiguration(classes = ClientConfiguration.class)
//@TestPropertySource(locations = "classpath:application.properties")
//@DirtiesContext
//class SongServiceControllerTest {
//
//  @Autowired
//  private SongServiceClient songServiceClient;
//
//  @BeforeAll
//  static void initialize() {
//    System.setProperty("song-service.endpoint", "http://localhost:1001/api/v1");
//  }
//
//  @Test
//  void shouldSaveSongMetadata() {
//    SongMetadata songMetadata = new SongMetadata.Builder(1L, "New office", "03:22")
//        .artist("John Kennedy")
//        .album("ASU")
//        .year(1999).build();
//    Mono<SongRecord> songRecordMono = songServiceClient.post(songMetadata);
//
//    StepVerifier.create(songRecordMono)
//        .assertNext(songRecord -> {
//          Assertions.assertEquals(199L, songRecord.getId());
//        })
//        .verifyComplete();
//  }
//
//  @Test
//  void shouldReturnBadRequestWhenSaveSongMetadata() {
//    SongMetadata songMetadata = new SongMetadata.Builder(1L, "New office", "03:22")
//        .artist("John Kennedy")
//        .album("ASU")
//        .year(2099).build();
//    Mono<SongRecord> songRecordMono = songServiceClient.post(songMetadata);
//
//    StepVerifier.create(songRecordMono)
//        .expectNextCount(0)
//        .expectComplete()
//        .verify();
//  }
//}
