package com.github.phoswald.fitbit.viewer.pages.azm;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.fitbit.viewer.repository.AzmEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record AzmViewModel(
        LocalDate begDate,
        LocalDate endDate,
        List<AzmEntity> entries,
        String errorMessage,
        ZonedDateTime now
) {

    static AzmViewModel create(
            LocalDate begDate,
            LocalDate endDate,
            List<AzmEntity> entries) {
        return new AzmViewModelBuilder()
                .begDate(begDate)
                .endDate(endDate)
                .entries(entries)
                .now(ZonedDateTime.now())
                .build();
    }

    static AzmViewModel createError(String errorMessage) {
        return new AzmViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public List<LocalDate> dates() {
        return entries.stream().map(AzmEntity::getDate).toList();
    }

    public List<Integer> activeZoneMinutes() {
        return entries.stream().map(AzmEntity::getActiveZoneMinutes).toList();
    }

    public List<Integer> fatBurnActiveZoneMinutes() {
        return entries.stream().map(AzmEntity::getFatBurnActiveZoneMinutes).toList();
    }

    public List<Integer> cardioActiveZoneMinutes() {
        return entries.stream().map(AzmEntity::getCardioActiveZoneMinutes).toList();
    }

    public List<Integer> peakActiveZoneMinutes() {
        return entries.stream().map(AzmEntity::getPeakActiveZoneMinutes).toList();
    }
}
