package com.fileprocessingservice.controller;

import com.fileprocessingservice.model.dto.FileUploadResponse;
import com.fileprocessingservice.model.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Log4j2
public class FileProcessController {
    @Value("${file.supported.type}")
    private String fileSupportedType;
    @Value("${file.supported.size}")
    private Long fileSupportedSize;
    @Value("${file.processing.topic}")
    private String defaultFileProcessingTopicName;

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {

        if (isInvalidFile(file)) {
            return ResponseEntity.badRequest().body(
                    new FileUploadResponse(
                            file.getOriginalFilename(),
                            MessageFormatter
                                    .format("File uploaded failed. File max size is {}. File supported type is {}. File cannot be empty!",
                                            fileSupportedSize,
                                            fileSupportedType).getMessage(),
                            Status.FAILED)
            );
        }

        kafkaTemplate.send(defaultFileProcessingTopicName, file.getBytes());

        return ResponseEntity.accepted()
                .body(
                        new FileUploadResponse(
                                file.getOriginalFilename(),
                                "File uploaded and processing started.",
                                Status.UPLOADED));
    }

    private boolean isInvalidFile(MultipartFile file) {
        return file == null ||
                file.isEmpty() ||
                file.getSize() > fileSupportedSize ||
                !Objects.requireNonNull(file.getOriginalFilename()).endsWith(fileSupportedType);
    }
}
