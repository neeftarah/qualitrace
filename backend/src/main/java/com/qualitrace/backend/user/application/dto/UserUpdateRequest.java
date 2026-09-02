package com.qualitrace.backend.user.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qualitrace.backend.user.domain.type.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UserUpdateRequest(
        @Schema(description = "Prénom", example = "John")
        String firstname,

        @Schema(description = "Nom de famille", example = "Doe")
        String surname,

        @Schema(description = "Rôles attribués, au moins un requis", example = "[\"AQ\"]")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        @NotEmpty
        Set<UserRole> roles
) {}
