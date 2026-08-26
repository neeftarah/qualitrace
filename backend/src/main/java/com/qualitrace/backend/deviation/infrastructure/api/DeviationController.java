package com.qualitrace.backend.deviation.infrastructure.api;

import com.qualitrace.backend.component.application.dto.ComponentResponse;
import com.qualitrace.backend.deviation.application.assembler.DeviationModelAssembler;
import com.qualitrace.backend.deviation.application.dto.DeviationCreateRequest;
import com.qualitrace.backend.deviation.application.dto.DeviationResponse;
import com.qualitrace.backend.deviation.application.dto.DeviationUpdateRequest;
import com.qualitrace.backend.deviation.application.service.DeviationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Infrastructure class that define the deviations API endpoints.
 */
@Tag(name = "Deviations", description = "Gestion des déviations")
@RestController
@RequestMapping("/api/v1/batches/{batchId}/deviations")
public class DeviationController {
    private final DeviationService deviationService;
    private final DeviationModelAssembler assembler;

    public DeviationController(
            DeviationService deviationService,
            DeviationModelAssembler assembler
    ) {
        this.deviationService = deviationService;
        this.assembler = assembler;
    }

    /**
     * List all deviations.
     *
     * @param batchId The ID of the batch
     * @return The list of all deviations
     */
    @Operation(
            summary = "Récupérer les déviations d'un batch",
            description = "Récupère les déviations d'un batch."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des déviations du batch"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public CollectionModel<EntityModel<DeviationResponse>> list(@PathVariable Long batchId) {
        List<DeviationResponse> specifications = deviationService.getByBatch(batchId);

        return assembler.toCollectionModel(specifications)
                .add(linkTo(methodOn(DeviationController.class).list(batchId)).withSelfRel());
    }

    /**
     * Create a new deviation.
     *
     * @param batchId The ID of the batch
     * @return The created deviation
     */
    @Operation(
            summary = "Créer une déviation",
            description = "Crée une nouvelle déviation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Déviation créée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('AQ')")
    public ResponseEntity<EntityModel<DeviationResponse>> create(@PathVariable Long batchId, @Valid @RequestBody DeviationCreateRequest request) {
        DeviationResponse created = deviationService.save(batchId, request);
        EntityModel<DeviationResponse> model = assembler.toModel(created);

        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    /**
     * Update an existing deviation.
     *
     * @param batchId The ID of the batch
     * @param id          The ID of the deviation
     * @return The updated deviation
     */
    @Operation(
            summary = "Mettre à jour une déviation",
            description = "Met à jour les informations d'une déviation existante."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Déviation mise à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Déviation introuvable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AQ')")
    public EntityModel<DeviationResponse> update(@PathVariable Long batchId, @PathVariable Long id, @Valid @RequestBody DeviationUpdateRequest request) {
        DeviationResponse updated = deviationService.update(id, request);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a deviation to CLOSE.
     *
     * @param batchId The ID of the batch
     * @param id          The ID of the deviation
     * @return The updated deviation
     */
    @Operation(
            summary = "Clôturer une déviation",
            description = "Clôturer une déviation. Réversible via /open."
    )
    @ApiResponse(responseCode = "204", description = "Déviation close")
    @ApiResponse(responseCode = "404", description = "Déviation introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<DeviationResponse> close(@PathVariable Long batchId, @PathVariable Long id) {
        DeviationResponse updated = deviationService.close(id);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a deviation to CLOSE.
     *
     * @param batchId The ID of the batch
     * @param id          The ID of the deviation
     * @return The updated deviation
     */
    @Operation(
            summary = "Ouvre une déviation",
            description = "Ouvre une déviation. Réversible via /close."
    )
    @ApiResponse(responseCode = "204", description = "Déviation ouverte")
    @ApiResponse(responseCode = "404", description = "Déviation introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<DeviationResponse> open(@PathVariable Long batchId, @PathVariable Long id) {
        DeviationResponse updated = deviationService.open(id);

        return assembler.toModel(updated);
    }
}