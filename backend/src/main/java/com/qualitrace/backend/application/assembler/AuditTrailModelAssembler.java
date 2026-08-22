package com.qualitrace.backend.application.assembler;

import com.qualitrace.backend.application.dto.AuditTrailResponse;
import com.qualitrace.backend.infrastructure.api.AuditTrailController;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

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
                        UUID.randomUUID(),
                        "",
                        "",
                        "",
                        "",
                        LocalDate.now(),
                        LocalDate.now(),
                        Pageable.unpaged(),
                        new PagedResourcesAssembler<>(null, null)
                )).withSelfRel()
        );
    }
}
