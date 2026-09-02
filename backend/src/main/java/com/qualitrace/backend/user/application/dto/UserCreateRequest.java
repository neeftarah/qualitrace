package com.qualitrace.backend.user.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qualitrace.backend.user.domain.type.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UserCreateRequest(
    @Schema(description = "Identifiant unique de l'utilisateur", example = "john.doe")
    @NotBlank
    String login,

    @Schema(description = "Mot de passe initial", example = "P@ssw0rd")
    @NotBlank
    String password,

    @Schema(description = "Adresse email", example = "john.doe@example.com")
    @NotBlank
    @Email
    String email,

    @Schema(description = "Prénom", example = "John")
    @NotBlank
    String firstname,

    @Schema(description = "Nom de famille", example = "Doe")
    @NotBlank
    String surname,

    @Schema(description = "Rôles attribués, au moins un requis", example = "[\"AQ\"]")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @NotEmpty
    Set<UserRole> roles
) {}
