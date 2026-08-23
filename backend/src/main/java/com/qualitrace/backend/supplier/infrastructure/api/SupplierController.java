package com.qualitrace.backend.supplier.infrastructure.api;

import com.qualitrace.backend.supplier.application.assembler.SupplierModelAssembler;
import com.qualitrace.backend.supplier.application.dto.SupplierCreateRequest;
import com.qualitrace.backend.supplier.application.dto.SupplierResponse;
import com.qualitrace.backend.supplier.application.dto.SupplierUpdateRequest;
import com.qualitrace.backend.supplier.application.service.SupplierService;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.shared.domain.model.SortQuery;
import com.qualitrace.backend.supplier.domain.model.SupplierFilter;
import com.qualitrace.backend.supplier.domain.type.SupplierStatus;
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
 * Infrastructure class that define the suppliers API endpoints.
 */
@Tag(name = "Suppliers", description = "Gestion des fournisseurs")
@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {
    private final SupplierService supplierService;
    private final SupplierModelAssembler assembler;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "code", "name"
    );

    public SupplierController(SupplierService supplierService, SupplierModelAssembler assembler) {
        this.supplierService = supplierService;
        this.assembler = assembler;
    }

    /**
     * List all suppliers.
     *
     * @return The list of all suppliers
     */
    @Operation(
            summary = "Rechercher des fournisseurs",
            description = "Retourne la liste des fournisseurs correspondant aux critères de recherche."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des fournisseurs retournée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public CollectionModel<EntityModel<SupplierResponse>> list(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) SupplierStatus status,
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable,
            PagedResourcesAssembler<SupplierResponse> pagedAssembler
    ) {
        validateSortFields(pageable.getSort());

        List<SortQuery> sortOrders = pageable.getSort().stream()
                .map(order -> new SortQuery(order.getProperty(),
                        order.isDescending() ? SortQuery.Direction.DESC : SortQuery.Direction.ASC))
                .toList();

        PageQuery pageQuery = new PageQuery(pageable.getPageNumber(), pageable.getPageSize(), sortOrders);
        SupplierFilter filter = new SupplierFilter(code, name, status);

        PageResult<SupplierResponse> result = supplierService.getAll(pageQuery, filter);

        Page<SupplierResponse> page = new PageImpl<>(
                result.content(),
                pageable,
                result.totalElements()
        );

        return pagedAssembler.toModel(page, assembler)
                .add(linkTo(methodOn(SupplierController.class).create(
                        new SupplierCreateRequest("", "", "")
                )).withRel("create"));
    }

    /**
     * Get details of a specific supplier.
     *
     * @param id The ID of the supplier
     * @return The details of the supplier
     */
    @Operation(
            summary = "Consulter un fournisseur",
            description = "Retourne le détail d'un fournisseur par son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fournisseur trouvé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Fournisseur introuvable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public EntityModel<SupplierResponse> get(@PathVariable Long id) {
        SupplierResponse supplier = supplierService.getOneById(id);

        return assembler.toModel(supplier);
    }

    /**
     * Create a new supplier.
     *
     * @return The created supplier
     */
    @Operation(
            summary = "Créer un fournisseur",
            description = "Crée un nouveau fournisseur. Le code et le nom doivent être uniques."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Fournisseur créé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Code déjà utilisé",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Nom déjà utilisé",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<SupplierResponse>> create(@Valid @RequestBody SupplierCreateRequest request) {
        SupplierResponse created = supplierService.save(request);
        EntityModel<SupplierResponse> model = assembler.toModel(created);

        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    /**
     * Update an existing supplier.
     *
     * @param id The ID of the supplier
     * @return The updated supplier
     */
    @Operation(
            summary = "Mettre à jour un fournisseur",
            description = "Met à jour les informations d'un fournisseur existant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fournisseur mis à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Fournisseur introuvable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<SupplierResponse> update(@PathVariable Long id, @Valid @RequestBody SupplierUpdateRequest request) {
        SupplierResponse updated = supplierService.update(id, request);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a supplier.
     *
     * @param id The ID of the supplier
     * @return The updated supplier
     */
    @Operation(
            summary = "Réactiver un fournisseur archivé",
            description = "Permet de réactiver un fournisseur archivé. Réversible via /archive."
    )
    @ApiResponse(responseCode = "200", description = "Fournisseur activé")
    @ApiResponse(responseCode = "404", description = "Fournisseur introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<SupplierResponse> activate(@PathVariable Long id) {
        SupplierResponse updated = supplierService.reactivate(id);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a supplier.
     *
     * @param id The ID of the supplier
     * @return The updated supplier
     */
    @Operation(
            summary = "Archiver un fournisseur",
            description = "Rend le fournisseur indisponible. Réversible via /activate."
    )
    @ApiResponse(responseCode = "200", description = "Fournisseur archivé")
    @ApiResponse(responseCode = "404", description = "Fournisseur introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<SupplierResponse> archive(@PathVariable Long id) {
        SupplierResponse updated = supplierService.archive(id);

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