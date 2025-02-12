package com.fileprocessingservice.service.impl;

import com.fileprocessingservice.model.dto.UserRequest;
import com.fileprocessingservice.model.dto.UserResponse;
import com.fileprocessingservice.model.entity.User;
import com.fileprocessingservice.repository.UserRepository;
import com.fileprocessingservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponse> saveAll(List<UserRequest> users) {
        return Optional.ofNullable(users).orElse(new ArrayList<>())
                .stream()
                .map(userRequest -> {

                    if (!userRequest.isValid()) {
                        log.error("saveUser :: Invalid user request: {}", userRequest);
                        return null;
                    }

                    if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
                        log.error("saveUser :: Duplicate username: {}", userRequest.getUsername());
                        return null;
                    }

                    if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
                        log.error("saveUser :: Duplicate email: {}", userRequest.getEmail());
                        return null;
                    }

                    User savedUser = userRepository.save(
                            User.builder()
                                    .username(userRequest.getUsername())
                                    .email(userRequest.getEmail())
                                    .firstName(userRequest.getFirstName())
                                    .lastName(userRequest.getLastName())
                                    .createDate(LocalDateTime.now())
                                    .build()
                    );

                    return mapToUserResponse(savedUser);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User savedUser) {
        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .createDate(savedUser.getCreateDate())
                .build();
    }
}
