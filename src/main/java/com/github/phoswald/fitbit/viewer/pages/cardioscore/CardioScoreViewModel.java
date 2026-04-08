package com.github.phoswald.fitbit.viewer.pages.cardioscore;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.repository.CardioScoreEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record CardioScoreViewModel(
        LocalDate begDate,
        LocalDate endDate,
        Collection<CardioScoreEntity> cardioScores,
        String errorMessage,
        ZonedDateTime now
) {

    static CardioScoreViewModel create(
            LocalDate begDate,
            LocalDate endDate,
            Collection<CardioScoreEntity> cardioScores) {
        return new CardioScoreViewModelBuilder()
                .begDate(begDate)
                .endDate(endDate)
                .cardioScores(cardioScores)
                .now(ZonedDateTime.now())
                .build();
    }

    static CardioScoreViewModel createError(String errorMessage) {
        return new CardioScoreViewModelBuilder()
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
