package com.qualitrace.backend.infrastructure.api;

import com.qualitrace.backend.batch.domain.model.Batch;
import com.qualitrace.backend.batch.domain.type.BatchStatus;
import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.component.domain.type.ComponentStatus;
import com.qualitrace.backend.component.domain.type.ComponentType;
import com.qualitrace.backend.controls.domain.model.ControlRangeSpecification;
import com.qualitrace.backend.controls.domain.repository.ControlRangeSpecificationRepository;
import com.qualitrace.backend.deviation.domain.model.Deviation;
import com.qualitrace.backend.deviation.domain.repository.DeviationRepository;
import com.qualitrace.backend.deviation.domain.type.DeviationStatus;
import com.qualitrace.backend.supplier.domain.model.Supplier;
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

import java.time.Instant;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(statements = "INSERT INTO suppliers (id, code, name, address, status) VALUES (123456789, 'SUPP01', 'Fournisseur Test', '123 Main Street', 'ACTIVE') ON CONFLICT DO NOTHING;")
@Sql(statements = "INSERT INTO components (id, type, reference, name, status, available_from, supplier_id) VALUES (123456789, 'RAW_MATERIAL', 'CMP-001', 'Test Component 1', 'ACTIVE', '2026-08-26 20:02:42.768537 +00:00', 123456789) ON CONFLICT DO NOTHING;")
@Sql(statements = "INSERT INTO batches (id, component_id, supplier_id, supplier_reference_number, internal_reference_number, expiry_date, reception_date, status, version) VALUES (123456789, 123456789, 123456789, 'REF-SUPP-LOT-01', 'REF-INT-LOT-01', '2028-08-26 20:02:42.768537 +00:00', '2026-08-26 20:02:42.768537 +00:00', 'QUARANTINE', 1) ON CONFLICT DO NOTHING;")
class DeviationControllerTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static RestTestClient restClient;

    @Autowired
    private DeviationRepository deviationRepository;
