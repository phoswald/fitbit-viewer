package com.github.phoswald.fitbit.viewer.pages.activities;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;

import com.github.phoswald.fitbit.viewer.repository.ActivityEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActivityViewModel(
        LocalDate begDate,
        int limit,
        Collection<ActivityEntity> activities,
        String errorMessage,
        ZonedDateTime now
) {

    static ActivityViewModel create(
            LocalDate begDate,
            int limit,
            Collection<ActivityEntity> activities) {
        return new ActivityViewModelBuilder()
                .begDate(begDate)
                .limit(limit)
                .activities(activities)
                .now(ZonedDateTime.now())
                .build();
    }

    static ActivityViewModel createError(String errorMessage) {
        return new ActivityViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }
}
