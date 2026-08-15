package com.qualitrace.backend.application.assembler;

import com.qualitrace.backend.application.dto.SupplierResponse;
import com.qualitrace.backend.application.dto.SupplierUpdateRequest;
import com.qualitrace.backend.domain.type.SupplierStatus;
import com.qualitrace.backend.infrastructure.api.SupplierController;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Convertit un {@link SupplierResponse} en {@link EntityModel} enrichi de liens HATEOAS.
 */
@Component
@NullMarked
public class SupplierModelAssembler implements RepresentationModelAssembler<SupplierResponse, EntityModel<SupplierResponse>> {

    @Override
    public EntityModel<SupplierResponse> toModel(SupplierResponse supplier) {

        EntityModel<SupplierResponse> model = EntityModel.of(
                supplier,
                linkTo(methodOn(SupplierController.class).get(supplier.id())).withSelfRel(),
                linkTo(methodOn(SupplierController.class).list(
                        "",
                        "",
                        SupplierStatus.ACTIVE,
                        Pageable.unpaged(),
                        new PagedResourcesAssembler<>(null, null)
                )).withRel("suppliers")
        );

        // Lien de mise à jour : présent uniquement si le fournisseur n'est pas archivé
        if (supplier.status() != SupplierStatus.ARCHIVED) {
            model.add(linkTo(methodOn(SupplierController.class).update(
                    supplier.id(),
                    new SupplierUpdateRequest("", "")
            )).withRel("update"));
            model.add(linkTo(methodOn(SupplierController.class).archive(supplier.id())).withRel("archive"));
        } else {
            model.add(linkTo(methodOn(SupplierController.class).activate(supplier.id())).withRel("activate"));
        }

        return model;
    }
}
