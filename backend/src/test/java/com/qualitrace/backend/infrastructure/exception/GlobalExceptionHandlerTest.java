package com.qualitrace.backend.infrastructure.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {
    private ExceptionHandlerExceptionResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ExceptionHandlerExceptionResolver();
        resolver.setApplicationContext(new StaticApplicationContext() {{
            registerSingleton("globalExceptionHandler", GlobalExceptionHandler.class);
            refresh();
        }});
        resolver.setMessageConverters(List.of(
                new JacksonJsonHttpMessageConverter() // ou l'équivalent Jackson 3 si Spring 7 l'a renommé
        ));
        resolver.afterPropertiesSet();
    }

    @Test
    void shouldHandleNoHandlerFoundException() throws UnsupportedEncodingException, JsonProcessingException {
        NoHandlerFoundException exception = new NoHandlerFoundException(
                "GET", "/api/v1/unknown-route", new HttpHeaders()
        );
        Integer code = HttpStatus.NOT_FOUND.value();

        JsonNode json = globalTest(exception, code);
        assertThat(json.get("status").asInt()).isEqualTo(code);
        assertThat(json.get("type").asText()).isEqualTo("urn:qualitrace:errors:ressource-not-found");
        assertThat(json.get("title").asText()).isEqualTo("Resource Not Found");
        assertThat(json.get("instance").asText()).isEqualTo("/api/v1/unknown-route");
        assertThat(json.get("detail").asText()).isEqualTo("No endpoint GET /api/v1/unknown-route.");
        assertThat(json.get("timestamp").asText()).isNotEmpty();
    }

    @Test
    void shouldHandleNoResourceFoundException() throws UnsupportedEncodingException, JsonProcessingException {
        NoResourceFoundException exception = new NoResourceFoundException(
                HttpMethod.GET,
                "/api/v1/unknown",
                "/api/v1/unknown"
        );
        Integer code = HttpStatus.NOT_FOUND.value();

        JsonNode json = globalTest(exception, code);
        assertThat(json.get("status").asInt()).isEqualTo(code);
        assertThat(json.get("type").asText()).isEqualTo("urn:qualitrace:errors:ressource-not-found");
        assertThat(json.get("title").asText()).isEqualTo("Resource Not Found");
        assertThat(json.get("instance").asText()).isEqualTo("/api/v1/unknown-route");
        assertThat(json.get("detail").asText()).isEqualTo("No static resource /api/v1/unknown for request '/api/v1/unknown'.");
        assertThat(json.get("timestamp").asText()).isNotEmpty();
    }

    @Test
    void shouldHandleNoSuchElementException() throws UnsupportedEncodingException, JsonProcessingException {
        NoSuchElementException exception = new NoSuchElementException("No such element!");
        Integer code = HttpStatus.NOT_FOUND.value();

        JsonNode json = globalTest(exception, code);
        assertThat(json.get("status").asInt()).isEqualTo(code);
        assertThat(json.get("type").asText()).isEqualTo("urn:qualitrace:errors:ressource-not-found");
        assertThat(json.get("title").asText()).isEqualTo("Resource Not Found");
        assertThat(json.get("instance").asText()).isEqualTo("/api/v1/unknown-route");
        assertThat(json.get("detail").asText()).isEqualTo("No such element!");
        assertThat(json.get("timestamp").asText()).isNotEmpty();
    }

    @Test
    void shouldHandleHttpRequestMethodNotSupportedException() throws UnsupportedEncodingException, JsonProcessingException {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException(
                "GET",
                List.of("POST", "PUT")
        );
        Integer code = HttpStatus.METHOD_NOT_ALLOWED.value();

        JsonNode json = globalTest(exception, code);
        assertThat(json.get("status").asInt()).isEqualTo(code);
        assertThat(json.get("type").asText()).isEqualTo("urn:qualitrace:errors:http-method-not-allowed");
        assertThat(json.get("title").asText()).isEqualTo("HTTP Method Not Allowed");
        assertThat(json.get("instance").asText()).isEqualTo("/api/v1/unknown-route");
        assertThat(json.get("detail").asText()).isEqualTo("The GET method is not supported for this resource");
        assertThat(json.get("timestamp").asText()).isNotEmpty();
        assertThat(json.get("allowedMethods").get(0).asText()).isEqualTo("POST");
        assertThat(json.get("allowedMethods").get(1).asText()).isEqualTo("PUT");
    }

    @Test
    void shouldHandleIllegalStateException() throws UnsupportedEncodingException, JsonProcessingException {
        IllegalStateException exception = new IllegalStateException("Invalid status transition: ACTIVE -> ACTIVE");
        Integer code = HttpStatus.CONFLICT.value();

        JsonNode json = globalTest(exception, code);
        assertThat(json.get("status").asInt()).isEqualTo(code);
        assertThat(json.get("type").asText()).isEqualTo("urn:qualitrace:errors:illegal-state");
        assertThat(json.get("title").asText()).isEqualTo("Illegal State");
        assertThat(json.get("instance").asText()).isEqualTo("/api/v1/unknown-route");
        assertThat(json.get("detail").asText()).isEqualTo("Invalid status transition: ACTIVE -> ACTIVE");
        assertThat(json.get("timestamp").asText()).isNotEmpty();
    }

    @Test
    void shouldHandleIllegalArgumentException() throws UnsupportedEncodingException, JsonProcessingException {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");
        Integer code = HttpStatus.BAD_REQUEST.value();

        JsonNode json = globalTest(exception, code);
        assertThat(json.get("status").asInt()).isEqualTo(code);
        assertThat(json.get("type").asText()).isEqualTo("urn:qualitrace:errors:invalid-argument");
        assertThat(json.get("title").asText()).isEqualTo("Invalid Argument");
        assertThat(json.get("instance").asText()).isEqualTo("/api/v1/unknown-route");
        assertThat(json.get("detail").asText()).isEqualTo("Invalid argument provided");
        assertThat(json.get("timestamp").asText()).isNotEmpty();
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() throws UnsupportedEncodingException, JsonProcessingException, NoSuchMethodException {
        // 1. Récupération d'une vraie méthode Java (n'importe laquelle, par exemple "toString") pour l'associer au paramètre
        Method dummyMethod = Object.class.getMethod("toString");
        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.getExecutable()).thenReturn(dummyMethod);
        when(parameter.getParameterIndex()).thenReturn(-1); // Requis pour éviter d'autres NPE internes

        // 2. Création du BindingResult
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError(
                "userCreateRequest",
                "email",
                null,                // rejectedValue
                false,               // bindingFailure
                new String[]{"NotBlank"}, // codes
                null,                // arguments
                "ne doit pas être vide" // defaultMessage (ce que tu veux tester)
        ));

        // 3. Instanciation de l'exception
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);
        Integer code = HttpStatus.BAD_REQUEST.value();

        JsonNode json = globalTest(exception, code);
        assertThat(json.get("status").asInt()).isEqualTo(code);
        assertThat(json.get("type").asText()).isEqualTo("urn:qualitrace:errors:validation-failed");
        assertThat(json.get("title").asText()).isEqualTo("Invalid Request");
        assertThat(json.get("instance").asText()).isEqualTo("/api/v1/unknown-route");
        assertThat(json.get("detail").asText()).isEqualTo("Validation failed");
        assertThat(json.get("timestamp").asText()).isNotEmpty();

        assertThat(json.get("errors").isObject()).isTrue();
        assertThat(json.get("errors").has("email")).isTrue();
        assertThat(json.get("errors").get("email").asText()).isEqualTo("ne doit pas être vide");
    }

    @Test
    void shouldHandleHttpMessageNotReadableException() throws UnsupportedEncodingException, JsonProcessingException {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Invalid message provided",
                mock(HttpInputMessage.class)
        );
        Integer code = HttpStatus.BAD_REQUEST.value();

        JsonNode json = globalTest(exception, code);
        assertThat(json.get("status").asInt()).isEqualTo(code);
        assertThat(json.get("type").asText()).isEqualTo("urn:qualitrace:errors:malformed-request-body");
        assertThat(json.get("title").asText()).isEqualTo("Malformed Request Body");
        assertThat(json.get("instance").asText()).isEqualTo("/api/v1/unknown-route");
        assertThat(json.get("detail").asText()).isEqualTo("Corps de requête invalide");
        assertThat(json.get("timestamp").asText()).isNotEmpty();
    }

    @Test
    void shouldHandleStandardException() throws UnsupportedEncodingException, JsonProcessingException {
        Exception exception = new Exception("Invalid message provided");
        Integer code = HttpStatus.INTERNAL_SERVER_ERROR.value();

        JsonNode json = globalTest(exception, code);
        assertThat(json.get("status").asInt()).isEqualTo(code);
        assertThat(json.get("type").asText()).isEqualTo("urn:qualitrace:errors:internal-server-error");
        assertThat(json.get("title").asText()).isEqualTo("Internal Server Error");
        assertThat(json.get("instance").asText()).isEqualTo("/api/v1/unknown-route");
        assertThat(json.get("detail").asText()).isEqualTo("Une erreur inattendue est survenue");
        assertThat(json.get("timestamp").asText()).isNotEmpty();
    }

    private JsonNode globalTest(Exception exception, Integer code) throws UnsupportedEncodingException, JsonProcessingException {
        // Initialize request, response and exception
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/unknown-route");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // handle the exception
        ModelAndView result = resolver.resolveException(request, response, null, exception);
        assertThat(result).isNotNull();

        ObjectMapper mapper = new ObjectMapper();
        String content = response.getContentAsString();
        assertThatCode(() -> mapper.readTree(content))
                .as("La réponse doit être un JSON valide")
                .doesNotThrowAnyException();

        // Assert
        assertThat(response.getStatus()).isEqualTo(code);
        assertThat(response.getContentAsString()).contains("/api/v1/unknown-route");
        assertThat(response.getContentType()).isEqualTo("application/problem+json");

        return mapper.readTree(content);
    }
}
