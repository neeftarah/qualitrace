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
import com.qualitrace.backend.supplier.domain.model.Supplier;
import com.qualitrace.backend.supplier.domain.repository.SupplierRepository;
import com.qualitrace.backend.user.domain.model.User;
import com.qualitrace.backend.user.domain.repository.UserRepository;
import com.qualitrace.backend.user.domain.type.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.TestSecurityContextHolder;
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
class AnalysisResultControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    private static final Long BATCH_ID = 123456789L;

    @Autowired private WebApplicationContext webApplicationContext;
    @Autowired private UserRepository userRepository;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private BatchRepository batchRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ComponentRepository componentRepository;
    @Autowired private SpecificationRepository controlRepository;
    @Autowired private AnalysisResultRepository analysisResultRepository;

    private static RestTestClient restClient;
    private User cqUser;
    private Long specificationId;

    @BeforeAll
    void setUpClient() {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity()).build();
        restClient = RestTestClient.bindTo(mockMvc).build();
    }

    @BeforeEach
    void setUpUser() {
        cqUser = userRepository.save(User.createNew(
                "cq-analysis", "password", "cq-analysis@example.com", "Control", "Quality", Set.of(UserRole.CQ)));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
        TestSecurityContextHolder.clearContext();
    }

    @Test
    void createShouldStoreAuthenticatedUserAsCreatedBy() {
        Batch batch = createBatch();
        authenticateAs("cq-analysis");

        restClient.post().uri("/api/v1/batches/{batchId}/analysis", batch.id())
                .contentType(MediaTypes.HAL_JSON)
                .body(createPayload(7.5))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectHeader().exists("Location")
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.batchId").isEqualTo(batch.id())
                .jsonPath("$.specificationId").isEqualTo(specificationId)
                .jsonPath("$.value").isEqualTo(7.5)
                .jsonPath("$.createdBy.id").isEqualTo(cqUser.id().toString())
                .jsonPath("$.createdBy.login").isEqualTo("cq-analysis");
    }

    @Test
    void listShouldReturnResultsForBatch() {
        Batch batch = createBatch();
        analysisResultRepository.save(new AnalysisResult(
                null, batch.id(), specificationId, 7.5, Instant.now(), cqUser));
        authenticateAs("cq-analysis");

        restClient.get().uri("/api/v1/batches/{batchId}/analysis", batch.id())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$._embedded.analysisResults").isArray()
                .jsonPath("$._embedded.analysisResults.length()").isEqualTo(1)
                .jsonPath("$._embedded.analysisResults[0].createdBy.login").isEqualTo("cq-analysis");
    }

    @Test
    void updateShouldChangeValue() {
        Batch batch = createBatch();
        AnalysisResult result = analysisResultRepository.save(new AnalysisResult(
                null, batch.id(), specificationId, 7.5, Instant.now(), cqUser));
        authenticateAs("cq-analysis");

        restClient.put().uri("/api/v1/batches/{batchId}/analysis/{id}", batch.id(), result.id())
                .contentType(MediaTypes.HAL_JSON)
                .body("{\"value\": 8.25}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(result.id())
                .jsonPath("$.value").isEqualTo(8.25)
                .jsonPath("$.createdBy.login").isEqualTo("cq-analysis");
    }

    @Test
    void updateUnknownBatchShouldReturn404() {
        authenticateAs("cq-analysis");

        restClient.put().uri("/api/v1/batches/{batchId}/analysis/{id}", 999999L, 999999L)
                .contentType(MediaTypes.HAL_JSON)
                .body(createPayload(7.5))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void updateUnknownIdShouldReturn404() {
        Batch batch = createBatch();
        authenticateAs("cq-analysis");

        restClient.put().uri("/api/v1/batches/{batchId}/analysis/{id}", batch.id(), 999999L)
                .contentType(MediaTypes.HAL_JSON)
                .body(createPayload(7.5))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void createUnknownBatchShouldReturn404() {
        authenticateAs("cq-analysis");

        restClient.post().uri("/api/v1/batches/{batchId}/analysis", 999999L)
                .contentType(MediaTypes.HAL_JSON)
                .body(createPayload(7.5))
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void createWithoutValueShouldReturn400() {
        Batch batch = createBatch();
        authenticateAs("cq-analysis");

        restClient.post().uri("/api/v1/batches/{batchId}/analysis", batch.id())
                .contentType(MediaTypes.HAL_JSON)
                .body("{\"specificationId\": " + specificationId + "}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void endpointsShouldReturn401WhenUnauthenticated() {
        restClient.get().uri("/api/v1/batches/{batchId}/analysis", BATCH_ID)
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void createShouldReturn403ForAuthenticatedNonCQUser() {
        User supply = userRepository.save(User.createNew(
                "supply-analysis", "password", "supply-analysis@example.com", "Supply", "User", Set.of(UserRole.SUPPLY)));
        authenticateAs("supply-analysis");
        Batch batch = createBatch();

        restClient.post().uri("/api/v1/batches/{batchId}/analysis", batch.id())
                .contentType(MediaTypes.HAL_JSON)
                .body(createPayload(7.5))
                .exchange().expectStatus().isForbidden();
    }

    private Batch createBatch() {
        Supplier supplier = supplierRepository.save(Supplier.createNew("SUP-ANALYSIS", "Analysis Supplier", "Test address"));
        Component component = componentRepository.save(Component.createNew(ComponentType.RAW_MATERIAL, "CMP-ANALYSIS", "Analysis Component", supplier));
        component = componentRepository.save(new Component(component.id(), component.type(), component.reference(), component.name(), Instant.now(), ComponentStatus.ACTIVE, supplier));
        specificationId = controlRepository.save(Specification.createNew("pH", "Titration", "pH", 6.0, 8.0, component.id(), componentRepository)).id();
        return batchRepository.save(new Batch(null, component, "LOT-ANALYSIS", "SUP-ANALYSIS-LOT", Instant.parse("2030-12-31T00:00:00Z"), Instant.now(), BatchStatus.QUARANTINE));
    }

    private String createPayload(double value) {
        return "{\"specificationId\": %d, \"value\": %s}".formatted(specificationId, value);
    }

    private void authenticateAs(String login) {
        UserDetails principal = userDetailsService.loadUserByUsername(login);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        TestSecurityContextHolder.setContext(context);
    }
}
