package com.qualitrace.backend.infrastructure.api;

import com.qualitrace.backend.application.assembler.ControlRangeSpecificationModelAssembler;
import com.qualitrace.backend.application.dto.ControlRangeSpecificationCreateRequest;
import com.qualitrace.backend.application.dto.ControlRangeSpecificationResponse;
import com.qualitrace.backend.application.dto.ControlRangeSpecificationUpdateRequest;
import com.qualitrace.backend.application.service.ControlRangeSpecificationService;
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
 * Infrastructure class that define the controlRangeSpecifications API endpoints.
 */
@Tag(name = "ControlRangeSpecifications", description = "Gestion des gammes de contrôles")
@RestController
@RequestMapping("/api/v1/components/{componentId}/controls")
public class ControlRangeSpecificationController {
    private final ControlRangeSpecificationService controlRangeSpecificationService;
    private final ControlRangeSpecificationModelAssembler assembler;

    public ControlRangeSpecificationController(
            ControlRangeSpecificationService controlRangeSpecificationService,
            ControlRangeSpecificationModelAssembler assembler
    ) {
        this.controlRangeSpecificationService = controlRangeSpecificationService;
        this.assembler = assembler;
    }

    /**
     * List all controlRangeSpecifications.
     *
     * @param componentId The ID of the component
     * @return The list of all controlRangeSpecifications
     */
    @Operation(
            summary = "Rechercher des gammes de contrôle",
            description = "Retourne la liste des gammes de contrôle correspondant aux critères de recherche."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des gammes de contrôle retournée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public CollectionModel<EntityModel<ControlRangeSpecificationResponse>> list(@PathVariable Long componentId) {
        List<ControlRangeSpecificationResponse> specifications = controlRangeSpecificationService.getByComponent(componentId);

        return assembler.toCollectionModel(specifications)
                .add(linkTo(methodOn(ControlRangeSpecificationController.class).list(componentId)).withSelfRel());
    }

    /**
     * Create a new controlRangeSpecification.
     *
     * @param componentId The ID of the component
     * @return The created controlRangeSpecification
     */
    @Operation(
            summary = "Créer une gamme de contrôle",
            description = "Crée une nouvelle gamme de contrôle. Le code et le nom doivent être uniques."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Gamme de contrôle créée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Référence déjà utilisée",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Nom déjà utilisé",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ControlRangeSpecificationResponse>> create(@PathVariable Long componentId, @Valid @RequestBody ControlRangeSpecificationCreateRequest request) {
        ControlRangeSpecificationResponse created = controlRangeSpecificationService.save(componentId, request);
        EntityModel<ControlRangeSpecificationResponse> model = assembler.toModel(created);

        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    /**
     * Update an existing controlRangeSpecification.
     *
     * @param componentId The ID of the component
     * @param id          The ID of the controlRangeSpecification
     * @return The updated controlRangeSpecification
     */
    @Operation(
            summary = "Mettre à jour une gamme de contrôle",
            description = "Met à jour les informations d'une gamme de contrôle existante."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gamme de contrôle mise à jour"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Gamme de contrôle introuvable",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<ControlRangeSpecificationResponse> update(@PathVariable Long componentId, @PathVariable Long id, @Valid @RequestBody ControlRangeSpecificationUpdateRequest request) {
        ControlRangeSpecificationResponse updated = controlRangeSpecificationService.update(componentId, id, request);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a controlRangeSpecification to DELETED.
     *
     * @param componentId The ID of the component
     * @param id          The ID of the controlRangeSpecification
     * @return The updated controlRangeSpecification
     */
    @Operation(
            summary = "Archiver une gamme de contrôle",
            description = "Rend la gamme de contrôle indisponible. Non réversible."
    )
    @ApiResponse(responseCode = "204", description = "Gamme de contrôle archivée")
    @ApiResponse(responseCode = "404", description = "Gamme de contrôle introuvable")
    @ApiResponse(responseCode = "409", description = "Transition invalide depuis le statut actuel")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long componentId, @PathVariable Long id) {
        controlRangeSpecificationService.delete(componentId, id);
        return ResponseEntity.noContent().build();
    }
}