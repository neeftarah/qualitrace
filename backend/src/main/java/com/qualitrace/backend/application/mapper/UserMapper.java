package com.qualitrace.backend.application.mapper;

import com.qualitrace.backend.application.dto.UserCreateRequest;
import com.qualitrace.backend.application.dto.UserResponse;
import com.qualitrace.backend.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getLogin(),
            user.getEmail(),
            user.getFirstname(),
            user.getSurname(),
            user.getStatus(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    public User toDomain(UserCreateRequest request) {
        return User.createNew(
            request.login(),
            request.password(),
            request.email(),
            request.firstname(),
            request.surname()
        );
    }
}