//
//    @Autowired
//    private ControlRangeSpecificationRepository controlRepository;

    private static final Batch TEST_BATCH = new Batch(
            123456789L,
            new Component(
                    123456789L,
                    ComponentType.RAW_MATERIAL,
                    "COMP-001",
                    "Test Component 1",
                    Instant.now(),
                    ComponentStatus.ACTIVE,
                    new Supplier(
                            123456789L,
                            "SUP001",
                            "Supplier 1",
                            "123 Main St, City, Country",
                            SupplierStatus.ACTIVE
                    )
            ),
            "LOT-2026-001",
            "SUP-LOT-999",
            Instant.now().plusSeconds(36000),
            Instant.now(),
            BatchStatus.QUARANTINE
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
    @WithMockUser(username = "aq", roles = "AQ")
    public void create() {
        restClient.post().uri("/api/v1/batches/123456789/deviations")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                          "code": "DEV-005",
                          "status": "OPENED",
                          "comment": "Test deviation"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.code").isEqualTo("DEV-005")
                .jsonPath("$.status").isEqualTo("OPENED")
                .jsonPath("$.comment").isEqualTo("Test deviation")
                .jsonPath("$.batchId").isEqualTo(123456789);
    }

    @Test
    @WithMockUser(username = "aq", roles = "AQ")
    public void createWithUnexistingBatch() {
        restClient.post().uri("/api/v1/batches/987654321/deviations")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                          "code": "DEV-005",
                          "status": "OPENED",
                          "comment": "Test deviation"
                        }
                        """)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "aq", roles = "AQ")
    public void createWithoutCode() {
        restClient.post().uri("/api/v1/batches/123456789/deviations")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                          "status": "OPENED",
                          "comment": "Test deviation"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "aq", roles = "AQ")
    public void createWithoutStatusOrCommentIsOK() {
        restClient.post().uri("/api/v1/batches/123456789/deviations")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                          "code": "DEV-005"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.code").isEqualTo("DEV-005")
                .jsonPath("$.status").isEqualTo("OPENED")
                .jsonPath("$.comment").isEqualTo("")
                .jsonPath("$.batchId").isEqualTo(123456789);
    }

    @Test
    @WithMockUser(username = "aq", roles = "AQ")
    public void update() {
        Long deviationId = createTestDeviation(123456789L, "DEV-005", DeviationStatus.OPENED, "");
        restClient.put().uri("/api/v1/batches/123456789/deviations/" + deviationId)
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                          "comment": "Updated comment"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.code").isEqualTo("DEV-005")
                .jsonPath("$.status").isEqualTo("OPENED")
                .jsonPath("$.comment").isEqualTo("Updated comment")
                .jsonPath("$.batchId").isEqualTo(123456789);
    }


    @Test
    @WithMockUser(username = "aq", roles = "AQ")
    public void updateUnexistingDeviation() {
        restClient.put().uri("/api/v1/batches/123456789/deviations/98765321")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                          "comment": "Updated comment"
                        }
                        """)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "aq", roles = "AQ")
    public void close() {
        Long deviationId = createTestDeviation(123456789L, "DEV-005", DeviationStatus.OPENED, "");
        restClient.patch().uri("/api/v1/batches/123456789/deviations/" + deviationId + "/close")
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.code").isEqualTo("DEV-005")
                .jsonPath("$.status").isEqualTo("CLOSED")
                .jsonPath("$.comment").isEqualTo("")
                .jsonPath("$.batchId").isEqualTo(123456789);
    }

    @Test
    @WithMockUser(username = "aq", roles = "AQ")
    public void open() {
        Long deviationId = createTestDeviation(123456789L, "DEV-006", DeviationStatus.CLOSED, "");
        restClient.patch().uri("/api/v1/batches/123456789/deviations/" + deviationId + "/open")
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.code").isEqualTo("DEV-006")
                .jsonPath("$.status").isEqualTo("OPENED")
                .jsonPath("$.comment").isEqualTo("")
                .jsonPath("$.batchId").isEqualTo(123456789);
    }

    @Test
    @WithMockUser(username = "aq", roles = "AQ")
    public void errorIfAlreadyOpen() {
        Long deviationId = createTestDeviation(123456789L, "DEV-007", DeviationStatus.OPENED, "");
        restClient.patch().uri("/api/v1/batches/123456789/deviations/" + deviationId + "/open")
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "aq", roles = "AQ")
    public void errorIfAlreadyClosed() {
        Long deviationId = createTestDeviation(123456789L, "DEV-008", DeviationStatus.CLOSED, "");
        restClient.patch().uri("/api/v1/batches/123456789/deviations/" + deviationId + "/close")
                .contentType(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    @WithMockUser(username = "admin", roles = "admin")
    public void createWithBadRole() {
        restClient.post().uri("/api/v1/batches/123456789/deviations")
                .contentType(MediaTypes.HAL_JSON)
                .body("""
                        {
                          "code": "DEV-005",
                          "status": "OPENED",
                          "comment": "Test deviation"
                        }
                        """)
                .exchange()
                .expectStatus().isForbidden()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    /**
     * Test la liste des composants.
     */
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void list() {
        createTestDeviation(123456789L, "DEV-008", DeviationStatus.CLOSED, "");
        createTestDeviation(123456789L, "DEV-009", DeviationStatus.OPENED, "");
        createTestDeviation(123456789L, "DEV-010", DeviationStatus.OPENED, "");

        restClient.get().uri("/api/v1/batches/123456789/deviations")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$._embedded.deviations").isArray()
                .jsonPath("$._embedded.deviations.length()").isEqualTo(3)
                .jsonPath("$._embedded.deviations[?(@.code=='DEV-008')]").exists()
                .jsonPath("$._embedded.deviations[?(@.code=='DEV-009')]").exists()
                .jsonPath("$._embedded.deviations[?(@.code=='DEV-010')]").exists();
    }

    private Long createTestDeviation(Long batchId, String code, DeviationStatus status, String comment) {
        Deviation deviation = Deviation.createNew(batchId, code, status, comment);
        return deviationRepository.save(deviation).id();
    }
}
