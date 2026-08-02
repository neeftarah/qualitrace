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
import com.qualitrace.backend.infrastructure.security.LoginAttemptService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
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
        UserCreateRequest hashedRequest = new UserCreateRequest(
                request.login(),
                passwordEncoder.encode(request.password()),
                request.email(),
                request.firstname(),
                request.surname(),
                request.roles()
        );

        User user = userMapper.toDomain(hashedRequest);
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

    public UserResponse unlock(UUID id) {
        User existing = findOrThrow(id);
        User activated = existing.unlock(); // ta méthode dédiée, cf. discussion précédente
        UserResponse response = userMapper.toResponse(userRepository.save(activated));
        loginAttemptService.resetOnUnlock(existing.login());

        return response;
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