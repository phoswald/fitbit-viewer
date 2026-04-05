package com.github.phoswald.fitbit.viewer.azm;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record AzmViewModel(
        LocalDate begDate,
        LocalDate endDate,
        List<AzmEntry> entries,
        String errorMessage,
        ZonedDateTime now
) {

    @RecordBuilder
    record AzmEntry(
            LocalDate date,
            Integer activeZoneMinutes,
            Integer fatBurnActiveZoneMinutes,
            Integer cardioActiveZoneMinutes,
            Integer peakActiveZoneMinutes
    ) { }

    static AzmViewModel create(
            LocalDate begDate,
            LocalDate endDate,
            List<AzmApiClient.AzmEntry> apiEntries) {
        return new AzmViewModelBuilder()
                .begDate(begDate)
                .endDate(endDate)
                .entries(apiEntries == null ? null : apiEntries.stream().map(AzmViewModel::createEntry).toList())
                .now(ZonedDateTime.now())
                .build();
    }

    static AzmViewModel createError(String errorMessage) {
        return new AzmViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    private static AzmEntry createEntry(AzmApiClient.AzmEntry entry) {
        AzmApiClient.AzmValue value = entry.value();
        return new AzmEntryBuilder()
                .date(LocalDate.parse(entry.dateTime()))
                .activeZoneMinutes(value == null ? null : value.activeZoneMinutes())
                .fatBurnActiveZoneMinutes(value == null ? null : value.fatBurnActiveZoneMinutes())
                .cardioActiveZoneMinutes(value == null ? null : value.cardioActiveZoneMinutes())
                .peakActiveZoneMinutes(value == null ? null : value.peakActiveZoneMinutes())
                .build();
    }

    public List<LocalDate> dates() {
        return entries.stream().map(AzmEntry::date).toList();
    }

    public List<Integer> activeZoneMinutes() {
        return entries.stream().map(AzmEntry::activeZoneMinutes).toList();
    }

    public List<Integer> fatBurnActiveZoneMinutes() {
        return entries.stream().map(AzmEntry::fatBurnActiveZoneMinutes).toList();
    }

    public List<Integer> cardioActiveZoneMinutes() {
        return entries.stream().map(AzmEntry::cardioActiveZoneMinutes).toList();
    }

    public List<Integer> peakActiveZoneMinutes() {
        return entries.stream().map(AzmEntry::peakActiveZoneMinutes).toList();
    }
}
