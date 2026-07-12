package com.qualitrace.backend.domain.model;

import com.qualitrace.backend.domain.type.UserRole;
import com.qualitrace.backend.domain.type.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;


class UserTest {
    @Test
    void createNewUserWithValidRoles() {
        User user = createUser();

        assertThat(user.id()).isInstanceOf(UUID.class);
        assertThat(user.login()).isEqualTo("jdoe");
        assertThat(user.password()).isEqualTo("password");
        assertThat(user.email()).isEqualTo("jdoe@exemple.com");
        assertThat(user.firstname()).isEqualTo("John");
        assertThat(user.surname()).isEqualTo("Doe");
        assertThat(user.roles()).containsExactlyInAnyOrder(UserRole.AQ, UserRole.CQ);
        assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.createdAt()).isNotNull();
    }

    @Test
    void createNewUserWithoutRolesShouldThrowException() {
        assertThatException().isThrownBy(() -> User.createNew(
                "jdoe",
                "password",
                "jdoe@exemple.com",
                "John",
                "Doe",
                Set.of()
        )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("User must have at least one role");
    }

    @Test
    void updateUserWithoutRolesShouldThrowException() {
        User user = createUser();

        assertThatException().isThrownBy(() -> user.update(
                        "John2",
                        "Doe2",
                        Set.of()
                )).isInstanceOf(IllegalArgumentException.class)
                .withMessage("User must have at least one role");
    }

    @Test
    void changeStatuses() {
        User user = createUser();
        assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);

        assertThatException().isThrownBy(user::reactivate).isInstanceOf(IllegalStateException.class)
                .withMessage("Seul un utilisateur archivé peut être réactivé (statut actuel : ACTIVE)");

        // ACTIVE ==> LOCKED
        User lockedUser = user.lock();
        assertThat(lockedUser.status()).isEqualTo(UserStatus.LOCKED);

        assertThatException().isThrownBy(lockedUser::lock).isInstanceOf(IllegalStateException.class)
                .withMessage("Seul un utilisateur actif peut être verrouillé (statut actuel : LOCKED)");

        // LOCKED ==> ACTIVE
        User activeUser = lockedUser.unlock();
        assertThat(activeUser.status()).isEqualTo(UserStatus.ACTIVE);

        // LOCKED ==> ACTIVE
        User archivedLockUser = lockedUser.archive();
        assertThat(archivedLockUser.status()).isEqualTo(UserStatus.ARCHIVED);

        // ACTIVE ==> ARCHIVED
        User archivedUser = activeUser.archive();
        assertThat(archivedUser.status()).isEqualTo(UserStatus.ARCHIVED);

        assertThatException().isThrownBy(archivedUser::lock).isInstanceOf(IllegalStateException.class)
                .withMessage("Seul un utilisateur actif peut être verrouillé (statut actuel : ARCHIVED)");
        assertThatException().isThrownBy(archivedUser::archive).isInstanceOf(IllegalStateException.class)
                .withMessage("L'utilisateur est déjà archivé");

        // ARCHIVED ==> ACTIVE
        User reactivatedUser = archivedUser.reactivate();
        assertThat(reactivatedUser.status()).isEqualTo(UserStatus.ACTIVE);
    }

    private User createUser() {
        return User.createNew(
                "jdoe",
                "password",
                "jdoe@exemple.com",
                "John",
                "Doe",
                Set.of(UserRole.AQ, UserRole.CQ)
        );
    }
}