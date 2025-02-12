package com.fileprocessingservice.model.dto;

import com.fileprocessingservice.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileUploadResponse {
    private String fileName;
    private String message;
    private Status status;
}
