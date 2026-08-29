package com.qualitrace.backend.analysisresult.infrastructure.api;

import com.qualitrace.backend.analysisresult.application.assembler.AnalysisResultModelAssembler;
import com.qualitrace.backend.analysisresult.application.dto.AnalysisResultCreateRequest;
import com.qualitrace.backend.analysisresult.application.dto.AnalysisResultResponse;
import com.qualitrace.backend.analysisresult.application.dto.AnalysisResultUpdateRequest;
import com.qualitrace.backend.analysisresult.application.service.AnalysisResultService;
import com.qualitrace.backend.shared.infrastructure.security.QualitracePrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Infrastructure class that define the deviations API endpoints.
 */
@Tag(name = "Analysis results", description = "Gestion des résultats d'analyses")
@RestController
@RequestMapping("/api/v1/batches/{batchId}/analysis")
public class AnalysisResultController {
    private final AnalysisResultService analysisResultService;
    private final AnalysisResultModelAssembler assembler;

    public AnalysisResultController(
            AnalysisResultService analysisResultService,
            AnalysisResultModelAssembler assembler
    ) {
        this.analysisResultService = analysisResultService;
        this.assembler = assembler;
    }

    /**
     * List all analysis results for a batch.
     *
     * @param batchId The ID of the batch
     * @return The list of all analysis results
     */
    @Operation(
            summary = "Récupérer les résultats d'analyses d'un batch",
            description = "Récupère les résultats d'analyses d'un batch."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des résultats d'analyses du batch"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public CollectionModel<EntityModel<AnalysisResultResponse>> list(@PathVariable Long batchId) {
        List<AnalysisResultResponse> specifications = analysisResultService.getByBatch(batchId);

        return assembler.toCollectionModel(specifications)
                .add(linkTo(methodOn(AnalysisResultController.class).list(batchId)).withSelfRel());
    }

    /**
     * Create a new analysis result.
     *
     * @param batchId The ID of the batch
     * @return The created analysis result
     */
    @Operation(
            summary = "Ajouter un résultat d'analyse",
            description = "Ajoute un résultat d'analyse."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Résultat d'analyse créée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('CQ')")
    public ResponseEntity<EntityModel<AnalysisResultResponse>> create(
            @PathVariable Long batchId,
            @Valid @RequestBody AnalysisResultCreateRequest request,
            @AuthenticationPrincipal QualitracePrincipal principal
    ) {
        AnalysisResultResponse created = analysisResultService.save(batchId, principal, request);
        EntityModel<AnalysisResultResponse> model = assembler.toModel(created);

        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    /**
     * Update an existing analysis result.
     *
     * @param batchId The ID of the batch
     * @param id      The ID of the analysis result
     * @return The updated analysis result
     */
    @Operation(
            summary = "Mettre à jour un résultat d'analyse",
            description = "Met à jour la valeur de résultat d'une analyse."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Résultat d'analyse mise à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Résultat d'analyse introuvable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CQ')")
    public EntityModel<AnalysisResultResponse> update(@PathVariable Long batchId, @PathVariable Long id, @Valid @RequestBody AnalysisResultUpdateRequest request) {
        AnalysisResultResponse updated = analysisResultService.update(id, request);

        return assembler.toModel(updated);
    }
}