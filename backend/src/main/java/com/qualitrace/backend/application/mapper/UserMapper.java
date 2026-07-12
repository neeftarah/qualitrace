package com.qualitrace.backend.application.mapper;

import com.qualitrace.backend.application.dto.UserCreateRequest;
import com.qualitrace.backend.application.dto.UserResponse;
import com.qualitrace.backend.domain.model.User;
import org.springframework.stereotype.Component;

@Component
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