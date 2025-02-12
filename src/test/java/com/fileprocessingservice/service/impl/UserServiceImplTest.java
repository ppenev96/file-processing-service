package com.fileprocessingservice.service.impl;

import com.fileprocessingservice.model.dto.UserRequest;
import com.fileprocessingservice.model.dto.UserResponse;
import com.fileprocessingservice.model.entity.User;
import com.fileprocessingservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unused")
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"${file.processing.topic}"})
class UserServiceImplTest {
    @MockitoSpyBean
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    private final UserRequest validUserRequest = UserRequest.builder()
            .username("john_doe")
            .email("john.doe@example.com")
            .firstName("John")
            .lastName("Doe")
            .build();
    private final UserRequest invalidUserRequest = UserRequest.builder()
            .username("")
            .email("")
            .firstName("Invalid")
            .lastName("User")
            .build();
    private final UserResponse userResponse = UserResponse.builder()
            .id(1L)
            .username("test")
            .email("test@test.com")
            .firstName("test1")
            .lastName("test2")
            .createDate(LocalDateTime.now())
            .build();

    @BeforeEach
    public void clearRepository() {
        userRepository.deleteAll();
    }

    @Test
    void testSaveAllWithValidUser() {

        User savedUser = User.builder()
                .id(1L)
                .username(validUserRequest.getUsername())
                .email(validUserRequest.getEmail())
                .firstName(validUserRequest.getFirstName())
                .lastName(validUserRequest.getLastName())
                .createDate(LocalDateTime.now())
                .build();

        List<UserResponse> responses = userService.saveAll(List.of(validUserRequest));

        assertNotNull(responses);
        assertEquals(1, responses.size());
        UserResponse response = responses.get(0);
        assertEquals(validUserRequest.getUsername(), response.getUsername());
        assertEquals(validUserRequest.getEmail(), response.getEmail());
    }

    @Test
    void testSaveAllWithInvalidUser() {
        userRepository.save(User.builder()
                .username(validUserRequest.getUsername())
                .email(validUserRequest.getEmail())
                .firstName(validUserRequest.getFirstName())
                .lastName(validUserRequest.getLastName())
                .createDate(LocalDateTime.now())
                .build());

        List<UserResponse> responses = userService.saveAll(List.of(invalidUserRequest));

        assertTrue(responses.isEmpty());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSaveAllWithDuplicateUsername() {
        userRepository.save(User.builder()
                .username(validUserRequest.getUsername())
                .email(validUserRequest.getEmail())
                .firstName(validUserRequest.getFirstName())
                .lastName(validUserRequest.getLastName())
                .createDate(LocalDateTime.now())
                .build());

        List<UserResponse> responses = userService.saveAll(List.of(validUserRequest));

        assertTrue(responses.isEmpty());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSaveAllWithDuplicateEmail() {
        userRepository.save(User.builder()
                .username("otherUsername")
                .email(validUserRequest.getEmail())
                .firstName(validUserRequest.getFirstName())
                .lastName(validUserRequest.getLastName())
                .createDate(LocalDateTime.now())
                .build());

        List<UserResponse> responses = userService.saveAll(List.of(validUserRequest));

        assertTrue(responses.isEmpty());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetAllUsers() {

        userRepository.save(User.builder()
                .username(validUserRequest.getUsername())
                .email(validUserRequest.getEmail())
                .firstName(validUserRequest.getFirstName())
                .lastName(validUserRequest.getLastName())
                .createDate(LocalDateTime.now())
                .build());

        List<UserResponse> responses = userService.getAllUsers();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        UserResponse response = responses.get(0);
        assertEquals(validUserRequest.getUsername(), response.getUsername());
        assertEquals(validUserRequest.getEmail(), response.getEmail());
    }
}
