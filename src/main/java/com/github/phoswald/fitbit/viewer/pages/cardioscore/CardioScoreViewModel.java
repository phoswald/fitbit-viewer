package com.github.phoswald.fitbit.viewer.pages.cardioscore;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.phoswald.fitbit.viewer.fitbitapi.CardioScoreApiClient;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record CardioScoreViewModel(
        LocalDate begDate,
        LocalDate endDate,
        List<CardioScoreEntry> scores,
        String errorMessage,
        ZonedDateTime now
) {

    @RecordBuilder
    record CardioScoreEntry(
            LocalDate date,
            Integer scoreMin,
            Integer scoreMax
    ) { }

    static CardioScoreViewModel create(
            LocalDate begDate,
            LocalDate endDate,
            List<CardioScoreApiClient.CardioScoreEntry> entries) {
        return new CardioScoreViewModelBuilder()
                .begDate(begDate)
                .endDate(endDate)
                .scores(entries == null ? null : entries.stream().map(CardioScoreViewModel::createEntry).toList())
                .now(ZonedDateTime.now())
                .build();
    }

    static CardioScoreViewModel createError(String errorMessage) {
        return new CardioScoreViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    static CardioScoreEntry createEntry(CardioScoreApiClient.CardioScoreEntry entry) {
        CardioScoreApiClient.CardioScoreValue value = entry.value();
        Matcher matcher = Pattern.compile("([0-9]+)-([0-9]+)").matcher(value == null ? null : value.vo2Max());
        return new CardioScoreEntryBuilder()
                .date(LocalDate.parse(entry.dateTime()))
                .scoreMin(!matcher.matches() ? null : Integer.parseInt(matcher.group(1)))
                .scoreMax(!matcher.matches() ? null : Integer.parseInt(matcher.group(2)))
                .build();
    }

    public List<LocalDate> scoreDates() {
        return scores.stream().map(CardioScoreEntry::date).toList();
    }

    public List<Integer> scoreMinValues() {
        return scores.stream().map(CardioScoreEntry::scoreMin).toList();
    }

    public List<Integer> scoreMaxValues() {
        return scores.stream().map(CardioScoreEntry::scoreMax).toList();
    }
}
