package com.fileprocessingservice.controller;

import com.fileprocessingservice.model.dto.FileUploadResponse;
import com.fileprocessingservice.model.enums.Status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"${file.processing.topic}"})
class FileProcessControllerTest {
    @Autowired
    private FileProcessController fileProcessController;

    @MockitoBean
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    private final byte[] fileContent = "test,test@test.com,test1,test2".getBytes();

    @Test
    public void testUploadFileSuccess() throws Exception {
        String fileName = "validFile.csv";
        MultipartFile mockFile = new MockMultipartFile(fileName, fileName, fileName, fileContent);

        when(kafkaTemplate.send(anyString(), any(byte[].class))).thenReturn(mock(CompletableFuture.class));

        ResponseEntity<FileUploadResponse> response = fileProcessController.uploadFile(mockFile);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("File uploaded and processing started.", response.getBody().getMessage());
        assertEquals(Status.UPLOADED, response.getBody().getStatus());
    }

    @Test
    public void testUploadFileInvalidFileSize() throws Exception {
        String fileName = "largeFile.txt";
        byte[] fileContent = new byte[1024 * 1024 * 10];

        MultipartFile mockFile = new MockMultipartFile(fileName, fileContent);

        ResponseEntity<FileUploadResponse> response = fileProcessController.uploadFile(mockFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("File max size"));
        assertEquals(Status.FAILED, response.getBody().getStatus());
    }

    @Test
    public void testUploadFile_invalidFileType() throws Exception {
        String fileName = "invalidFile.pdf";

        MultipartFile mockFile = new MockMultipartFile(fileName, fileContent);

        ResponseEntity<FileUploadResponse> response = fileProcessController.uploadFile(mockFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("File supported type"));
        assertEquals(Status.FAILED, response.getBody().getStatus());
    }

    @Test
    public void testUploadFile_emptyFile() throws Exception {
        String fileName = "emptyFile.txt";
        byte[] fileContent = new byte[0];

        MultipartFile mockFile = new MockMultipartFile(fileName, fileContent);

        ResponseEntity<FileUploadResponse> response = fileProcessController.uploadFile(mockFile);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("File cannot be empty"));
        assertEquals(Status.FAILED, response.getBody().getStatus());
    }
}
