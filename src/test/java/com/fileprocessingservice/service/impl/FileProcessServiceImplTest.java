package com.fileprocessingservice.service.impl;

import com.fileprocessingservice.model.dto.UserResponse;
import com.fileprocessingservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"${file.processing.topic}"})
class FileProcessServiceImplTest {

    private final UserResponse userResponse = UserResponse.builder()
            .id(1L)
            .username("test")
            .email("test@test.com")
            .firstName("test1")
            .lastName("test2")
            .createDate(LocalDateTime.now())
            .build();

    private final byte[] fileContent = "test,test@test.com,test1,test2".getBytes();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoSpyBean
    private FileProcessServiceImpl fileProcessService;

    @Test
    public void testProcessFileSuccess() {
        when(fileProcessService.getFileSupportedType()).thenReturn(".csv");

        List<UserResponse> userResponses = Collections.singletonList(userResponse);

        when(userService.saveAll(anyList())).thenReturn(userResponses);

        fileProcessService.processFile(fileContent);

        verify(userService, times(1)).saveAll(anyList());
        assertNotNull(userResponses);
    }

    @Test
    public void testProcessFileNoUsersSaved() {
        when(fileProcessService.getFileSupportedType()).thenReturn(".csv");

        List<UserResponse> userResponses = Collections.emptyList();

        when(userService.saveAll(anyList())).thenReturn(userResponses);

        fileProcessService.processFile(fileContent);

        verify(userService, times(1)).saveAll(anyList());
    }

    @Test
    public void testProcessFileEmptyFile() {
        when(fileProcessService.getFileSupportedType()).thenReturn(".csv");

        byte[] fileContent = new byte[0];

        fileProcessService.processFile(fileContent);

        verify(userService, never()).saveAll(anyList());
    }

    @Test
    public void testProcessFileInvalidExtension() {
        when(fileProcessService.getFileSupportedType()).thenReturn("invalid");

        fileProcessService.processFile(fileContent);

        verify(userService, never()).saveAll(anyList());
    }
}
