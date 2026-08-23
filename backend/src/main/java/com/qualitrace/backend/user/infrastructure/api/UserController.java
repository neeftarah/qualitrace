package com.qualitrace.backend.user.infrastructure.api;

import com.qualitrace.backend.user.application.assembler.UserModelAssembler;
import com.qualitrace.backend.user.application.dto.UserCreateRequest;
import com.qualitrace.backend.user.application.dto.UserResponse;
import com.qualitrace.backend.user.application.dto.UserUpdateRequest;
import com.qualitrace.backend.user.application.service.UserService;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.shared.domain.model.SortQuery;
import com.qualitrace.backend.user.domain.model.UserFilter;
import com.qualitrace.backend.user.domain.type.UserRole;
import com.qualitrace.backend.user.domain.type.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Infrastructure class that define the users API endpoints.
 */
@Tag(name = "Users", description = "Gestion des comptes utilisateurs et de leurs habilitations")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final UserModelAssembler assembler;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "login", "email", "firstname", "surname", "status", "created_at", "updated_at"
    );

    public UserController(UserService userService, UserModelAssembler assembler) {
        this.userService = userService;
        this.assembler = assembler;
    }

    /**
     * List all users.
     *
     * @return The list of all users
     */
    @Operation(
            summary = "Rechercher des utilisateurs",
            description = "Retourne la liste des utilisateurs correspondant aux critères de recherche."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs retournée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public CollectionModel<EntityModel<UserResponse>> list(
            @RequestParam(required = false) String login,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstname,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) UserRole role,
            @ParameterObject @PageableDefault(size = 10, sort = "surname") Pageable pageable,
            PagedResourcesAssembler<UserResponse> pagedAssembler
    ) {
        validateSortFields(pageable.getSort());

        List<SortQuery> sortOrders = pageable.getSort().stream()
                .map(order -> new SortQuery(order.getProperty(),
                        order.isDescending() ? SortQuery.Direction.DESC : SortQuery.Direction.ASC))
                .toList();

        PageQuery pageQuery = new PageQuery(pageable.getPageNumber(), pageable.getPageSize(), sortOrders);
        UserFilter filter = new UserFilter(login, email, firstname, surname, status, role);

        PageResult<UserResponse> result = userService.getAll(pageQuery, filter);

        Page<UserResponse> page = new PageImpl<>(
                result.content(),
                pageable,
                result.totalElements()
        );

        return pagedAssembler.toModel(page, assembler)
                .add(linkTo(methodOn(UserController.class).create(
                        new UserCreateRequest("", "", "", "", "", Collections.emptySet())
                )).withRel("create"));
    }

    /**
     * Get details of a specific user.
     *
     * @param id The UUID of the user
     * @return The details of the user
     */
    @Operation(
            summary = "Consulter un utilisateur",
            description = "Retourne le détail d'un utilisateur par son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public EntityModel<UserResponse> get(@PathVariable UUID id) {
        UserResponse user = userService.getOneById(id);

        return assembler.toModel(user);
    }

    /**
     * Create a new user.
     *
     * @return The created user
     */
    @Operation(
            summary = "Créer un utilisateur",
            description = "Crée un nouvel utilisateur avec au moins un rôle. Le login doit être unique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur créé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide (validation, rôle manquant)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Login déjà utilisé",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<UserResponse>> create(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.save(request);
        EntityModel<UserResponse> model = assembler.toModel(created);

        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    /**
     * Update an existing user.
     *
     * @param id The UUID of the user
     * @return The updated user
     */
    @Operation(
            summary = "Mettre à jour un utilisateur",
            description = "Met à jour les informations d'un utilisateur existant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur mis à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide (validation, rôle manquant)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public EntityModel<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updated = userService.update(id, request);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a user.
     *
     * @param id The UUID of the user
     * @return The updated user
     */
    @Operation(
            summary = "Déverrouiller un utilisateur",
            description = "Débloque un utilisateur verrouillé par le système et active de nouveau la connexion."
    )
    @ApiResponse(responseCode = "200", description = "Utilisateur déverrouillé")
    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<UserResponse> unlock(@PathVariable UUID id) {
        UserResponse updated = userService.unlock(id);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a user.
     *
     * @param id The UUID of the user
     * @return The updated user
     */
    @Operation(
            summary = "Réactiver un utilisateur archivé",
            description = "Permet la connexion. Réversible via /archive."
    )
    @ApiResponse(responseCode = "200", description = "Utilisateur activé")
    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<UserResponse> activate(@PathVariable UUID id) {
        UserResponse updated = userService.reactivate(id);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a user.
     *
     * @param id The UUID of the user
     * @return The updated user
     */
    @Operation(
            summary = "Archiver un utilisateur",
            description = "Rend l'utilisateur indisponible. Réversible via /activate."
    )
    @ApiResponse(responseCode = "200", description = "Utilisateur archivé")
    @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<UserResponse> archive(@PathVariable UUID id) {
        UserResponse updated = userService.archive(id);

        return assembler.toModel(updated);
    }

    /**
     * Validation des critères de tri.
     *
     * @param sort Critères de tri.
     */
    private void validateSortFields(Sort sort) {
        sort.forEach(order -> {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException("Champ de tri non autorisé : " + order.getProperty());
            }
        });
    }
}