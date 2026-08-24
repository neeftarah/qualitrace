package com.qualitrace.backend.analysisresult.infrastructure.persistence.adapter;

import com.qualitrace.backend.analysisresult.domain.model.AnalysisResult;
import com.qualitrace.backend.analysisresult.domain.repository.AnalysisResultRepository;
import com.qualitrace.backend.analysisresult.infrastructure.persistence.entity.AnalysisResultEntity;
import com.qualitrace.backend.analysisresult.infrastructure.persistence.repository.AnalysisResultJpaRepository;
import com.qualitrace.backend.user.domain.model.User;
import com.qualitrace.backend.user.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AnalysisResultRepositoryAdapter implements AnalysisResultRepository {

    private final AnalysisResultJpaRepository jpaRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    public AnalysisResultRepositoryAdapter(AnalysisResultJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<AnalysisResult> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public AnalysisResult save(AnalysisResult result) {
        AnalysisResultEntity entity = result.id() != null
                ? jpaRepository.findById(result.id())
                .map(existing -> applyChanges(existing, result))
                .orElseGet(() -> toNewEntity(result))
                : toNewEntity(result);
        AnalysisResultEntity saved = jpaRepository.saveAndFlush(entity);

        return toDomain(saved);
    }

    @Override
    public List<AnalysisResult> findAllByBatchId(Long id) {
        return jpaRepository.findAllByBatchId(id)
                .stream().map(this::toDomain).toList();
    }

    private AnalysisResultEntity applyChanges(AnalysisResultEntity entity, AnalysisResult analysisResult) {
        return entity;
    }

    /**
     * Construit une entité du domaine à partir d'une entité JPA
     *
     * @param entity Objet JPA
     * @return Objet du domaine (AnalysisResult)
     */
    private AnalysisResult toDomain(AnalysisResultEntity entity) {
        return new AnalysisResult(
                entity.getId(),
                entity.getBatchId(),
                entity.getSpecificationId(),
                entity.getValue(),
                entity.getCreatedAt(),
                entity.getCreatedBy() != null ? new User(
                        entity.getCreatedBy().getId(),
                        entity.getCreatedBy().getLogin(),
                        entity.getCreatedBy().getPassword(),
                        entity.getCreatedBy().getEmail(),
                        entity.getCreatedBy().getFirstname(),
                        entity.getCreatedBy().getSurname(),
                        entity.getCreatedBy().getStatus(),
                        entity.getCreatedBy().getVersion(),
                        entity.getCreatedBy().getCreatedAt(),
                        entity.getCreatedBy().getUpdatedAt(),
                        entity.getCreatedBy().getRoles()
                ) : null
        );
    }

    private AnalysisResultEntity toNewEntity(AnalysisResult analysisResult) {
        UserEntity createdBy = analysisResult.createdBy() != null && analysisResult.createdBy().id() != null
                ? entityManager.getReference(UserEntity.class, analysisResult.createdBy().id())
                : null;

        return new AnalysisResultEntity(
                analysisResult.id(),
                analysisResult.batchId(),
                analysisResult.specificationId(),
                analysisResult.value(),
                analysisResult.createdAt(),
                createdBy
        );
    }
}
