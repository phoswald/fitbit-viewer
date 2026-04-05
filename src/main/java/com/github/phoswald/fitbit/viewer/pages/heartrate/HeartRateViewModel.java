package com.github.phoswald.fitbit.viewer.pages.heartrate;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.fitbit.viewer.repository.HeartRateEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record HeartRateViewModel(
        LocalDate begDate,
        LocalDate endDate,
        List<HeartRateEntity> heartRates,
        String errorMessage,
        ZonedDateTime now
) {

    static HeartRateViewModel create(
            LocalDate begDate,
            LocalDate endDate,
            List<HeartRateEntity> heartRates) {
        return new HeartRateViewModelBuilder()
                .begDate(begDate)
                .endDate(endDate)
                .heartRates(heartRates)
                .now(ZonedDateTime.now())
                .build();
    }

    static HeartRateViewModel createError(String errorMessage) {
        return new HeartRateViewModelBuilder()
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
