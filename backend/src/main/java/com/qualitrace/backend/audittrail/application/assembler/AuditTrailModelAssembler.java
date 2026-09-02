package com.qualitrace.backend.audittrail.application.assembler;

import com.qualitrace.backend.audittrail.application.dto.AuditTrailResponse;
import com.qualitrace.backend.audittrail.infrastructure.api.AuditTrailController;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Convertit un {@link AuditTrailResponse} en {@link EntityModel} enrichi de liens HATEOAS.
 */
@Component
@NullMarked
public class AuditTrailModelAssembler implements RepresentationModelAssembler<AuditTrailResponse, EntityModel<AuditTrailResponse>> {

    @Override
    public EntityModel<AuditTrailResponse> toModel(AuditTrailResponse response) {
        return EntityModel.of(
                response,
                linkTo(methodOn(AuditTrailController.class).list(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        Pageable.unpaged(),
                        new PagedResourcesAssembler<>(null, null)
                )).withSelfRel()
        );
    }
}
