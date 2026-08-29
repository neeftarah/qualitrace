package com.qualitrace.backend.batch.infrastructure.api;

import com.qualitrace.backend.batch.application.assembler.BatchModelAssembler;
import com.qualitrace.backend.batch.application.dto.BatchCreateRequest;
import com.qualitrace.backend.batch.application.dto.BatchResponse;
import com.qualitrace.backend.batch.application.dto.BatchValidationRequest;
import com.qualitrace.backend.batch.application.service.BatchService;
import com.qualitrace.backend.batch.domain.model.BatchFilter;
import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.shared.domain.model.SortQuery;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Infrastructure class that define the batchs API endpoints.
 */
@Tag(name = "Batches", description = "Gestion des lots")
@RestController
@RequestMapping("/api/v1/batches")
public class BatchController {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "code",
            "name",
            "internalReferenceNumber",
            "supplierId",
            "supplierReferenceNumber",
            "expiryDate",
            "receptionDate",
            "status",
            "validatedBy",
            "validationDate"
    );
    private final BatchService batchService;
    private final BatchModelAssembler assembler;

    public BatchController(BatchService batchService, BatchModelAssembler assembler) {
        this.batchService = batchService;
        this.assembler = assembler;
    }

    /**
     * List all batchs.
     *
     * @return The list of all batchs
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
    public CollectionModel<EntityModel<BatchResponse>> list(
            @RequestParam(required = false) String internalReferenceNumber,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String supplierReferenceNumber,
            @RequestParam(required = false) LocalDate expiryFromDate,
            @RequestParam(required = false) LocalDate expiryToDate,
            @RequestParam(required = false) LocalDate receptionFromDate,
            @RequestParam(required = false) LocalDate receptionToDate,
            @RequestParam(required = false) BatchStatus status,
            @RequestParam(required = false) UUID validatedBy,
            @RequestParam(required = false) LocalDate validationFromDate,
            @RequestParam(required = false) LocalDate validationToDate,
            @ParameterObject @PageableDefault(size = 10, sort = "receptionDate") Pageable pageable,
            PagedResourcesAssembler<BatchResponse> pagedAssembler
    ) {
        validateSortFields(pageable.getSort());

        List<SortQuery> sortOrders = pageable.getSort().stream()
                .map(order -> new SortQuery(order.getProperty(),
                        order.isDescending() ? SortQuery.Direction.DESC : SortQuery.Direction.ASC))
                .toList();

        PageQuery pageQuery = new PageQuery(pageable.getPageNumber(), pageable.getPageSize(), sortOrders);
        BatchFilter filter = new BatchFilter(
                internalReferenceNumber,
                supplierId,
                supplierReferenceNumber,
                expiryFromDate,
                expiryToDate,
                receptionFromDate,
                receptionToDate,
                status,
                validatedBy,
                validationFromDate,
                validationToDate
        );

        PageResult<BatchResponse> result = batchService.getAll(pageQuery, filter);

        Page<BatchResponse> page = new PageImpl<>(
                result.content(),
                pageable,
                result.totalElements()
        );

        return pagedAssembler.toModel(page, assembler)
                .add(linkTo(methodOn(BatchController.class).create(null)).withRel("create"));
    }

    /**
     * Get details of a specific batch.
     *
     * @param id The ID of the batch
     * @return The details of the batch
     */
    @Operation(
            summary = "Consulter un lot",
            description = "Retourne le détail d'un lot par son identifiant."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lot trouvé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Lot introuvable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public EntityModel<BatchResponse> get(@PathVariable Long id) {
        BatchResponse batch = batchService.getOneById(id);

        return assembler.toModel(batch);
    }

    /**
     * Create a new batch.
     *
     * @return The created batch
     */
    @Operation(
            summary = "Créer un lot",
            description = "Crée un nouveau lot."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lot créé"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Référence déjà utilisée",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPPLY')")
    public ResponseEntity<EntityModel<BatchResponse>> create(@Valid @RequestBody BatchCreateRequest request) {
        BatchResponse created = batchService.save(request);
        EntityModel<BatchResponse> model = assembler.toModel(created);

        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    /**
     * Change the status of a batch.
     *
     * @param id The ID of the batch
     * @return The updated batch
     */
    @Operation(
            summary = "Valide ou refuse un lot en quarantaine",
            description = "Permet de valider un lot en quarantaine."
    )
    @ApiResponse(responseCode = "200", description = "Lot validé")
    @ApiResponse(responseCode = "404", description = "Lot introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @PatchMapping("/{id}/validate")
    @PreAuthorize("hasRole('AQ')")
    public EntityModel<BatchResponse> validate(@PathVariable Long id, @Valid @RequestBody BatchValidationRequest request) {
        BatchResponse updated = batchService.validate(id, request);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a batch.
     *
     * @param id The ID of the batch
     * @return The updated batch
     */
    @Operation(
            summary = "Utilise un lot en reçu",
            description = "Permet de déclarer un lot en reçu comme utilisé."
    )
    @ApiResponse(responseCode = "200", description = "Lot utilisé")
    @ApiResponse(responseCode = "404", description = "Lot introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @PatchMapping("/{id}/use")
    @PreAuthorize("hasRole('PROD')")
    public EntityModel<BatchResponse> use(@PathVariable Long id) {
        BatchResponse updated = batchService.use(id);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a batch.
     *
     * @param id The ID of the batch
     * @return The updated batch
     */
    @Operation(
            summary = "Détruit un lot refusé ou périmé",
            description = "Permet de détruire un lot refusé ou périmé."
    )
    @ApiResponse(responseCode = "200", description = "Lot détruit")
    @ApiResponse(responseCode = "404", description = "Lot introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @PatchMapping("/{id}/destroy")
    @PreAuthorize("hasRole('SUPPLY')")
    public EntityModel<BatchResponse> destroy(@PathVariable Long id) {
        BatchResponse updated = batchService.destroy(id);

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
