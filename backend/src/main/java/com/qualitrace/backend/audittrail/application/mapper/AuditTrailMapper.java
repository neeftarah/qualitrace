package com.qualitrace.backend.audittrail.application.mapper;

import com.qualitrace.backend.audittrail.application.dto.AuditTrailResponse;
import com.qualitrace.backend.user.application.dto.UserResponse;
import com.qualitrace.backend.audittrail.domain.model.AuditTrail;
import org.springframework.stereotype.Service;

@Service
public class AuditTrailMapper {

    public AuditTrailResponse toResponse(AuditTrail auditTrail) {
        return new AuditTrailResponse(
            auditTrail.id(),
            new UserResponse(
                    auditTrail.author().id(),
                    auditTrail.author().login(),
                    auditTrail.author().email(),
                    auditTrail.author().firstname(),
                    auditTrail.author().surname(),
                    auditTrail.author().status(),
                    auditTrail.author().roles(),
                    auditTrail.author().createdAt(),
                    auditTrail.author().updatedAt()
            ),
            auditTrail.event(),
            auditTrail.entity_type(),
            auditTrail.entity_id(),
            auditTrail.timestamp(),
            auditTrail.previous_data(),
            auditTrail.changed_data()
        );
    }
}