package com.qualitrace.backend.domain.repository;

import com.qualitrace.backend.domain.model.AuditTrail;
import com.qualitrace.backend.domain.model.AuditTrailFilter;
import com.qualitrace.backend.domain.model.PageQuery;
import com.qualitrace.backend.domain.model.PageResult;

public interface AuditTrailRepository {
    PageResult<AuditTrail> findAll(PageQuery pageQuery, AuditTrailFilter filter);
}
