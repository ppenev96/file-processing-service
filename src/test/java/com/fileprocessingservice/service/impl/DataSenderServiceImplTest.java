package com.fileprocessingservice.service.impl;

import com.fileprocessingservice.model.dto.UserResponse;
import com.fileprocessingservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"${file.processing.topic}"})
class DataSenderServiceImplTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WebClient webClient;

    @Autowired
    private DataSenderServiceImpl dataSenderService;

    private final UserResponse validUser = UserResponse.builder()
            .username("john_doe")
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .build();

    @Test
    void testSendDataSendsDataWhenUsersExist() {

        when(userService.getAllUsers()).thenReturn(Collections.singletonList(validUser));

        WebClient.RequestBodyUriSpec requestURIBodySpec = mock(WebClient.RequestBodyUriSpec.class);
        when(webClient.post()).thenReturn(requestURIBodySpec);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestURIBodySpec.uri(any(String.class))).thenReturn(requestURIBodySpec);

        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(requestURIBodySpec.body(any(), eq(UserResponse.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("Success"));

        dataSenderService.sendData();

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testSendDataSkipsSendingWhenNoUsersExist() {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        dataSenderService.sendData();

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testSendWithRetrySuccess() {
        WebClient.RequestBodyUriSpec requestURIBodySpec = mock(WebClient.RequestBodyUriSpec.class);
        when(webClient.post()).thenReturn(requestURIBodySpec);

        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(requestURIBodySpec.uri(any(String.class))).thenReturn(requestURIBodySpec);

        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        when(requestURIBodySpec.body(any(), eq(UserResponse.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("Success"));

        dataSenderService.sendWithRetry(validUser);

        verify(webClient, times(1)).post();
    }
}
