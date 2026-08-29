package com.qualitrace.backend.analysisresult.domain.exception;

import com.qualitrace.backend.shared.domain.exception.DomainNotFoundException;

public class AnalysisResultNotFoundException extends DomainNotFoundException {
    public AnalysisResultNotFoundException(Long id) {
        super("Analysis result not found: " + id);
    }
}