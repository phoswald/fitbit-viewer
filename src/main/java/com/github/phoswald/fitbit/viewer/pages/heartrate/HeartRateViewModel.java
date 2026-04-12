package com.github.phoswald.fitbit.viewer.pages.heartrate;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.HeartRateEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record HeartRateViewModel(
        DateRangeViewModel dateRange,
        Collection<HeartRateEntity> heartRates,
        String errorMessage,
        ZonedDateTime now
) {

    static HeartRateViewModel create(
            DateRangeViewModel dateRange,
            Collection<HeartRateEntity> heartRates) {
        return new HeartRateViewModelBuilder()
                .dateRange(dateRange)
                .heartRates(heartRates)
                .now(ZonedDateTime.now())
                .build();
    }

    static HeartRateViewModel createError(String errorMessage) {
        return new HeartRateViewModelBuilder()
                .dateRange(new DateRangeViewModelBuilder().build())
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public List<LocalDate> heartRateDates() {
        return heartRates.stream().map(HeartRateEntity::getDate).toList();
    }

    public List<Integer> restingHeartRates() {
        return heartRates.stream().map(HeartRateEntity::getRestingHeartRate).toList();
    }
}
