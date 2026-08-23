package com.qualitrace.backend.shared.infrastructure.audit;

import com.qualitrace.backend.shared.infrastructure.security.QualitracePrincipal;
import org.hibernate.event.spi.*;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.*;

@Component
public class AuditEventListener implements
        PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    private static final Set<String> SENSITIVE_FIELDS = Set.of("password");

    private final JsonMapper jsonMapper;

    public AuditEventListener(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        Map<String, Object> after = sanitize(toMap(event.getPersister(), event.getState()));
        write((EventSource) event.getSession(), "CREATE", entityType(event), entityId(event.getId()), Map.of(), after);
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        Map<String, Object> before = toMap(event.getPersister(), event.getOldState());
        Map<String, Object> after = toMap(event.getPersister(), event.getState());
        Map<String, Object>[] diff = diff(before, after);

        // Si rien n'a été réellement modifié
        if (diff[0].isEmpty()) return;

        write(
            (EventSource) event.getSession(),
            "UPDATE",
            entityType(event),
            entityId(event.getId()),
            sanitize(diff[0]),
            sanitize(diff[1])
        );
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        Map<String, Object> before = sanitize(toMap(event.getPersister(), event.getDeletedState()));
        write((EventSource) event.getSession(), "DELETE", entityType(event), entityId(event.getId()), before, Map.of());
    }

    // false = exécution SYNCHRONE, pendant le flush, dans la transaction en cours — pas après commit
    @Override public boolean requiresPostCommitHandling(EntityPersister persister) { return false; }

    /**
     * Écrit la ligne d'audit en JDBC brut, sur la connexion de la session Hibernate en cours.
     * Bypass volontairement le Persistence Context (pas de session.persist) pour éviter
     * tout problème de ré-entrance de flush, et pour rester dans la même transaction/connexion
     * que l'opération métier : si cet INSERT échoue, toute la transaction est rollback.
     */
    private void write(EventSource session, String event, String entityType, String entityId,
                       Map<String, Object> previous, Map<String, Object> changed) {
        UUID authorId = currentAuthorId();
        String previousJson = jsonMapper.writeValueAsString(previous);
        String changedJson = jsonMapper.writeValueAsString(changed);

        session.doWork(connection -> {
            try (PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO audit_trail (author_id, event, entity_type, entity_id, timestamp, previous_data, changed_data)
                    VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb))
                    """)) {
                if (authorId != null) {
                    ps.setObject(1, authorId);
                } else {
                    ps.setNull(1, Types.OTHER);
                }

                ps.setString(2, event);
                ps.setString(3, entityType);
                ps.setString(4, entityId);
                ps.setTimestamp(5, Timestamp.from(Instant.now()));
                ps.setString(6, previousJson);
                ps.setString(7, changedJson);
                ps.executeUpdate();
            }
        });
    }

    private Map<String, Object> toMap(EntityPersister persister, Object[] state) {
        if (state == null) return Map.of();

        String[] names = persister.getPropertyNames();
        Map<String, Object> map = new LinkedHashMap<>();

        for (int i = 0; i < names.length; i++) {
            map.put(names[i], state[i]);
        }

        return map;
    }

    private Map<String, Object> sanitize(Map<String, Object> data) {
        if (data == null || data.isEmpty()) return Map.of();

        Map<String, Object> copy = new LinkedHashMap<>(data);
        SENSITIVE_FIELDS.forEach(field -> {
            if (copy.containsKey(field)) {
                copy.put(field, "***CENSORED***");
            }
        });

        return copy;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object>[] diff(Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> oldValues = new LinkedHashMap<>();
        Map<String, Object> newValues = new LinkedHashMap<>();
        for (String key : after.keySet()) {
            Object oldVal = before.get(key);
            Object newVal = after.get(key);
            if (!Objects.equals(oldVal, newVal)) {
                oldValues.put(key, oldVal);
                newValues.put(key, newVal);
            }
        }
        return new Map[]{oldValues, newValues};
    }

    private String entityType(AbstractDatabaseOperationEvent event) {
        return event.getEntity().getClass().getSimpleName();
    }

    private String entityId(Object id) {
        return String.valueOf(id);
    }

    private UUID currentAuthorId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof QualitracePrincipal principal) {
            return principal.getId();
        }
        return null; // action système (seed, tâche planifiée) — pas d'auteur humain
    }
}