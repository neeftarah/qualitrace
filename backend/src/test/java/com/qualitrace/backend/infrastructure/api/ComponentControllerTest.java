package com.qualitrace.backend.infrastructure.api;

import com.qualitrace.backend.domain.model.Component;
import com.qualitrace.backend.domain.model.ControlRangeSpecification;
import com.qualitrace.backend.domain.model.Supplier;
import com.qualitrace.backend.domain.repository.ComponentRepository;
import com.qualitrace.backend.domain.repository.ControlRangeSpecificationRepository;
import com.qualitrace.backend.domain.type.ComponentStatus;
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
import org.springframework.test.context.jdbc.Sql;
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
@Sql(statements = "INSERT INTO suppliers (id, code, name, address, status) VALUES (1, 'SUPP01', 'Fournisseur Test', '123 Main Street', 'ACTIVE') ON CONFLICT DO NOTHING;")
class ComponentControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static RestTestClient restClient;

    @Autowired
    private ComponentRepository componentRepository; // le port du domaine, pas le JPA repository directement

    @Autowired
    private ControlRangeSpecificationRepository controlRepository;

    private static final Supplier TEST_SUPPLIER = new Supplier(
            1L,
            "SUPP01",
            "Fournisseur Test",
            "123 Main Street",
            SupplierStatus.ACTIVE
    );

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
        restClient.post().uri("/api/v1/components")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                          "type": "RAW_MATERIAL",
                          "reference": "CMP-001",
                          "name": "Matériau de haute qualité",
                          "supplierId": 1
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.type").isEqualTo("RAW_MATERIAL")
                .jsonPath("$.reference").isEqualTo("CMP-001")
                .jsonPath("$.name").isEqualTo("Matériau de haute qualité")
                .jsonPath("$.supplier.id").isEqualTo(1)
                .jsonPath("$.supplier.code").isEqualTo("SUPP01")
                .jsonPath("$.supplier.name").isEqualTo("Fournisseur Test")
                .jsonPath("$.supplier.address").isEqualTo("123 Main Street")
                .jsonPath("$.supplier.status").isEqualTo("ACTIVE")
                .jsonPath("$.status").isEqualTo("DRAFT");
    }

    /**
     * Test la liste des composants.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void list() {
        createTestComponent(ComponentType.RAW_MATERIAL, "COMP-001", "Test Component 1", ComponentStatus.DRAFT);
        createTestComponent(ComponentType.COMPONENT, "COMP-002", "Test Component 2", ComponentStatus.ACTIVE);
        createTestComponent(ComponentType.COMPONENT, "COMP-003", "Test Component 3", ComponentStatus.ARCHIVED);

        restClient.get().uri("/api/v1/components")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$._embedded.components").isArray()
                .jsonPath("$._embedded.components.length()").isEqualTo(3)
                .jsonPath("$._embedded.components[?(@.reference=='COMP-001')]").exists()
                .jsonPath("$._embedded.components[?(@.reference=='COMP-002')]").exists()
                .jsonPath("$._embedded.components[?(@.reference=='COMP-003')]").exists();
    }

    /**
     * Test la récupération des informations d'un composant.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void get() {
        String id = createTestComponent(ComponentType.RAW_MATERIAL, "COMP-001", "Test Component 1", ComponentStatus.ACTIVE);
        restClient.get().uri("/api/v1/components/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.type").isEqualTo("RAW_MATERIAL")
                .jsonPath("$.reference").isEqualTo("COMP-001")
                .jsonPath("$.name").isEqualTo("Test Component 1")
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void getUnexistingComponent() {
        restClient.get().uri("/api/v1/components/{id}", 999999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    /**
     * Test de la mise à jour d'un composant existant.
     * Vérifie que le champ name est correctement mis à jour.
     * Vérifie également que le code reste inchangé.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void update() {
        String id = createTestComponent(ComponentType.RAW_MATERIAL, "COMP-001", "Test Component 1", ComponentStatus.DRAFT);
        restClient.put().uri("/api/v1/components/{id}", id)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                            "name": "Test Component 1 MODIFIED"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(Long.parseLong(id))
                .jsonPath("$.reference").isEqualTo("COMP-001")
                .jsonPath("$.name").isEqualTo("Test Component 1 MODIFIED");
    }

    /**
     * Test de l'archivage d'un composant.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void archive() {
        String id = createTestComponent(ComponentType.RAW_MATERIAL, "COMP-001", "Test Component 1", ComponentStatus.ACTIVE);
        restClient.patch().uri("/api/v1/components/{id}/archive", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("ARCHIVED")
                .jsonPath("$.availableFrom").isEqualTo(null);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void archiveNotValidIfAlreadyArchived() {
        String id = createTestComponent(ComponentType.RAW_MATERIAL, "COMP-001", "Test Component 1", ComponentStatus.ARCHIVED);
        restClient.patch().uri("/api/v1/components/{id}/archive", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void activateNotValidIfActive() {
        String id = createTestComponent(ComponentType.RAW_MATERIAL, "COMP-001", "Test Component 1", ComponentStatus.ACTIVE);
        restClient.patch().uri("/api/v1/components/{id}/activate", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    /**
     * Test de l'unicité de la référence du composant.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void createComponentCodeUnicity() {
        createTestComponent(ComponentType.RAW_MATERIAL, "COMP-001", "Test Component 1", ComponentStatus.ACTIVE);
        restClient.post().uri("/api/v1/components")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                            "type": "RAW_MATERIAL",
                            "reference": "COMP-001",
                            "name": "Matériau de haute qualité V2",
                            "supplierId": 1
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
            "GET, /api/v1/components",
            "GET, /api/v1/components/1",
            "POST, /api/v1/components",
            "PUT, /api/v1/components/1",
            "PATCH, /api/v1/components/1/archive",
            "PATCH, /api/v1/components/1/activate"
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
            "PATCH, /api/v1/components/1/archive",
            "PATCH, /api/v1/components/1/activate"
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
        restClient.post().uri("/api/v1/components")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {"type": "RAW_MATERIAL", "reference": "SUP999", "name": "Test", "supplierId": 1}
                        """)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "simple-user", roles = "AQ")
    void updateShouldReturn403WhenInsufficientRole() {
        String id = createTestComponent(ComponentType.RAW_MATERIAL, "COMP-001", "Test Component 1", ComponentStatus.ACTIVE);
        restClient.put().uri("/api/v1/components/{id}", id)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {"name": "Hacked"}
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
        String id = createTestComponent(ComponentType.RAW_MATERIAL, "COMP-001", "Test Component 1", ComponentStatus.ACTIVE);
        restClient.get().uri("/api/v1/components/{id}", id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "simple-user", roles = "AQ")
    void listShouldSucceedForAnyAuthenticatedUser() {
        restClient.get().uri("/api/v1/components")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void activateShouldFailWithoutAnySpecification() {
        String id = createTestComponent(ComponentType.COMPONENT, "COMP-001", "Test Component 1", ComponentStatus.DRAFT); // aucune gamme créée

        restClient.patch().uri("/api/v1/components/{id}/activate", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void activateShouldSucceedWithAtLeastOneActiveSpecification() {
        String id = createTestComponent(ComponentType.COMPONENT, "COMP-001", "Test Component 1", ComponentStatus.DRAFT);
        createTestControl(Long.valueOf(id), "pH", "Titration", "pH", 6.5, 7.5);

        restClient.patch().uri("/api/v1/components/{id}/activate", id)
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("ACTIVE")
                .jsonPath("$.availableFrom").isNotEmpty();
    }

    /**
     * Méthode générique de création d'un composant pour simplifier les autres tests.
     *
     * @param type      Le code du composant
     * @param reference La référence du composant
     * @param name      Le nom du composant
     * @param status    Le statut du composant
     * @return L'id du composant créé
     */
    private String createTestComponent(ComponentType type, String reference, String name, ComponentStatus status) {
        Component component = Component.createNew(type, reference, name, TEST_SUPPLIER);

        if (status == ComponentStatus.ARCHIVED) {
            component = component.archive();
        } else if (status == ComponentStatus.ACTIVE) {
            component = component.activate();
        }

        Component saved = componentRepository.save(component);

        return saved.id().toString();
    }

    private Long createTestControl(Long componentId, String name, String method, String unit, double min, double max) {
        ControlRangeSpecification spec = ControlRangeSpecification.createNew(name, method, unit, min, max, componentId);
        return controlRepository.save(spec).id();
    }
}