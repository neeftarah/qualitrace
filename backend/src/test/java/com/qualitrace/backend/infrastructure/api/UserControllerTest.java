package com.qualitrace.backend.infrastructure.api;

import com.qualitrace.backend.domain.model.User;
import com.qualitrace.backend.domain.repository.UserRepository;
import com.qualitrace.backend.domain.type.UserRole;
import com.qualitrace.backend.domain.type.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Set;
import java.util.UUID;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
@Transactional
class UserControllerTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private RestTestClient restClient;

    @Autowired
    private UserRepository userRepository; // le port du domaine, pas le JPA repository directement

    @Test
    public void create() {
        restClient.post().uri("/api/v1/users")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                {
                    "login": "jdupont",
                    "password": "SecurePass123!",
                    "email": "jdupont@qualitrace.com",
                    "firstname": "Jean",
                    "surname": "Dupont",
                    "roles": [
                        "ADMIN"
                    ]
                }
                """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.login").isEqualTo("jdupont")
                .jsonPath("$.email").isEqualTo("jdupont@qualitrace.com")
                .jsonPath("$.firstname").isEqualTo("Jean")
                .jsonPath("$.surname").isEqualTo("Dupont")
                .jsonPath("$.status").isEqualTo("ACTIVE")
                .jsonPath("$.roles").isArray()
                .jsonPath("$.roles[0]").isEqualTo("ADMIN")
                .jsonPath("$.createdAt").exists()
                .jsonPath("$.password").doesNotExist(); // sécurité : jamais renvoyé
    }

    /**
     * Test la liste des utilisateurs.
     */
    @Test
    public void list() {
        createTestUser("user1", "user1@test.com", "User1", "One", UserStatus.ACTIVE, UserRole.AQ);
        createTestUser("user2", "user2@test.com", "User2", "Two", UserStatus.ACTIVE, UserRole.CQ, UserRole.PRODUCTION);

        restClient.get().uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$._embedded.users").isArray()
                .jsonPath("$._embedded.users.length()").isEqualTo(2)
                .jsonPath("$._embedded.users[?(@.login=='user1')]").exists()
                .jsonPath("$._embedded.users[?(@.login=='user2')]").exists();
    }

    /**
     * Test la récupération des informations d'un utilisateur.
     * Vérifie que le mot de passe n'est jamais renvoyé dans la réponse.
     */
    @Test
    public void get() {
        String id = createTestUser("user1", "user1@test.com", "User", "One", UserStatus.ACTIVE, UserRole.AQ);
        restClient.get().uri("/api/v1/users/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.login").isEqualTo("user1")
                .jsonPath("$.email").isEqualTo("user1@test.com")
                .jsonPath("$.firstname").isEqualTo("User")
                .jsonPath("$.surname").isEqualTo("One")
                .jsonPath("$.status").isEqualTo("ACTIVE")
                .jsonPath("$.roles").isArray()
                .jsonPath("$.roles[0]").isEqualTo("AQ")
                .jsonPath("$.createdAt").exists()
                .jsonPath("$.password").doesNotExist(); // sécurité : jamais renvoyé
    }

    @Test
    public void getUnexistingUser() {
        String id = UUID.randomUUID().toString();
        restClient.get().uri("/api/v1/users/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    /**
     * Test de la mise à jour d'un utilisateur existant.
     * Vérifie que les champs firstname, surname et roles sont correctement mis à jour.
     * Vérifie également que les champs login, email et status restent inchangés.
     * Vérifie que le mot de passe n'est jamais renvoyé dans la réponse.
     */
    @Test
    public void update() {
        String id = createTestUser("user1", "user1@test.com", "User1", "One", UserStatus.ACTIVE, UserRole.AQ);
        restClient.put().uri("/api/v1/users/{id}", id)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                {
                    "firstname": "Jean",
                    "surname": "Dupont",
                    "roles": [
                        "CQ",
                        "PLANNING",
                        "PRODUCTION"
                    ]
                }
                """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.login").isEqualTo("user1")
                .jsonPath("$.email").isEqualTo("user1@test.com")
                .jsonPath("$.firstname").isEqualTo("Jean")
                .jsonPath("$.surname").isEqualTo("Dupont")
                .jsonPath("$.status").isEqualTo("ACTIVE")
                .jsonPath("$.roles").isArray()
                .jsonPath("$.roles[0]").isEqualTo("CQ")
                .jsonPath("$.roles[1]").isEqualTo("PLANNING")
                .jsonPath("$.roles[2]").isEqualTo("PRODUCTION")
                .jsonPath("$.createdAt").exists()
                .jsonPath("$.updatedAt").exists()
                .jsonPath("$.password").doesNotExist(); // sécurité : jamais renvoyé
    }

    /**
     * Test du changement de statut d'un utilisateur existant.
     * Vérifie que le champ status est correctement mis à jour.
     * Vérifie également que les autres champs restent inchangés.
     * Vérifie que le mot de passe n'est jamais renvoyé dans la réponse.
     */
    @Test
    public void unlock() {
        String id = createTestUser("user1", "user1@test.com", "User1", "One", UserStatus.LOCKED, UserRole.AQ);
        restClient.patch().uri("/api/v1/users/{id}/unlock", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.login").isEqualTo("user1")
                .jsonPath("$.email").isEqualTo("user1@test.com")
                .jsonPath("$.firstname").isEqualTo("User1")
                .jsonPath("$.surname").isEqualTo("One")
                .jsonPath("$.status").isEqualTo("ACTIVE")
                .jsonPath("$.roles").isArray()
                .jsonPath("$.roles[0]").isEqualTo("AQ")
                .jsonPath("$.createdAt").exists()
                .jsonPath("$.updatedAt").exists()
                .jsonPath("$.password").doesNotExist(); // sécurité : jamais renvoyé
    }

    @Test
    public void unlockNotValidIfActive() {
        String id = createTestUser("user1", "user1@test.com", "User1", "One", UserStatus.ACTIVE, UserRole.AQ);
        restClient.patch().uri("/api/v1/users/{id}/unlock", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Seul un utilisateur verrouillé peut être déverrouillé (statut actuel : ACTIVE)");
    }

    @Test
    public void unlockNotValidIfArchive() {
        String id = createTestUser("user1", "user1@test.com", "User1", "One", UserStatus.ARCHIVED, UserRole.AQ);
        restClient.patch().uri("/api/v1/users/{id}/unlock", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Seul un utilisateur verrouillé peut être déverrouillé (statut actuel : ARCHIVED)");
    }

    @Test
    public void activateNotValidIfActive() {
        String id = createTestUser("user1", "user1@test.com", "User1", "One", UserStatus.ACTIVE, UserRole.AQ);
        restClient.patch().uri("/api/v1/users/{id}/activate", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Seul un utilisateur archivé peut être réactivé (statut actuel : ACTIVE)");
    }

    @Test
    public void archiveNotValidIfArchive() {
        String id = createTestUser("user1", "user1@test.com", "User1", "One", UserStatus.ARCHIVED, UserRole.AQ);
        restClient.patch().uri("/api/v1/users/{id}/archive", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.detail").isEqualTo("L'utilisateur est déjà archivé");
    }

    @Test
    public void createUserLoginUnicity() {
        createTestUser("user1", "user1@test.com", "User1", "One", UserStatus.ACTIVE, UserRole.AQ);
        restClient.post().uri("/api/v1/users")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                {
                    "login": "user1",
                    "password": "P@ssw0rd",
                    "email": "user1@test.com",
                    "firstname": "User1",
                    "surname": "One",
                    "roles": ["AQ"]
                }
                """)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.detail").isEqualTo("Une ressource avec le même identifiant unique (uk_users_login) existe déjà !");
    }

    @Test
    public void search() {
    }

    /**
     * Méthode générique de création d'un utilisateur pour simplifier les autres tests.
     *
     * @param login L'identifiant de l'utilisateur
     * @param email L'e-mail de l'utilisateur
     * @param firstname Le prénom de l'utilisateur
     * @param surname Le nom de l'utilisateur
     * @param status Le statut de l'utilisateur
     * @param roles Le(s) rôle(s)  de l'utilisateur
     *
     * @return L'UUID de l'utilisateur créé
     */
    private String createTestUser(String login, String email, String firstname, String surname, UserStatus status, UserRole... roles) {
        User user = User.createNew(
                login,
                "SecurePass123!",
                email,
                firstname,
                surname,
                Set.of(roles)
        );

        switch (status) {
            case LOCKED -> user = user.lock();
            case ARCHIVED -> user = user.archive();
            default -> {
                // Do nothing, the default status is ACTIVE
            }
        }

        userRepository.save(user);

        return user.id().toString();
    }
}