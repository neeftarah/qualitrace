package com.qualitrace.backend.infrastructure.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Gère spécifiquement les 404 (si configuré)
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class, NoSuchElementException.class})
    public ProblemDetail handleNotFound(Exception ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("urn:qualitrace:errors:ressource-not-found"));
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://api.qualitrace.com/errors/not-found"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleInvalidTransition(IllegalStateException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create("urn:qualitrace:errors:invalid-transition"));
        problem.setTitle("Invalid State Transition");
        problem.setType(URI.create("https://api.qualitrace.com/errors/invalid-transition"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("urn:qualitrace:errors:invalid-argument"));
        problem.setTitle("Invalid Argument");
        problem.setType(URI.create("https://api.qualitrace.com/errors/invalid-argument"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setType(URI.create("urn:qualitrace:errors:validation-failed"));
        problem.setTitle("Invalid Request");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errors", errors); // extension custom RFC 7807

        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String detail = "Corps de requête invalide";

        // Cas spécifique : valeur d'enum invalide → message plus parlant
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife && ife.getTargetType() != null && ife.getTargetType().isEnum()) {
            String fieldName = ife.getPath().isEmpty()
                    ? ""
                    : " pour le champ '" + ife.getPath().getLast().getPropertyName() + "'";

            Object[] validValues = ife.getTargetType().getEnumConstants();
            detail = "Valeur invalide '%s'%s. Valeurs acceptées : %s"
                    .formatted(ife.getValue(), fieldName, Arrays.toString(validValues));
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setType(URI.create("urn:qualitrace:errors:malformed-request-body"));
        problem.setTitle("Malformed Request Body");
        problem.setInstance(URI.create(request.getRequestURI()));

        return problem;
    }

    // Gère toutes les autres exceptions non prévues (500)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(Exception ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur inattendue est survenue"
        );
        problem.setType(URI.create("urn:qualitrace:errors:server-error"));
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.qualitrace.com/errors/internal-server-error"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());

        // Enregistrement de l'erreur dans les logs
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);

        return problem;
    }
}