package com.github.phoswald.fitbit.viewer.pages.cardioscore;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModel;
import com.github.phoswald.fitbit.viewer.pages.DateRangeViewModelBuilder;
import com.github.phoswald.fitbit.viewer.repository.CardioScoreEntity;
import com.github.phoswald.fitbit.viewer.widgets.Chart;
import com.github.phoswald.fitbit.viewer.widgets.ChartBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartDataBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsAxisBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsBuilder;
import com.github.phoswald.fitbit.viewer.widgets.ChartOptionsScalesBuilder;
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

    public Chart cardioScoresChart() {
        return new ChartBuilder()
                .type("line")
                .data(new ChartDataBuilder()
                        .labels(Chart.createLabels(cardioScores, CardioScoreEntity::getDate))
                        .datasets(List.of(
                                Chart.createDataset("Score (Min)", cardioScores, CardioScoreEntity::getScoreMin),
                                Chart.createDataset("Score (Max)", cardioScores, CardioScoreEntity::getScoreMax)))
                        .build())
                .options(new ChartOptionsBuilder()
                        .scales(new ChartOptionsScalesBuilder()
                                .y(new ChartOptionsAxisBuilder()
                                        .grace("50%")
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
