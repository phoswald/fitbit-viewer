package com.github.phoswald.fitbit.viewer.pages.azm;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.ActiveZoneMinutesEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record ActiveZoneMinutesViewModel(
        DateRangeViewModel dateRange,
        Collection<ActiveZoneMinutesEntity> azms,
        String errorMessage,
        ZonedDateTime now
) {

    static ActiveZoneMinutesViewModel create(
            DateRangeViewModel dateRange,
            Collection<ActiveZoneMinutesEntity> azms) {
        return new ActiveZoneMinutesViewModelBuilder()
                .dateRange(dateRange)
                .azms(azms)
                .now(ZonedDateTime.now())
                .build();
    }

    static ActiveZoneMinutesViewModel createError(String errorMessage) {
        return new ActiveZoneMinutesViewModelBuilder()
                .dateRange(new DateRangeViewModelBuilder().build())
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public List<LocalDate> dates() {
        return azms.stream().map(ActiveZoneMinutesEntity::getDate).toList();
    }

    public List<Integer> totalAzm() {
        return azms.stream().map(ActiveZoneMinutesEntity::getTotalAzm).toList();
    }

    public List<Integer> fatBurnAzm() {
        return azms.stream().map(ActiveZoneMinutesEntity::getFatBurnAzm).toList();
    }

    public List<Integer> cardioAzm() {
        return azms.stream().map(ActiveZoneMinutesEntity::getCardioAzm).toList();
    }

    public List<Integer> peakAzm() {
        return azms.stream().map(ActiveZoneMinutesEntity::getPeakAzm).toList();
    }
}
