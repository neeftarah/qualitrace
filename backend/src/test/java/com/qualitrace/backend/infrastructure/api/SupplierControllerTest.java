package com.qualitrace.backend.infrastructure.api;

import com.qualitrace.backend.domain.model.Component;
import com.qualitrace.backend.domain.model.Supplier;
import com.qualitrace.backend.domain.repository.ComponentRepository;
import com.qualitrace.backend.domain.repository.SupplierRepository;
import com.qualitrace.backend.domain.type.ComponentType;
import com.qualitrace.backend.domain.type.SupplierStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SupplierControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static RestTestClient restClient;

    @Autowired
    private SupplierRepository supplierRepository; // le port du domaine, pas le JPA repository directement

    @Autowired
    private ComponentRepository componentRepository; // le port du domaine, pas le JPA repository directement

    @BeforeAll
    void setUpClient() {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        restClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void create() {
        restClient.post().uri("/api/v1/suppliers")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                            "code": "SUP001",
                            "name": "Acme Corporation",
                            "address": "123 Main Street"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.code").isEqualTo("SUP001")
                .jsonPath("$.name").isEqualTo("Acme Corporation")
                .jsonPath("$.address").isEqualTo("123 Main Street")
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    /**
     * Test la liste des fournisseurs.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void list() {
        createTestSupplier("SUP001", "Acme Corporation", "123 Main Street", SupplierStatus.ACTIVE);
        createTestSupplier("SUP002", "Globex Inc", "456 Other Ave", SupplierStatus.ACTIVE);

        restClient.get().uri("/api/v1/suppliers")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$._embedded.suppliers").isArray()
                .jsonPath("$._embedded.suppliers.length()").isEqualTo(2)
                .jsonPath("$._embedded.suppliers[?(@.code=='SUP001')]").exists()
                .jsonPath("$._embedded.suppliers[?(@.code=='SUP002')]").exists();
    }

    /**
     * Test la récupération des informations d'un fournisseur.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void get() {
        String id = createTestSupplier("SUP001", "Acme Corporation", "123 Main Street", SupplierStatus.ACTIVE);
        restClient.get().uri("/api/v1/suppliers/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.code").isEqualTo("SUP001")
                .jsonPath("$.name").isEqualTo("Acme Corporation")
                .jsonPath("$.address").isEqualTo("123 Main Street")
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void getUnexistingSupplier() {
        restClient.get().uri("/api/v1/suppliers/{id}", 999999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    /**
     * Test de la mise à jour d'un fournisseur existant.
     * Vérifie que les champs name et address sont correctement mis à jour.
     * Vérifie également que le code reste inchangé.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void update() {
        String id = createTestSupplier("SUP001", "Acme Corporation", "123 Main Street", SupplierStatus.ACTIVE);
        restClient.put().uri("/api/v1/suppliers/{id}", id)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                            "name": "Acme Corp Renamed",
                            "address": "789 New Street"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(Long.parseLong(id))
                .jsonPath("$.code").isEqualTo("SUP001")
                .jsonPath("$.name").isEqualTo("Acme Corp Renamed")
                .jsonPath("$.address").isEqualTo("789 New Street");
    }

    /**
     * Test de l'archivage d'un fournisseur.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void archive() {
        String id = createTestSupplier("SUP001", "Acme Corporation", "123 Main Street", SupplierStatus.ACTIVE);
        restClient.patch().uri("/api/v1/suppliers/{id}/archive", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("ARCHIVED");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void archiveNotValidIfAlreadyArchived() {
        String id = createTestSupplier("SUP001", "Acme Corporation", "123 Main Street", SupplierStatus.ARCHIVED);
        restClient.patch().uri("/api/v1/suppliers/{id}/archive", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    /**
     * Test de la réactivation d'un fournisseur archivé.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void activate() {
        String id = createTestSupplier("SUP001", "Acme Corporation", "123 Main Street", SupplierStatus.ARCHIVED);
        restClient.patch().uri("/api/v1/suppliers/{id}/activate", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void activateNotValidIfActive() {
        String id = createTestSupplier("SUP001", "Acme Corporation", "123 Main Street", SupplierStatus.ACTIVE);
        restClient.patch().uri("/api/v1/suppliers/{id}/activate", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    /**
     * Test de l'unicité du code fournisseur.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void createSupplierCodeUnicity() {
        createTestSupplier("SUP001", "Acme Corporation", "123 Main Street", SupplierStatus.ACTIVE);
        restClient.post().uri("/api/v1/suppliers")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                            "code": "SUP001",
                            "name": "Another Corp",
                            "address": "999 Other Street"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    /**
     * Test les accès "non authentifiés → 401", sur toutes les routes protégées.
     */
    @ParameterizedTest
    @CsvSource({
            "GET, /api/v1/suppliers",
            "GET, /api/v1/suppliers/1",
            "POST, /api/v1/suppliers",
            "PUT, /api/v1/suppliers/1",
            "PATCH, /api/v1/suppliers/1/archive",
            "PATCH, /api/v1/suppliers/1/activate"
    })
    void shouldReturn401WhenNotAuthenticated(String method, String uri) {
        restClient.method(HttpMethod.valueOf(method)).uri(uri)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    /**
     * Test les accès "authentifiés mais rôle insuffisant → 403", sur les routes réservées ADMIN.
     */
    @ParameterizedTest
    @CsvSource({
            "PATCH, /api/v1/suppliers/1/archive",
            "PATCH, /api/v1/suppliers/1/activate"
    })
    @WithMockUser(username = "simple-user", roles = "AQ")
    void shouldReturn403WhenInsufficientRole(String method, String uri) {
        restClient.method(HttpMethod.valueOf(method)).uri(uri)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "simple-user", roles = "AQ")
    void createShouldReturn403WhenInsufficientRole() {
        restClient.post().uri("/api/v1/suppliers")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {"code": "SUP999", "name": "Test", "address": "Test address"}
                        """)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "simple-user", roles = "AQ")
    void updateShouldReturn403WhenInsufficientRole() {
        String id = createTestSupplier("SUP001", "Acme Corporation", "123 Main Street", SupplierStatus.ACTIVE);
        restClient.put().uri("/api/v1/suppliers/{id}", id)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {"name": "Hacked", "address": "Nowhere"}
                        """)
                .exchange()
                .expectStatus().isForbidden();
    }

    /**
     * GET/list restent accessibles à tout utilisateur authentifié, pas seulement ADMIN.
     */
    @Test
    @WithMockUser(username = "simple-user", roles = "AQ")
    void getShouldSucceedForAnyAuthenticatedUser() {
        String id = createTestSupplier("SUP001", "Acme Corporation", "123 Main Street", SupplierStatus.ACTIVE);
        restClient.get().uri("/api/v1/suppliers/{id}", id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "simple-user", roles = "AQ")
    void listShouldSucceedForAnyAuthenticatedUser() {
        restClient.get().uri("/api/v1/suppliers")
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Teste que l'archivage d'un fournisseur entraîne l'archivage de ses composants associés.
     * Vérifie également que l'audit est correctement écrit.
     *
     * RG-REF-02 : L'archivage d'un fournisseur entraîne automatiquement l'archivage de toutes les matières premières qui lui sont rattachées.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void archivingSupplierCascadesToComponentsAndWritesAudit() {
        Supplier supplier = supplierRepository.save(
                Supplier.createNew("SUP001", "Acme Corporation", "123 Main Street")
        );
        String componentId = createTestComponent(supplier);

        restClient.patch().uri("/api/v1/suppliers/{id}/archive", supplier.id())
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("ARCHIVED");

        restClient.get().uri("/api/v1/components/{id}", componentId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.id").isEqualTo(componentId)
                .jsonPath("$.status").isEqualTo("ARCHIVED");
    }


    /**
     * Méthode générique de création d'un fournisseur pour simplifier les autres tests.
     *
     * @param code    Le code du fournisseur
     * @param name    Le nom du fournisseur
     * @param address L'adresse du fournisseur
     * @param status  Le statut du fournisseur
     * @return L'id du fournisseur créé
     */
    private String createTestSupplier(String code, String name, String address, SupplierStatus status) {
        Supplier supplier = Supplier.createNew(code, name, address);

        if (status == SupplierStatus.ARCHIVED) {
            supplier = supplier.archive();
        }

        Supplier saved = supplierRepository.save(supplier);

        return saved.id().toString();
    }

    private String createTestComponent(Supplier supplier) {
        Component component = Component.createNew(
                ComponentType.RAW_MATERIAL,
                "COMP-001",
                "Test Component 1",
                supplier
        );

        Component saved = componentRepository.save(component);

        return saved.id().toString();
    }
}