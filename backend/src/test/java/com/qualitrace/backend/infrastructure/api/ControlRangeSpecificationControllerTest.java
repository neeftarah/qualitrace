package com.qualitrace.backend.infrastructure.api;

import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.controls.domain.model.ControlRangeSpecification;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.controls.domain.repository.ControlRangeSpecificationRepository;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.component.domain.type.ComponentType;
import com.qualitrace.backend.supplier.domain.type.SupplierStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(statements = "INSERT INTO suppliers (id, code, name, address, status) VALUES (1, 'SUPP01', 'Fournisseur Test', '123 Main Street', 'ACTIVE') ON CONFLICT DO NOTHING;")
class ControlRangeSpecificationControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static RestTestClient restClient;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private ControlRangeSpecificationRepository controlRepository;

    private static final Supplier TEST_SUPPLIER = new Supplier(
            1L, "SUPP01", "Fournisseur Test", "123 Main Street", SupplierStatus.ACTIVE
    );

    @BeforeAll
    void setUpClient() {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        restClient = RestTestClient.bindTo(mockMvc).build();
    }

    // ------------------------------------------------------------------
    // CRUD de base
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void create() {
        Long componentId = createTestComponent(ComponentStatus.ACTIVE);

        restClient.post().uri("/api/v1/components/{componentId}/controls", componentId)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                {"name": "pH", "method": "pH-3215", "unit": "pH", "min": 6.5, "max": 7.5}
                """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo("pH")
                .jsonPath("$.method").isEqualTo("pH-3215")
                .jsonPath("$.unit").isEqualTo("pH")
                .jsonPath("$.min").isEqualTo(6.5)
                .jsonPath("$.max").isEqualTo(7.5)
                .jsonPath("$.componentId").isEqualTo(componentId);

        // Effet de bord : le composant repasse en DRAFT
        Component reloaded = componentRepository.findById(componentId).orElseThrow();
        assertThat(reloaded.status()).isEqualTo(ComponentStatus.DRAFT);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createShouldFailWhenComponentArchived() {
        Long componentId = createTestComponent(ComponentStatus.ARCHIVED);

        restClient.post().uri("/api/v1/components/{componentId}/controls", componentId)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                {"name": "pH", "method": "pH-3215", "unit": "pH", "min": 6.5, "max": 7.5}
                """)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createShouldFailWhenComponentNotFound() {
        restClient.post().uri("/api/v1/components/{componentId}/controls", 999999L)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                {"name": "pH", "method": "pH-3215", "unit": "pH", "min": 6.5, "max": 7.5}
                """)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void update() {
        Long componentId = createTestComponent(ComponentStatus.DRAFT);
        Long specId = createTestControl(componentId, "pH", "pH-3215", "pH", 6.5, 7.5);

        restClient.put().uri("/api/v1/components/{componentId}/controls/{id}", componentId, specId)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                {"method": "HPLC", "min": 6.0, "max": 8.0}
                """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(specId)
                .jsonPath("$.name").isEqualTo("pH")
                .jsonPath("$.method").isEqualTo("HPLC")
                .jsonPath("$.min").isEqualTo(6.0)
                .jsonPath("$.max").isEqualTo(8.0);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateShouldFailWhenComponentArchived() {
        Long componentId = createTestComponent(ComponentStatus.ACTIVE);
        Long specId = createTestControl(componentId, "pH", "pH-3215", "pH", 6.5, 7.5);
        archiveComponent(componentId);

        restClient.put().uri("/api/v1/components/{componentId}/controls/{id}", componentId, specId)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                {"name": "pH updated", "method": "HPLC", "unit": "pH", "min": 6.0, "max": 8.0}
                """)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateShouldFailWhenSpecNotFound() {
        Long componentId = createTestComponent(ComponentStatus.DRAFT);

        restClient.put().uri("/api/v1/components/{componentId}/controls/{id}", componentId, 999999L)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                {"name": "pH", "method": "pH-3215", "unit": "pH", "min": 6.5, "max": 7.5}
                """)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void delete() {
        Long componentId = createTestComponent(ComponentStatus.ACTIVE);
        Long specId = createTestControl(componentId, "pH", "pH-3215", "pH", 6.5, 7.5);

        restClient.method(HttpMethod.DELETE)
                .uri("/api/v1/components/{componentId}/controls/{id}", componentId, specId)
                .exchange()
                .expectStatus().isNoContent();

        Component reloaded = componentRepository.findById(componentId).orElseThrow();
        assertThat(reloaded.status()).isEqualTo(ComponentStatus.DRAFT);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteAlreadyDeletedShouldFail() {
        Long componentId = createTestComponent(ComponentStatus.DRAFT);
        Long specId = createTestControl(componentId, "pH", "pH-3215", "pH", 6.5, 7.5);
        controlRepository.save(controlRepository.findById(specId).orElseThrow().delete());

        restClient.method(HttpMethod.DELETE)
                .uri("/api/v1/components/{componentId}/controls/{id}", componentId, specId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteShouldFailWhenComponentArchived() {
        Long componentId = createTestComponent(ComponentStatus.ACTIVE);
        Long specId = createTestControl(componentId, "pH", "pH-3215", "pH", 6.5, 7.5);
        archiveComponent(componentId);

        restClient.method(HttpMethod.DELETE)
                .uri("/api/v1/components/{componentId}/controls/{id}", componentId, specId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    // ------------------------------------------------------------------
    // Liste : exclut les gammes supprimées, accessible même si composant archivé
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void listShouldExcludeDeletedSpecs() {
        Long componentId = createTestComponent(ComponentStatus.DRAFT);
        createTestControl(componentId, "pH", "pH-3215", "pH", 6.5, 7.5);
        Long deletedId = createTestControl(componentId, "Humidity", "Karl Fischer", "%", 0.0, 5.0);
        controlRepository.save(controlRepository.findById(deletedId).orElseThrow().delete());

        restClient.get().uri("/api/v1/components/{componentId}/controls", componentId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$._embedded.controlRangeSpecifications").isArray()
                .jsonPath("$._embedded.controlRangeSpecifications.length()").isEqualTo(1)
                .jsonPath("$._embedded.controlRangeSpecifications[0].name").isEqualTo("pH");
    }

    @Test
    @WithMockUser(username = "simple-user", roles = "AQ")
    void listShouldBeAccessibleEvenIfComponentArchived() {
        Long componentId = createTestComponent(ComponentStatus.ACTIVE);
        createTestControl(componentId, "pH", "pH-3215", "pH", 6.5, 7.5);
        archiveComponent(componentId);

        restClient.get().uri("/api/v1/components/{componentId}/controls", componentId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$._embedded.controlRangeSpecifications.length()").isEqualTo(1);
    }

    // ------------------------------------------------------------------
    // Sécurité
    // ------------------------------------------------------------------

    @ParameterizedTest
    @CsvSource({
            "GET, /api/v1/components/1/controls",
            "POST, /api/v1/components/1/controls",
            "PUT, /api/v1/components/1/controls/1",
            "DELETE, /api/v1/components/1/controls/1"
    })
    void shouldReturn401WhenNotAuthenticated(String method, String uri) {
        restClient.method(HttpMethod.valueOf(method)).uri(uri)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }


    @Test
    @WithMockUser(username = "simple-user", roles = "AQ")
    void createShouldReturn403WhenInsufficientRole() {
        restClient.post().uri("/api/v1/components/1/controls")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                {"name": "pH", "method": "pH-3215", "unit": "pH", "min": 6.5, "max": 7.5}
                """)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "simple-user", roles = "AQ")
    void updateShouldReturn403WhenInsufficientRole() {
        restClient.put().uri("/api/v1/components/1/controls/1")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                {"method": "HPLC", "min": 6.0, "max": 8.0}
                """)
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "simple-user", roles = "AQ")
    void listShouldSucceedForAnyAuthenticatedUser() {
        Long componentId = createTestComponent(ComponentStatus.DRAFT);

        restClient.get().uri("/api/v1/components/{componentId}/controls", componentId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "simple-user", roles = "AQ")
    void deleteShouldReturn403WhenInsufficientRole() {
        restClient.delete().uri("/api/v1/components/1/controls/1")
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    // ------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------

    private Long createTestComponent(ComponentStatus status) {
        Component component = Component.createNew(
                ComponentType.RAW_MATERIAL, "REF-" + System.nanoTime(), "Test Component", TEST_SUPPLIER);

        if (status == ComponentStatus.ARCHIVED) {
            component = component.activate().archive();
        } else if (status == ComponentStatus.ACTIVE) {
            component = component.activate();
        }

        return componentRepository.save(component).id();
    }

    private void archiveComponent(Long componentId) {
        Component component = componentRepository.findById(componentId).orElseThrow();
        if (component.status() == ComponentStatus.DRAFT) {
            component = component.activate();
        }
        componentRepository.save(component.archive());
    }

    private Long createTestControl(Long componentId, String name, String method, String unit, double min, double max) {
        ControlRangeSpecification spec = ControlRangeSpecification.createNew(name, method, unit, min, max, componentId);
        return controlRepository.save(spec).id();
    }
}