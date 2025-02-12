package com.fileprocessingservice.service.impl;

import com.fileprocessingservice.model.dto.UserResponse;
import com.fileprocessingservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class DataSenderServiceImpl {
    private final UserService userService;
    private final WebClient webClient;

    @Value("${file.time-restrictions.start}")
    private String restrictedStartTime;

    @Value("${file.time-restrictions.end}")
    private String restrictedEndTime;

    @Value("${external.service.receive-users-url}")
    private String externalServiceReceiveUsersUrl;

    @Scheduled(fixedDelay = 60000)
    public void sendData() {
        if (isRestrictedTime()) {
            log.info("sendData :: Skipping data sending due to restricted time.");
            return;
        }

        List<UserResponse> users = userService.getAllUsers();
        if (users.isEmpty()) {
            log.info("sendData :: Skipping data sending, no users found.");
            return;
        }

        users.forEach(this::sendWithRetry);
    }

    private boolean isRestrictedTime() {

        LocalTime now = LocalTime.now();

        LocalTime start = LocalTime.parse(restrictedStartTime);
        LocalTime end = LocalTime.parse(restrictedEndTime);

        if (start.isAfter(end)) {
            return now.isAfter(start) || now.isBefore(end);
        } else {
            return now.isAfter(start) && now.isBefore(end);
        }
    }

    @Retryable(retryFor = RestClientException.class, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendWithRetry(UserResponse user) {
        try {
            String targetResponse = webClient
                    .post()
                    .uri(externalServiceReceiveUsersUrl)
                    .body(Mono.just(user), UserResponse.class)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RestClientException("Client error")))
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RestClientException("Server error")))
                    .bodyToMono(String.class)
                    .block();
            log.info("User {} sent successfully. Response: {}", user.getUsername(), targetResponse);
        } catch (RestClientException e) {
            log.error("Failed to send user {}: {}", user.getUsername(), e.getMessage());
            throw e;
        }
    }
}
