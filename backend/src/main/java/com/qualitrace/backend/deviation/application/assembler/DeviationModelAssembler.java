package com.qualitrace.backend.deviation.application.assembler;

import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.deviation.domain.type.DeviationStatus;
import com.qualitrace.backend.deviation.infrastructure.api.DeviationController;
import com.qualitrace.backend.deviation.application.dto.DeviationResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Convertit un {@link DeviationResponse} en {@link EntityModel} enrichi de liens HATEOAS.
 */
@Component
@NullMarked
public class DeviationModelAssembler implements RepresentationModelAssembler<DeviationResponse, EntityModel<DeviationResponse>> {

    @Override
    public EntityModel<DeviationResponse> toModel(DeviationResponse deviation) {

        EntityModel<DeviationResponse> model = EntityModel.of(
                deviation,
                linkTo(methodOn(DeviationController.class)
                        .list(deviation.batchId()))
                        .withSelfRel()
        );

        model.add(linkTo(methodOn(DeviationController.class).update(0L, 0L, null)).withRel("update"));

        if (deviation.status() == DeviationStatus.OPENED) {
            model.add(linkTo(methodOn(DeviationController.class).close(0L, 0L)).withRel("close"));
        } else {
            model.add(linkTo(methodOn(DeviationController.class).open(0L, 0L)).withRel("open"));
        }

        return model;
    }
}
