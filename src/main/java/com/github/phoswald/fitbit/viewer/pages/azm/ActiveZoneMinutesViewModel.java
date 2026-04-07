package com.github.phoswald.fitbit.viewer.pages.azm;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.repository.ActiveZoneMinutesEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActiveZoneMinutesViewModel(
        LocalDate begDate,
        LocalDate endDate,
        Collection<ActiveZoneMinutesEntity> entries,
        String errorMessage,
        ZonedDateTime now
) {

    static ActiveZoneMinutesViewModel create(
            LocalDate begDate,
            LocalDate endDate,
            Collection<ActiveZoneMinutesEntity> entries) {
        return new ActiveZoneMinutesViewModelBuilder()
                .begDate(begDate)
                .endDate(endDate)
                .entries(entries)
                .now(ZonedDateTime.now())
                .build();
    }

    static ActiveZoneMinutesViewModel createError(String errorMessage) {
        return new ActiveZoneMinutesViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public List<LocalDate> dates() {
        return entries.stream().map(ActiveZoneMinutesEntity::getDate).toList();
    }

    public List<Integer> totalAzm() {
        return entries.stream().map(ActiveZoneMinutesEntity::getTotalAzm).toList();
    }

    public List<Integer> fatBurnAzm() {
        return entries.stream().map(ActiveZoneMinutesEntity::getFatBurnAzm).toList();
    }

    public List<Integer> cardioAzm() {
        return entries.stream().map(ActiveZoneMinutesEntity::getCardioAzm).toList();
    }

    public List<Integer> peakAzm() {
        return entries.stream().map(ActiveZoneMinutesEntity::getPeakAzm).toList();
    }
}
