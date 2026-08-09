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

/**
 * Convertit un {@link UserResponse} en {@link EntityModel} enrichi de liens HATEOAS.
 * <p>
 * Les liens exposés dépendent du statut courant de l'utilisateur, pour refléter
 * uniquement les transitions et actions réellement valides à cet instant
 * (ex : pas de lien "archive" si l'utilisateur est déjà archivé).
 * <p>
 * Toujours présents : self, update (sauf si archivé).
 * Conditionnels selon le statut : lock/unlock/archive/activate.
 */
@Component
@NullMarked
public class UserModelAssembler implements RepresentationModelAssembler<UserResponse, EntityModel<UserResponse>> {

    @Override
    public EntityModel<UserResponse> toModel(UserResponse user) {
        EntityModel<UserResponse> model = EntityModel.of(
                user,
                linkTo(methodOn(UserController.class).get(user.id())).withSelfRel(),
                linkTo(methodOn(UserController.class).list(null, null, null, null, null, null, null, null)).withRel("users")
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
