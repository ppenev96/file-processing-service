package com.fileprocessingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class FileProcessingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileProcessingServiceApplication.class, args);
    }

}
