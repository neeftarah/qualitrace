package com.qualitrace.backend.infrastructure.api;

import com.qualitrace.backend.analysisresult.domain.model.AnalysisResult;
import com.qualitrace.backend.analysisresult.domain.repository.AnalysisResultRepository;
import com.qualitrace.backend.batch.domain.model.Batch;
import com.qualitrace.backend.batch.domain.repository.BatchRepository;
import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.component.domain.type.ComponentType;
import com.qualitrace.backend.specification.domain.model.Specification;
import com.qualitrace.backend.specification.domain.repository.SpecificationRepository;
import com.qualitrace.backend.deviation.domain.model.Deviation;
import com.qualitrace.backend.deviation.domain.repository.DeviationRepository;
import com.qualitrace.backend.deviation.domain.type.DeviationStatus;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import com.qualitrace.backend.supplier.domain.repository.SupplierRepository;
import com.qualitrace.backend.user.domain.model.User;
import com.qualitrace.backend.user.domain.repository.UserRepository;
import com.qualitrace.backend.user.domain.type.UserRole;
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

import java.time.Instant;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BatchControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private DeviationRepository deviationRepository;

    @Autowired
    private SpecificationRepository controlRepository;

    @Autowired
    private UserRepository userRepository;

    private static RestTestClient restClient;

    @BeforeAll
    void setUpClient() {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        restClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    @WithMockUser(username = "supply", roles = "SUPPLY")
    void createShouldCreateQuarantineBatch() {
        Component component = createActiveComponent("CMP-001");

        restClient.post().uri("/api/v1/batches")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                          "componentId": %d,
                          "supplierReferenceNumber": "SUP-LOT-001",
                          "expiryDate": "2030-12-31T00:00:00Z"
                        }
                        """.formatted(component.id()))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.component.id").isEqualTo(component.id())
                .jsonPath("$.supplierReferenceNumber").isEqualTo("SUP-LOT-001")
                .jsonPath("$.internalReferenceNumber").value(value -> org.junit.jupiter.api.Assertions.assertTrue(value.toString().startsWith("LOT-MP-")))
                .jsonPath("$.status").isEqualTo("QUARANTINE");
    }

    @Test
    @WithMockUser(username = "user", roles = "AQ")
    void listShouldReturnBatches() {
        Component component = createActiveComponent("CMP-001");
        createBatch(component, "SUP-LOT-001", BatchStatus.QUARANTINE);
        createBatch(component, "SUP-LOT-002", BatchStatus.RECEIVED);

        restClient.get().uri("/api/v1/batches")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$._embedded.batches").isArray()
                .jsonPath("$._embedded.batches.length()").isEqualTo(2)
                .jsonPath("$._embedded.batches[?(@.supplierReferenceNumber=='SUP-LOT-001')]").exists()
                .jsonPath("$._embedded.batches[?(@.supplierReferenceNumber=='SUP-LOT-002')]").exists();
    }

    @Test
    @WithMockUser(username = "user", roles = "AQ")
    void getShouldReturnBatch() {
        Batch batch = createBatch(createActiveComponent("CMP-001"), "SUP-LOT-001", BatchStatus.QUARANTINE);

        restClient.get().uri("/api/v1/batches/{id}", batch.id())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(batch.id())
                .jsonPath("$.supplierReferenceNumber").isEqualTo("SUP-LOT-001")
                .jsonPath("$.status").isEqualTo("QUARANTINE");
    }

    @Test
    @WithMockUser(username = "user", roles = "AQ")
    void getUnknownBatchShouldReturn404() {
        restClient.get().uri("/api/v1/batches/{id}", 999999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "aq", roles = "AQ")
    void validateShouldReturnConflictWhenAnalysesAreMissing() {
        Batch batch = createBatch(createActiveComponent("CMP-001"), "SUP-LOT-001", BatchStatus.QUARANTINE);

        restClient.patch().uri("/api/v1/batches/{id}/validate", batch.id())
                .contentType(MediaTypes.HAL_JSON)
                .body("{" + "\"accept\":true}")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "prod", roles = "PROD")
    void useShouldChangeReceivedBatchStatus() {
        Batch batch = createBatch(createActiveComponent("CMP-001"), "SUP-LOT-001", BatchStatus.RECEIVED);

        restClient.patch().uri("/api/v1/batches/{id}/use", batch.id())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("USED");
    }

    @Test
    @WithMockUser(username = "supply", roles = "SUPPLY")
    void destroyShouldChangeRefusedBatchStatus() {
        Batch batch = createBatch(createActiveComponent("CMP-001"), "SUP-LOT-001", BatchStatus.REFUSED);

        restClient.patch().uri("/api/v1/batches/{id}/destroy", batch.id())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("DESTROYED");
    }

    @ParameterizedTest
    @CsvSource({
            "GET, /api/v1/batches?sort=internalReferenceNumber,asc",
            "GET, /api/v1/batches/1",
            "POST, /api/v1/batches",
            "PATCH, /api/v1/batches/1/validate",
            "PATCH, /api/v1/batches/1/use",
            "PATCH, /api/v1/batches/1/destroy"
    })
    void protectedEndpointsShouldReturn401WhenUnauthenticated(String method, String uri) {
        restClient.method(HttpMethod.valueOf(method)).uri(uri)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @WithMockUser(username = "user", roles = "AQ")
    @Test
    void createShouldReturn403WhenRoleIsInsufficient() {
        restClient.post().uri("/api/v1/batches")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {"componentId": 1, "supplierReferenceNumber": "SUP-LOT-001", "expiryDate": "2030-12-31T00:00:00Z"}
                        """)
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "user", roles = "SUPPLY")
    void validateShouldReturn403WhenRoleIsInsufficient() {
        restClient.patch().uri("/api/v1/batches/{id}/validate", 1)
                .contentType(MediaTypes.HAL_JSON)
                .body("{\"accept\":true}")
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "user", roles = "AQ")
    void useShouldReturn403WhenRoleIsInsufficient() {
        restClient.patch().uri("/api/v1/batches/{id}/use", 1)
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "user", roles = "AQ")
    void destroyShouldReturn403WhenRoleIsInsufficient() {
        restClient.patch().uri("/api/v1/batches/{id}/destroy", 1)
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "user", roles = "AQ")
    void getBatchesDetailGenealogy() {
        User user = userRepository.save(User.createNew(
                "cq-analysis", "password", "cq-analysis@example.com", "Control", "Quality", Set.of(UserRole.CQ)));

        Component component = createActiveComponent("CMP-001");
        Batch batch = createBatch(component, "SUP-LOT-001", BatchStatus.QUARANTINE);

        Long specId1 = createTestControl(component.id(), "pH", "pH-3215", "pH", 6.5, 7.5);
        Long specId2 = createTestControl(component.id(), "Viscosité", "Vi-25_30_15", "Cps", 2000, 5000);

        createTestDeviation(batch.id(), "DEV-008", DeviationStatus.CLOSED, "");
        createTestDeviation(batch.id(), "DEV-009", DeviationStatus.OPENED, "");

        createTestAnalysisResult(batch.id(), specId1, 7.2, user);
        createTestAnalysisResult(batch.id(), specId2, 3257.0, user);

        restClient.get().uri("/api/v1/batches/{id}", batch.id())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(batch.id())
                .jsonPath("$.internalReferenceNumber").isEqualTo(batch.internalReferenceNumber())
                .jsonPath("$.supplierReferenceNumber").isEqualTo(batch.supplierReferenceNumber())
                .jsonPath("$.expiryDate").isNotEmpty()
                .jsonPath("$.receptionDate").isNotEmpty()
                .jsonPath("$.status").isEqualTo("QUARANTINE")
                .jsonPath("$.component.id").isEqualTo(component.id())
                .jsonPath("$.component.reference").isEqualTo("CMP-001")
                .jsonPath("$.component.supplier.id").isEqualTo(component.supplier().id())
                .jsonPath("$.component.supplier.code").isEqualTo("SUP-CMP-001")
                .jsonPath("$.component.supplier.name").isEqualTo("Supplier CMP-001")
                .jsonPath("$.specifications").isArray()
                .jsonPath("$.specifications.length()").isEqualTo(2)
                .jsonPath("$.specifications[?(@.results.value==7.2)]").exists()
                .jsonPath("$.specifications[?(@.results.value==3257.0)]").exists()
                .jsonPath("$.deviations").isArray()
                .jsonPath("$.deviations.length()").isEqualTo(2)
                .jsonPath("$.deviations[?(@.code=='DEV-008')]").exists()
                .jsonPath("$.deviations[?(@.code=='DEV-009')]").exists();
    }

    private Component createActiveComponent(String reference) {
        Supplier supplier = supplierRepository.save(Supplier.createNew("SUP-" + reference, "Supplier " + reference, "1 rue des Tests"));
        Component draft = componentRepository.save(Component.createNew(ComponentType.RAW_MATERIAL, reference, "Component " + reference, supplier));
        return componentRepository.save(new Component(
                draft.id(), draft.type(), draft.reference(), draft.name(), Instant.now(), ComponentStatus.ACTIVE, supplier));
    }

    private Batch createBatch(Component component, String supplierReference, BatchStatus status) {
        return batchRepository.save(new Batch(
                null,
                component,
                "LOT-MP-203001-" + supplierReference.substring(supplierReference.length() - 3),
                supplierReference,
                Instant.parse("2030-12-31T00:00:00Z"),
                Instant.parse("2030-01-01T00:00:00Z"),
                status
        ));
    }

    private Long createTestDeviation(Long batchId, String code, DeviationStatus status, String comment) {
        Deviation deviation = Deviation.createNew(batchId, code, status, comment, batchRepository);
        return deviationRepository.save(deviation).id();
    }

    private Long createTestControl(Long componentId, String name, String method, String unit, double min, double max) {
        Specification spec = Specification.createNew(name, method, unit, min, max, componentId, componentRepository);
        return controlRepository.save(spec).id();
    }

    private Long createTestAnalysisResult(Long batchId, Long specId, Double value, User createdBy) {
        AnalysisResult analysisResult = AnalysisResult.createNew(batchId, specId, value, createdBy, batchRepository);
        return analysisResultRepository.save(analysisResult).id();
    }
}
