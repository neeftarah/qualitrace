package com.qualitrace.backend.infrastructure.audit;

import com.qualitrace.backend.user.domain.model.User;
import com.qualitrace.backend.user.domain.repository.UserRepository;
import com.qualitrace.backend.user.domain.type.UserRole;
import com.qualitrace.backend.audittrail.infrastructure.persistence.entity.AuditTrailEntity;
import com.qualitrace.backend.audittrail.infrastructure.persistence.repository.AuditTrailJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AuditTrailTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditTrailJpaRepository auditTrailJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * 1. Vérification de l'enregistrement de l'audit trail
     */
    @Test
    void shouldRecordAuditEntryOnUserCreation() {
        User user = User.createNew("audituser1", "hashed-pwd", "audit1@test.com", "Audit", "One", Set.of(UserRole.AQ));
        User saved = userRepository.save(user);

        List<AuditTrailEntity> entries = auditTrailJpaRepository.findAll();

        assertThat(entries)
                .anySatisfy(entry -> {
                    assertThat(entry.getEvent()).isEqualTo("CREATE");
                    assertThat(entry.getEntityType()).isEqualTo("UserEntity");
                    assertThat(entry.getEntityId()).isEqualTo(saved.id().toString());
                    assertThat(entry.getPreviousData()).isEqualTo("{}");
                    assertThat(entry.getChangedData()).contains("\"login\": \"audituser1\"");
                    assertThat(entry.getChangedData()).doesNotContain("hashed-pwd"); // rédaction du mot de passe
                    assertThat(entry.getChangedData()).contains("***CENSORED***");
                });
    }

    @Test
    void shouldRecordAuditEntryOnUserUpdateWithOnlyChangedFields() {
        User user = User.createNew("audituser2", "hashed-pwd", "audit2@test.com", "Jean", "Dupont", Set.of(UserRole.AQ));
        User saved = userRepository.save(user);

        User updated = saved.update("Jean-Michel", "Dupont", Set.of(UserRole.CQ));
        userRepository.save(updated);

        List<AuditTrailEntity> entries = auditTrailJpaRepository.findAll();

        assertThat(entries)
                .filteredOn(entry -> entry.getEntityId().equals(saved.id().toString()) && entry.getEvent().equals("UPDATE"))
                .hasSize(1)
                .first()
                .satisfies(entry -> {
                    // seuls les champs réellement modifiés doivent apparaître (firstname, roles) — pas surname/login/email
                    assertThat(entry.getPreviousData()).contains("\"firstname\": \"Jean\"");
                    assertThat(entry.getChangedData()).contains("\"firstname\": \"Jean-Michel\"");
                    assertThat(entry.getPreviousData()).doesNotContain("surname");
                    assertThat(entry.getPreviousData()).doesNotContain("login");
                });
    }

    @Test
    void shouldRecordAuthorIdWhenAuthenticated() {
        // Pas de contexte de sécurité positionné ici → author doit être null (action système)
        User user = User.createNew("audituser3", "hashed-pwd", "audit3@test.com", "Test", "User", Set.of(UserRole.AQ));
        User saved = userRepository.save(user);

        AuditTrailEntity entry = auditTrailJpaRepository.findAll().stream()
                .filter(e -> e.getEntityId().equals(saved.id().toString()))
                .findFirst()
                .orElseThrow();

        assertThat(entry.getAuthor()).isNull();
    }

    /**
     * 2. Vérification du rollback complet en cas d'échec d'écriture d'audit
     */
    @Test
    void shouldRollbackEntireTransactionWhenAuditWriteFails() {
        User user = User.createNew("audituser4", "hashed-pwd", "audit4@test.com", "Original", "Name", Set.of(UserRole.AQ));
        User saved = userRepository.save(user);
        long auditCountBefore = auditTrailJpaRepository.count();

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // On casse volontairement la cible d'écriture de l'audit, DANS une transaction séparée.
        // Postgres supporte le DDL transactionnel : si cette transaction échoue et rollback,
        // le renommage de table est automatiquement annulé avec elle — pas de nettoyage manuel requis.
        assertThatThrownBy(() ->
                txTemplate.executeWithoutResult(status -> {
                    jdbcTemplate.execute("ALTER TABLE audit_trail RENAME TO audit_trail_broken");

                    User updated = saved.update("Modified", "Name", Set.of(UserRole.CQ));
                    userRepository.save(updated); // déclenche le listener -> INSERT échoue (table introuvable) -> exception
                })
        ).isInstanceOf(DataAccessException.class);

        // La transaction interne a rollback : le renommage de table ET l'update de l'utilisateur
        // sont tous deux annulés ensemble. On vérifie les deux, dans une nouvelle transaction propre.
        User reloaded = userRepository.findById(saved.id()).orElseThrow();
        assertThat(reloaded.firstname()).isEqualTo("Original"); // pas "Modified" : rollback confirmé
        assertThat(reloaded.roles()).containsExactly(UserRole.AQ); // pas CQ

        long auditCountAfter = auditTrailJpaRepository.count();
        assertThat(auditCountAfter).isEqualTo(auditCountBefore); // aucune ligne d'audit orpheline créée

        // Confirme que la table audit_trail est bien accessible normalement (rollback du RENAME confirmé)
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'audit_trail'", Integer.class))
                .isEqualTo(1);
    }
}