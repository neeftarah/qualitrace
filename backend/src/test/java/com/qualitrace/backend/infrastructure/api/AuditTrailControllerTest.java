package com.qualitrace.backend.infrastructure.api;

import com.qualitrace.backend.component.domain.model.Component;
import com.qualitrace.backend.supplier.domain.model.Supplier;
import com.qualitrace.backend.user.domain.model.User;
import com.qualitrace.backend.component.domain.repository.ComponentRepository;
import com.qualitrace.backend.supplier.domain.repository.SupplierRepository;
import com.qualitrace.backend.user.domain.repository.UserRepository;
import com.qualitrace.backend.component.domain.type.ComponentType;
import com.qualitrace.backend.user.domain.type.UserRole;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(statements = "INSERT INTO users" +
        "(id, login, password, email, firstname, surname, status, roles, version, created_at)" +
        "VALUES" +
        "(" +
        "    '8b289d81-8824-4554-8f16-7ba3f687abe1'," +
        "    'root'," +
        "    '$2y$10$TmcP7m6NXUz.5xI/tULCmeD3l9GynICd64WnY.5pCbOIN0sDzXb26'," +
        "    'jmoreau.dev+root@gmail.com'," +
        "    'Jérémy'," +
        "    'MOREAU'," +
        "    'ACTIVE'," +
        "    '{ADMIN, SUPPLY, AQ, CQ, PLANNING, PRODUCTION}'," +
        "    0," +
        "    NOW()" +
        ")")
class AuditTrailControllerTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static RestTestClient restClient;

    @Autowired
    private ComponentRepository componentRepository; // le port du domaine, pas le JPA repository directement

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @BeforeAll
    void setUpClient() {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        restClient = RestTestClient.bindTo(mockMvc).build();
    }

    @BeforeEach
    void setUp() {
        authenticateAs("root");
        User user = userRepository.save(User.createNew("audituser1", "hashed-pwd", "audit1@test.com", "Audit", "One", Set.of(UserRole.ADMIN)));
        User archivedUser = userRepository.save(user.archive());
        userRepository.save(archivedUser.reactivate());

        authenticateAs("audituser1");
        Supplier savedSupplier = supplierRepository.save(Supplier.createNew("SUP-00001", "First Supplier", "Supplier address"));
        Supplier updatedSupplier = supplierRepository.save(savedSupplier.update("Audit2", "Other Adress"));
        componentRepository.save(Component.createNew(ComponentType.RAW_MATERIAL, "CMP-20260822-01", "TestComponent", updatedSupplier));
    }

    @Test
    public void listAll() {
        // Aucun filtre
        restClient.get().uri("/api/v1/audit_trail")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.page.totalElements").isEqualTo(6)
                .jsonPath("$._embedded.audit_trails").isArray()
                .jsonPath("$._embedded.audit_trails.length()").isEqualTo(6);

    }

    @Test
    public void listByAuthor() {
        // Filtre sur author_id
        restClient.get().uri("/api/v1/audit_trail?author_id=8b289d81-8824-4554-8f16-7ba3f687abe1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.page.totalElements").isEqualTo(3);

    }

    @Test
    public void listByEvent() {
        // Filtre sur event
        restClient.get().uri("/api/v1/audit_trail?event=CREATE")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.page.totalElements").isEqualTo(3);

    }

    @Test
    public void listByEntity() {
        // Filtre sur entity_type et entity_id
        restClient.get().uri(uriBuilder -> uriBuilder
                        .path("/api/v1/audit_trail")
                        .queryParam("entity_type", "SupplierEntity")
                        .queryParam("entity_id", "1")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.page.totalElements").isEqualTo(2);
    }

    @Test
    public void listByContent() {
        // Filtre sur entity_type et entity_id
        restClient.get().uri("/api/v1/audit_trail?content=CMP-20260822-01")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.page.totalElements").isEqualTo(1);
    }

    @Test
    public void listByDateFrom() {
        LocalDate hier = LocalDate.now().minusDays(1);
        LocalDate demain = LocalDate.now().plusDays(1);

        // Filtre sur entity_type et entity_id
        restClient.get().uri("/api/v1/audit_trail?fromDate=" + hier)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.page.totalElements").isEqualTo(6);

        // Filtre sur entity_type et entity_id
        restClient.get().uri("/api/v1/audit_trail?fromDate=" + demain)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.page.totalElements").isEqualTo(0);
    }

    @Test
    public void listByDateTo() {
        LocalDate hier = LocalDate.now().minusDays(1);
        LocalDate demain = LocalDate.now().plusDays(1);

        // Filtre sur entity_type et entity_id
        restClient.get().uri("/api/v1/audit_trail?toDate=" + hier)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.page.totalElements").isEqualTo(0);

        // Filtre sur entity_type et entity_id
        restClient.get().uri("/api/v1/audit_trail?toDate=" + demain)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody()
                .jsonPath("$.page.totalElements").isEqualTo(6);
    }

    private void authenticateAs(String login) {
        UserDetails principal = userDetailsService.loadUserByUsername(login);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        TestSecurityContextHolder.setContext(context);
    }
}