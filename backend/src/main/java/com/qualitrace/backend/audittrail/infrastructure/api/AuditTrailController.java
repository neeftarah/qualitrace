package com.qualitrace.backend.audittrail.infrastructure.api;

import com.qualitrace.backend.audittrail.application.assembler.AuditTrailModelAssembler;
import com.qualitrace.backend.audittrail.application.dto.AuditTrailResponse;
import com.qualitrace.backend.audittrail.application.service.AuditTrailService;
import com.qualitrace.backend.audittrail.domain.model.AuditTrailFilter;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.shared.infrastructure.api.PageQueryUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Infrastructure class that define the audit trails API endpoints.
 */
@Tag(name = "AuditTrails", description = "Gestion des traces d'audit")
@RestController
@RequestMapping("/api/v1/audit_trail")
public class AuditTrailController {
    private final AuditTrailService auditTrailService;
    private final AuditTrailModelAssembler assembler;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "author_id", "event", "entity_type", "entity_id", "content", "timestamp"
    );

    public AuditTrailController(AuditTrailService auditTrailService, AuditTrailModelAssembler assembler) {
        this.auditTrailService = auditTrailService;
        this.assembler = assembler;
    }

    /**
     * List all audit trails.
     *
     * @return The list of all audit trails
     */
    @Operation(
            summary = "Rechercher des traces d'audit",
            description = "Retourne la liste des traces d'audit correspondant aux critères de recherche."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des traces d'audit retournée"),
            @ApiResponse(responseCode = "400", description = "Requête invalide",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('AQ') or hasRole('ADMIN')")
    public CollectionModel<EntityModel<AuditTrailResponse>> list(
            @RequestParam(required = false) UUID author_id,
            @RequestParam(required = false) String event,
            @RequestParam(required = false) String entity_type,
            @RequestParam(required = false) String entity_id,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @ParameterObject @PageableDefault(size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable,
            PagedResourcesAssembler<AuditTrailResponse> pagedAssembler
    ) {
        System.out.println(pageable);
        PageQuery pageQuery = PageQueryUtils.toPageQuery(pageable, ALLOWED_SORT_FIELDS);
        AuditTrailFilter filter = new AuditTrailFilter(author_id, event, entity_type, entity_id, content, fromDate, toDate);

        PageResult<AuditTrailResponse> result = auditTrailService.getAll(pageQuery, filter);

        Page<AuditTrailResponse> page = new PageImpl<>(
                result.content(),
                pageable,
                result.totalElements()
        );

        return pagedAssembler.toModel(page, assembler);
    }
}