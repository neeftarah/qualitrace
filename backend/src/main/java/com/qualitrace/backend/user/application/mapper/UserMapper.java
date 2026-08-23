package com.qualitrace.backend.user.application.mapper;

import com.qualitrace.backend.user.application.dto.UserCreateRequest;
import com.qualitrace.backend.user.application.dto.UserResponse;
import com.qualitrace.backend.user.domain.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
            user.id(),
            user.login(),
            user.email(),
            user.firstname(),
            user.surname(),
            user.status(),
            user.roles(),
            user.createdAt(),
            user.updatedAt()
        );
    }

    public User toDomain(UserCreateRequest request) {
        return User.createNew(
            request.login(),
            request.password(),
            request.email(),
            request.firstname(),
            request.surname(),
            request.roles()
        );
    }
}