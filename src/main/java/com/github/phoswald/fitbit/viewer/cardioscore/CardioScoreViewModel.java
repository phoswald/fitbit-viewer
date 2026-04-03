package com.github.phoswald.fitbit.viewer.cardioscore;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record CardioScoreViewModel(
        LocalDate begDate,
        LocalDate endDate,
        List<CardioScoreEntry> scores,
        String errorMessage,
        ZonedDateTime now
) {
    record CardioScoreEntry(LocalDate date, Integer scoreMin, Integer scoreMax) { }

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
        Matcher matcher = Pattern.compile("([0-9]+)-([0-9]+)").matcher(entry.value().vo2Max());
        Integer scoreMin = null;
        Integer scoreMax = null;
        if(matcher.matches()) {
            scoreMin = Integer.valueOf(matcher.group(1));
            scoreMax = Integer.valueOf(matcher.group(2));
        }
        return new CardioScoreEntry(LocalDate.parse(entry.dateTime()), scoreMin, scoreMax);
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
