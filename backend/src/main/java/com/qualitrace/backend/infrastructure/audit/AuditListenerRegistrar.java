package com.qualitrace.backend.infrastructure.audit;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
public class AuditListenerRegistrar {

    private final EntityManagerFactory entityManagerFactory;
    private final AuditEventListener auditEventListener;

    public AuditListenerRegistrar(EntityManagerFactory entityManagerFactory, AuditEventListener auditEventListener) {
        this.entityManagerFactory = entityManagerFactory;
        this.auditEventListener = auditEventListener;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void registerListener() {
        SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        assert registry != null;
        registry.appendListeners(EventType.POST_INSERT, auditEventListener);
        registry.appendListeners(EventType.POST_UPDATE, auditEventListener);
        registry.appendListeners(EventType.POST_DELETE, auditEventListener);
    }
}