package com.qualitrace.backend.application.assembler;

import com.qualitrace.backend.application.dto.UserResponse;
import com.qualitrace.backend.domain.type.UserStatus;
import com.qualitrace.backend.infrastructure.api.UserController;
import org.jspecify.annotations.NullMarked;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@NullMarked
public class UserModelAssembler implements RepresentationModelAssembler<UserResponse, EntityModel<UserResponse>> {

    @Override
    public EntityModel<UserResponse> toModel(UserResponse user) {
        EntityModel<UserResponse> model = EntityModel.of(
                user,
                linkTo(methodOn(UserController.class).get(user.id())).withSelfRel()
        );

        // Lien de mise à jour : présent uniquement si l'utilisateur n'est pas archivé
        if (user.status() != UserStatus.ARCHIVED) {
            model.add(linkTo(methodOn(UserController.class).update(user.id(), null))
                    .withRel("update"));
        }

        // Liens de transition de statut, selon les règles métier de User.isValidTransition()
        addStatusTransitionLinks(model, user);

        return model;
    }

    private void addStatusTransitionLinks(EntityModel<UserResponse> model, UserResponse user) {
        switch (user.status()) {
            case ACTIVE -> {
                model.add(linkTo(methodOn(UserController.class).archive(user.id()))
                        .withRel("archive"));
            }
            case LOCKED -> {
                model.add(linkTo(methodOn(UserController.class).unlock(user.id()))
                        .withRel("activate"));
            }
            case ARCHIVED -> {
                model.add(linkTo(methodOn(UserController.class).activate(user.id()))
                        .withRel("activate"));
            }
        }
    }
}
