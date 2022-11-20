package com.epam.training.microservicefoundation.resourceprocessor.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BaseClient {
    public static final String URL = "url";
    public static final String CONNECTION_TIMEOUT = "connection.timeout";
    public static final String RESPONSE_TIMEOUT = "response.timeout";
    public static final String READ_TIMEOUT = "read.timeout";
    public static final String WRITE_TIMEOUT="write.timeout";
    private final WebClient client;

    BaseClient(Map<String, String> headers) {
        HttpClient httpClient = HttpClient.create()
                .baseUrl(headers.get(URL))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Integer.parseInt(headers.get(CONNECTION_TIMEOUT)))
                .responseTimeout(Duration.ofMillis(Long.parseLong(headers.get(RESPONSE_TIMEOUT))))
                .doOnConnected(connection ->
                        connection.addHandlerFirst(new ReadTimeoutHandler(Long.parseLong(headers.get(READ_TIMEOUT)),
                                        TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(Long.parseLong(headers.get(WRITE_TIMEOUT)),
                                        TimeUnit.MILLISECONDS)));

        this.client = WebClient.builder()
                .baseUrl(headers.get(URL))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    protected WebClient.RequestHeadersSpec<?> get(URI path) {
        return client.get().uri(path);
    }

    protected WebClient.RequestBodySpec post(URI path) {
        return client.post().uri(path);
    }
}
