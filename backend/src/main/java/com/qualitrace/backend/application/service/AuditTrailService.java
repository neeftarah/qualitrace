package com.qualitrace.backend.application.service;

import com.qualitrace.backend.application.dto.AuditTrailResponse;
import com.qualitrace.backend.application.mapper.AuditTrailMapper;
import com.qualitrace.backend.domain.model.AuditTrailFilter;
import com.qualitrace.backend.domain.model.PageQuery;
import com.qualitrace.backend.domain.model.PageResult;
import com.qualitrace.backend.domain.repository.AuditTrailRepository;
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