package com.qualitrace.backend.infrastructure.api;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure class that define the users API endpoints.
 */
@RestController
public class UserController {

    /**
     * List all users.
     *
     * @return The list of all users
     */
    @GetMapping("/api/v1/users")
    public Map<String, String> list() {
        return Map.of("controller", "UserController", "method", "list");
    }

    /**
     * A sublist of all users matching the search criteria.
     * The search criteria are passed in the request body as a JSON object.
     *
     * @return The list of matching users
     */
    @PostMapping("/api/v1/users/search")
    public Map<String, String> search() {
        return Map.of("controller", "UserController", "method", "search");
    }

    /**
     * Create a new user.
     *
     * @return The created user
     */
    @PostMapping("/api/v1/users")
    public Map<String, String> create() {
        return Map.of("controller", "UserController", "method", "create");
    }

    /**
     * Get details of a specific user.
     *
     * @param id The ID of the user
     *
     * @return The details of the user
     */
    @GetMapping("/api/v1/users/{id}")
    public Map<String, String> get(@PathVariable UUID id) {
        return Map.of("controller", "UserController", "method", "get", "id", id.toString());
    }

    /**
     * Update an existing user.
     *
     * @param id The ID of the user
     * @return The updated user
     */
    @PutMapping("/api/v1/users/{id}")
    public Map<String, String> update(@PathVariable UUID id) {
        return Map.of("controller", "UserController", "method", "update", "id", id.toString());
    }

    /**
     * Archive an active user.
     *
     * @param id The ID of the user
     *
     * @return The result of the request
     */
    @DeleteMapping("/api/v1/users/{id}")
    public Map<String, String> delete(@PathVariable UUID id) {
        return Map.of("controller", "UserController", "method", "delete", "id", id.toString());
    }
}