package com.qualitrace.backend.analysisresult.application.assembler;

import com.qualitrace.backend.analysisresult.application.dto.AnalysisResultResponse;
import com.qualitrace.backend.analysisresult.application.dto.AnalysisResultUpdateRequest;
import com.qualitrace.backend.analysisresult.infrastructure.api.AnalysisResultController;
import org.jspecify.annotations.NullMarked;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Convertit un {@link AnalysisResultResponse} en {@link EntityModel} enrichi de liens HATEOAS.
 */
@Component
@NullMarked
public class AnalysisResultModelAssembler implements RepresentationModelAssembler<AnalysisResultResponse, EntityModel<AnalysisResultResponse>> {

    @Override
    public EntityModel<AnalysisResultResponse> toModel(AnalysisResultResponse analysisResult) {

        EntityModel<AnalysisResultResponse> model = EntityModel.of(
                analysisResult,
                linkTo(methodOn(AnalysisResultController.class)
                        .list(analysisResult.batchId()))
                        .withSelfRel()
        );

        model.add(linkTo(methodOn(AnalysisResultController.class).update(
                analysisResult.batchId(),
                analysisResult.id(),
                new AnalysisResultUpdateRequest(0.0)
        )).withRel("update"));

        return model;
    }
}
