package com.qualitrace.backend.specification.application.assembler;

import com.qualitrace.backend.specification.application.dto.SpecificationResponse;
import com.qualitrace.backend.specification.application.dto.SpecificationUpdateRequest;
import com.qualitrace.backend.specification.infrastructure.api.SpecificationController;
import org.jspecify.annotations.NullMarked;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Convertit un {@link SpecificationResponse} en {@link EntityModel} enrichi de liens HATEOAS.
 */
@Component
@NullMarked
public class SpecificationModelAssembler implements RepresentationModelAssembler<SpecificationResponse, EntityModel<SpecificationResponse>> {

    @Override
    public EntityModel<SpecificationResponse> toModel(SpecificationResponse specification) {

        EntityModel<SpecificationResponse> model = EntityModel.of(
                specification,
                linkTo(methodOn(SpecificationController.class)
                        .list(specification.componentId()))
                        .withSelfRel()
        );

        // Lien de mise à jour : présent uniquement si la gamme de contrôle n'est pas archivée
        model.add(linkTo(methodOn(SpecificationController.class).update(
                specification.componentId(),
                specification.id(),
                new SpecificationUpdateRequest("", 0.0, 0.0)
        )).withRel("update"));
        model.add(linkTo(methodOn(SpecificationController.class).delete(
                specification.componentId(),
                specification.id()
        )).withRel("delete"));

        return model;
    }
}
