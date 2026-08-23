package com.qualitrace.backend.component.infrastructure.api;

import com.qualitrace.backend.component.application.assembler.ComponentModelAssembler;
import com.qualitrace.backend.component.application.dto.ComponentCreateRequest;
import com.qualitrace.backend.component.application.dto.ComponentResponse;
import com.qualitrace.backend.component.application.dto.ComponentUpdateRequest;
import com.qualitrace.backend.component.application.service.ComponentService;
import com.qualitrace.backend.component.domain.model.ComponentFilter;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.shared.domain.model.SortQuery;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.component.domain.type.ComponentType;
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

import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Infrastructure class that define the components API endpoints.
 */
@Tag(name = "Components", description = "Gestion des composants")
@RestController
@RequestMapping("/api/v1/components")
public class ComponentController {
    private final ComponentService componentService;
    private final ComponentModelAssembler assembler;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "code", "name"
    );

    public ComponentController(ComponentService componentService, ComponentModelAssembler assembler) {
        this.componentService = componentService;
        this.assembler = assembler;
    }

    /**
     * List all components.
     *
     * @return The list of all components
     */
    @Operation(
            summary = "Rechercher des composants",
            description = "Retourne la liste des composants correspondant aux critères de recherche."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des composants retournée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public CollectionModel<EntityModel<ComponentResponse>> list(
            @RequestParam(required = false) ComponentType type,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ComponentStatus status,
            @RequestParam(required = false) Long supplierId,
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable,
            PagedResourcesAssembler<ComponentResponse> pagedAssembler
    ) {
        validateSortFields(pageable.getSort());

        List<SortQuery> sortOrders = pageable.getSort().stream()
                .map(order -> new SortQuery(order.getProperty(),
                        order.isDescending() ? SortQuery.Direction.DESC : SortQuery.Direction.ASC))
                .toList();

        PageQuery pageQuery = new PageQuery(pageable.getPageNumber(), pageable.getPageSize(), sortOrders);
        ComponentFilter filter = new ComponentFilter(type, reference, name, status, supplierId);

        PageResult<ComponentResponse> result = componentService.getAll(pageQuery, filter);

        Page<ComponentResponse> page = new PageImpl<>(
                result.content(),
                pageable,
                result.totalElements()
        );

        return pagedAssembler.toModel(page, assembler)
                .add(linkTo(methodOn(ComponentController.class).create(null)).withRel("create"));
    }

    /**
     * Get details of a specific component.
     *
     * @param id The ID of the component
     * @return The details of the component
     */
    @Operation(
            summary = "Consulter un composant",
            description = "Retourne le détail d'un composant par son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Composant trouvé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Composant introuvable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public EntityModel<ComponentResponse> get(@PathVariable Long id) {
        ComponentResponse component = componentService.getOneById(id);

        return assembler.toModel(component);
    }

    /**
     * Create a new component.
     *
     * @return The created component
     */
    @Operation(
            summary = "Créer un composant",
            description = "Crée un nouveau composant. Le code et le nom doivent être uniques."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Composant créé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Référence déjà utilisée",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Nom déjà utilisé",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ComponentResponse>> create(@Valid @RequestBody ComponentCreateRequest request) {
        ComponentResponse created = componentService.save(request);
        EntityModel<ComponentResponse> model = assembler.toModel(created);

        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    /**
     * Update an existing component.
     *
     * @param id The ID of the component
     * @return The updated component
     */
    @Operation(
            summary = "Mettre à jour un composant",
            description = "Met à jour les informations d'un composant existant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Composant mis à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Composant introuvable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<ComponentResponse> update(@PathVariable Long id, @Valid @RequestBody ComponentUpdateRequest request) {
        ComponentResponse updated = componentService.update(id, request);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a component.
     *
     * @param id The ID of the component
     * @return The updated component
     */
    @Operation(
            summary = "Réactiver un composant désactivé",
            description = "Permet de réactiver un composant désactivé. Réversible via /disable."
    )
    @ApiResponse(responseCode = "200", description = "Composant activé")
    @ApiResponse(responseCode = "404", description = "Composant introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<ComponentResponse> activate(@PathVariable Long id) {
        ComponentResponse updated = componentService.activate(id);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a component.
     *
     * @param id The ID of the component
     * @return The updated component
     */
    @Operation(
            summary = "Archiver un composant",
            description = "Rend le composant indisponible. Réversible via /activate."
    )
    @ApiResponse(responseCode = "200", description = "Composant archivé")
    @ApiResponse(responseCode = "404", description = "Composant introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<ComponentResponse> archive(@PathVariable Long id) {
        ComponentResponse updated = componentService.archive(id);

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