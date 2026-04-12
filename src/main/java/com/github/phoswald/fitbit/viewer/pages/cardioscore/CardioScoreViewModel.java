package com.github.phoswald.fitbit.viewer.pages.cardioscore;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.CardioScoreEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record CardioScoreViewModel(
        DateRangeViewModel dateRange,
        Collection<CardioScoreEntity> cardioScores,
        String errorMessage,
        ZonedDateTime now
) {

    static CardioScoreViewModel create(
            DateRangeViewModel dateRange,
            Collection<CardioScoreEntity> cardioScores) {
        return new CardioScoreViewModelBuilder()
                .dateRange(dateRange)
                .cardioScores(cardioScores)
                .now(ZonedDateTime.now())
                .build();
    }

    static CardioScoreViewModel createError(String errorMessage) {
        return new CardioScoreViewModelBuilder()
                .dateRange(new DateRangeViewModelBuilder().build())
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public List<LocalDate> cardioScoreDates() {
        return cardioScores.stream().map(CardioScoreEntity::getDate).toList();
    }

    public List<Integer> cardioScoreMinValues() {
        return cardioScores.stream().map(CardioScoreEntity::getScoreMin).toList();
    }

    public List<Integer> cardioScoreMaxValues() {
        return cardioScores.stream().map(CardioScoreEntity::getScoreMax).toList();
    }
}
