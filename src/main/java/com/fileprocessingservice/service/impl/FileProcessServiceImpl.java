package com.fileprocessingservice.service.impl;

import com.fileprocessingservice.model.dto.UserRequest;
import com.fileprocessingservice.model.dto.UserResponse;
import com.fileprocessingservice.model.enums.Extension;
import com.fileprocessingservice.service.FileProcessService;
import com.fileprocessingservice.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class FileProcessServiceImpl implements FileProcessService {
    @Value("${file.supported.type}")
    @Getter
    private String fileSupportedType;

    private final UserService userService;

    @SneakyThrows
    @Override
    @KafkaListener(
            topics = "${file.processing.topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void processFile(byte[] file) {
        log.info("processFile :: Processing file");

        Extension extension = Extension.valueOfExtension(getFileSupportedType());

        if (extension != null && file.length > 0) {
            List<UserRequest> userRequests = extension.parseUserFile(file);
            List<UserResponse> userResponses = userService.saveAll(userRequests);
            if (!userResponses.isEmpty()) {
                log.info("processFile :: Saved users");
            } else {
                log.info("processFile :: No users or failed to save users");
            }
        }

    }
}
