package com.qualitrace.backend.audittrail.domain.repository;

import com.qualitrace.backend.audittrail.domain.model.AuditTrail;
import com.qualitrace.backend.audittrail.domain.model.AuditTrailFilter;
import com.qualitrace.backend.shared.domain.model.PageQuery;
import com.qualitrace.backend.shared.domain.model.PageResult;

public interface AuditTrailRepository {
    PageResult<AuditTrail> findAll(PageQuery pageQuery, AuditTrailFilter filter);
}
