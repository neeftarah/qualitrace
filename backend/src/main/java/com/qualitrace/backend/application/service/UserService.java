package com.qualitrace.backend.application.service;

import com.qualitrace.backend.application.dto.UserCreateRequest;
import com.qualitrace.backend.application.dto.UserResponse;
import com.qualitrace.backend.application.dto.UserUpdateRequest;
import com.qualitrace.backend.application.mapper.UserMapper;
import com.qualitrace.backend.domain.exception.UserNotFoundException;
import com.qualitrace.backend.domain.model.PageQuery;
import com.qualitrace.backend.domain.model.PageResult;
import com.qualitrace.backend.domain.model.User;
import com.qualitrace.backend.domain.model.UserFilter;
import com.qualitrace.backend.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

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
            .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponse(user);
    }

    public PageResult<UserResponse> getAll(PageQuery pageQuery, UserFilter filter) {
        return userRepository.findAll(pageQuery, filter)
                .map(userMapper::toResponse);
    }

    public UserResponse save(UserCreateRequest request) {
        User user = userMapper.toDomain(request);
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    public UserResponse update(UUID id, UserUpdateRequest request) {
        User existing = findOrThrow(id);
        User updated = existing.update(
            request.firstname(),
            request.surname(),
            request.roles()
        );

        return userMapper.toResponse(userRepository.save(updated));
    }

    public UserResponse lock(UUID id) {
        User existing = findOrThrow(id);
        return userMapper.toResponse(userRepository.save(existing.lock()));
    }

    public UserResponse unlock(UUID id) {
        User existing = findOrThrow(id);
        return userMapper.toResponse(userRepository.save(existing.unlock()));
    }

    public UserResponse archive(UUID id) {
        User existing = findOrThrow(id);
        return userMapper.toResponse(userRepository.save(existing.archive()));
    }

    public UserResponse reactivate(UUID id) {
        User existing = findOrThrow(id);
        return userMapper.toResponse(userRepository.save(existing.reactivate()));
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}