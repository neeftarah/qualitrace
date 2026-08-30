package com.qualitrace.backend.specification.infrastructure.api;

import com.qualitrace.backend.specification.application.assembler.SpecificationModelAssembler;
import com.qualitrace.backend.specification.application.dto.SpecificationCreateRequest;
import com.qualitrace.backend.specification.application.dto.SpecificationResponse;
import com.qualitrace.backend.specification.application.dto.SpecificationUpdateRequest;
import com.qualitrace.backend.specification.application.service.SpecificationService;
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
 * Infrastructure class that define the specifications API endpoints.
 */
@Tag(name = "Specifications", description = "Gestion des gammes de contrôles")
@RestController
@RequestMapping("/api/v1/components/{componentId}/controls")
public class SpecificationController {
    private final SpecificationService specificationService;
    private final SpecificationModelAssembler assembler;

    public SpecificationController(
            SpecificationService specificationService,
            SpecificationModelAssembler assembler
    ) {
        this.specificationService = specificationService;
        this.assembler = assembler;
    }

    /**
     * List all specifications.
     *
     * @param componentId The ID of the component
     * @return The list of all specifications
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
    public CollectionModel<EntityModel<SpecificationResponse>> list(@PathVariable Long componentId) {
        List<SpecificationResponse> specifications = specificationService.getByComponent(componentId);

        return assembler.toCollectionModel(specifications)
                .add(linkTo(methodOn(SpecificationController.class).list(componentId)).withSelfRel());
    }

    /**
     * Create a new specification.
     *
     * @param componentId The ID of the component
     * @return The created specification
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
    public ResponseEntity<EntityModel<SpecificationResponse>> create(@PathVariable Long componentId, @Valid @RequestBody SpecificationCreateRequest request) {
        SpecificationResponse created = specificationService.save(componentId, request);
        EntityModel<SpecificationResponse> model = assembler.toModel(created);

        return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
    }

    /**
     * Update an existing specification.
     *
     * @param componentId The ID of the component
     * @param id          The ID of the specification
     * @return The updated specification
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
    public EntityModel<SpecificationResponse> update(@PathVariable Long componentId, @PathVariable Long id, @Valid @RequestBody SpecificationUpdateRequest request) {
        SpecificationResponse updated = specificationService.update(id, request);

        return assembler.toModel(updated);
    }

    /**
     * Change the status of a specification to DELETED.
     *
     * @param componentId The ID of the component
     * @param id          The ID of the specification
     * @return The updated specification
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
        specificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}