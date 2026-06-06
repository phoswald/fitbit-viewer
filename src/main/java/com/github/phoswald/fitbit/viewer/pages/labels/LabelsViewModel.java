package com.github.phoswald.fitbit.viewer.pages.labels;

import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.fitbit.viewer.repository.LabelSummaryEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record LabelsViewModel(
        List<LabelSummaryEntity> labels,
        String userId,
        String errorMessage,
        ZonedDateTime now
) {

    static LabelsViewModel create(List<LabelSummaryEntity> labels, String userId) {
        return new LabelsViewModelBuilder()
                .labels(labels)
                .userId(userId)
                .now(ZonedDateTime.now())
                .build();
    }

    static LabelsViewModel createError(String errorMessage) {
        return new LabelsViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }
}
