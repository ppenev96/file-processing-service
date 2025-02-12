package com.fileprocessingservice.service;

import com.fileprocessingservice.model.dto.UserRequest;
import com.fileprocessingservice.model.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> saveAll(List<UserRequest> users);

    List<UserResponse> getAllUsers();
}
