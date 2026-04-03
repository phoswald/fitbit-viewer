package com.github.phoswald.fitbit.viewer.heartrate;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record HeartRateViewModel(
        LocalDate begDate,
        LocalDate endDate,
        List<HeartRateEntry> heartRates,
        String errorMessage,
        ZonedDateTime now
) {
    record HeartRateEntry(LocalDate date, Integer restingHeartRate) { }

    static HeartRateViewModel create(
            LocalDate begDate,
            LocalDate endDate,
            List<HeartRateApiClient.HeartRateEntry> entries) {
        return new HeartRateViewModelBuilder()
                .begDate(begDate)
                .endDate(endDate)
                .heartRates(entries == null ? null : entries.stream().map(HeartRateViewModel::createEntry).toList())
                .now(ZonedDateTime.now())
                .build();
    }

    static HeartRateViewModel createError(String errorMessage) {
        return new HeartRateViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    static HeartRateEntry createEntry(HeartRateApiClient.HeartRateEntry entry) {
        Integer bpm = entry.value() != null ? entry.value().restingHeartRate() : null;
        return new HeartRateEntry(LocalDate.parse(entry.dateTime()), bpm);
    }

    public List<LocalDate> heartRateDates() {
        return heartRates.stream().map(HeartRateEntry::date).toList();
    }

    public List<Integer> restingHeartRates() {
        return heartRates.stream().map(HeartRateEntry::restingHeartRate).toList();
    }
}
