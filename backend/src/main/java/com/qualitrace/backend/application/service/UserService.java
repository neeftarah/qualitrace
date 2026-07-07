package com.qualitrace.backend.application.service;

import com.qualitrace.backend.application.dto.UserCreateRequest;
import com.qualitrace.backend.application.dto.UserResponse;
import com.qualitrace.backend.application.mapper.UserMapper;
import com.qualitrace.backend.domain.model.User;
import com.qualitrace.backend.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserResponse getOneById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + id));
        return userMapper.toResponse(user);
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse save(UserCreateRequest request) {
        User user = userMapper.toDomain(request);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }
}