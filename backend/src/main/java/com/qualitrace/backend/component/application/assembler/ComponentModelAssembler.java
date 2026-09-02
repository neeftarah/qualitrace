package com.qualitrace.backend.component.application.assembler;

import com.qualitrace.backend.component.application.dto.ComponentResponse;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.component.infrastructure.api.ComponentController;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Convertit un {@link ComponentResponse} en {@link EntityModel} enrichi de liens HATEOAS.
 */
@Component
@NullMarked
public class ComponentModelAssembler implements RepresentationModelAssembler<ComponentResponse, EntityModel<ComponentResponse>> {

    @Override
    public EntityModel<ComponentResponse> toModel(ComponentResponse component) {

        EntityModel<ComponentResponse> model = EntityModel.of(
                component,
                linkTo(methodOn(ComponentController.class).get(component.id())).withSelfRel(),
                linkTo(methodOn(ComponentController.class).list(
                        null,
                        null,
                        null,
                        null,
                        null,
                        Pageable.unpaged(),
                        new PagedResourcesAssembler<>(null, null)
                )).withRel("components")
        );

        // Lien de mise à jour : présent uniquement si le composant n'est pas archivé
        if (component.status() != ComponentStatus.ARCHIVED) {
            model.add(linkTo(methodOn(ComponentController.class).update(component.id(), null)).withRel("update"));
            model.add(linkTo(methodOn(ComponentController.class).archive(component.id())).withRel("archive"));
        } else {
            model.add(linkTo(methodOn(ComponentController.class).activate(component.id())).withRel("activate"));
        }

        return model;
    }
}
