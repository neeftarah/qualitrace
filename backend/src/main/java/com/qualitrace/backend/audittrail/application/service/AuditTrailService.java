package com.qualitrace.backend.audittrail.application.service;

import com.qualitrace.backend.audittrail.application.dto.AuditTrailResponse;
import com.qualitrace.backend.audittrail.application.mapper.AuditTrailMapper;
import com.qualitrace.backend.audittrail.domain.model.AuditTrailFilter;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;
import com.qualitrace.backend.audittrail.domain.repository.AuditTrailRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditTrailService {
    private final AuditTrailRepository auditTrailRepository;
    private final AuditTrailMapper auditTrailMapper;

    public AuditTrailService(AuditTrailRepository auditTrailRepository, AuditTrailMapper auditTrailMapper) {
        this.auditTrailRepository = auditTrailRepository;
        this.auditTrailMapper = auditTrailMapper;
    }

    public PageResult<AuditTrailResponse> getAll(PageQuery pageQuery, AuditTrailFilter filter) {
        return auditTrailRepository.findAll(pageQuery, filter)
                .map(auditTrailMapper::toResponse);
    }
}