package com.qualitrace.backend.batch.application.assembler;

import com.qualitrace.backend.batch.application.dto.BatchResponse;
import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.batch.infrastructure.api.BatchController;
import org.jspecify.annotations.NullMarked;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Convertit un {@link BatchResponse} en {@link EntityModel} enrichi de liens HATEOAS.
 */
@Component
@NullMarked
public class BatchModelAssembler implements RepresentationModelAssembler<BatchResponse, EntityModel<BatchResponse>> {

    @Override
    public EntityModel<BatchResponse> toModel(BatchResponse batch) {

        EntityModel<BatchResponse> model = EntityModel.of(
                batch,
                linkTo(methodOn(BatchController.class).get(batch.id())).withSelfRel(),
                linkTo(methodOn(BatchController.class).list(null, null, null, null, null, null, null, null, null, null, null, null, null)).withRel("batches"),
                linkTo(methodOn(BatchController.class).destroy(batch.id())).withRel("destroy")
        );

        if (batch.status() == BatchStatus.QUARANTINE) {
            model.add(linkTo(methodOn(BatchController.class).validate(batch.id(), null)).withRel("validate"));
        } else if (batch.status() == BatchStatus.RELEASED) {
            model.add(linkTo(methodOn(BatchController.class).use(batch.id())).withRel("use"));
        }

        return model;
    }
}
