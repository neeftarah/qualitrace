package com.qualitrace.backend.application.assembler;

import com.qualitrace.backend.application.dto.ControlRangeSpecificationResponse;
import com.qualitrace.backend.infrastructure.api.ControlRangeSpecificationController;
import org.jspecify.annotations.NullMarked;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Convertit un {@link ControlRangeSpecificationResponse} en {@link EntityModel} enrichi de liens HATEOAS.
 */
@Component
@NullMarked
public class ControlRangeSpecificationModelAssembler implements RepresentationModelAssembler<ControlRangeSpecificationResponse, EntityModel<ControlRangeSpecificationResponse>> {

    @Override
    public EntityModel<ControlRangeSpecificationResponse> toModel(ControlRangeSpecificationResponse controlRangeSpecification) {

        EntityModel<ControlRangeSpecificationResponse> model = EntityModel.of(
                controlRangeSpecification,
                linkTo(methodOn(ControlRangeSpecificationController.class)
                        .list(controlRangeSpecification.componentId()))
                        .withSelfRel()
        );

        // Lien de mise à jour : présent uniquement si la gamme de contrôle n'est pas archivée
        model.add(linkTo(methodOn(ControlRangeSpecificationController.class).update(0L, 0L, null)).withRel("update"));
        model.add(linkTo(methodOn(ControlRangeSpecificationController.class).delete(0L, 0L)).withRel("delete"));

        return model;
    }
}
