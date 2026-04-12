package com.github.phoswald.fitbit.viewer.pages.steps;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.List;

import com.github.phoswald.fitbit.viewer.repository.StepsEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record StepsViewModel(
        LocalDate begDate,
        LocalDate endDate,
        Collection<StepsEntity> steps,
        String errorMessage,
        ZonedDateTime now
) {

    static StepsViewModel create(
            LocalDate begDate,
            LocalDate endDate,
            Collection<StepsEntity> steps) {
        return new StepsViewModelBuilder()
                .begDate(begDate)
                .endDate(endDate)
                .steps(steps)
                .now(ZonedDateTime.now())
                .build();
    }

    static StepsViewModel createError(String errorMessage) {
        return new StepsViewModelBuilder()
                .begDate(LocalDate.now())
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }

    public List<LocalDate> stepDates() {
        return steps.stream().map(StepsEntity::getDate).toList();
    }

    public List<Integer> stepCounts() {
        return steps.stream().map(StepsEntity::getStepCount).toList();
    }

    public LocalDate prevWeekBeg() {
        return begDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
    }

    public LocalDate prevWeekEnd() {
        return prevWeekBeg().plusDays(6);
    }

    public LocalDate curWeekBeg() {
        return begDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public LocalDate curWeekEnd() {
        return curWeekBeg().plusDays(6);
    }

    public LocalDate nextWeekBeg() {
        return begDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(1);
    }

    public LocalDate nextWeekEnd() {
        return nextWeekBeg().plusDays(6);
    }

    public LocalDate prevMonthBeg() {
        return begDate.withDayOfMonth(1).minusMonths(1);
    }

    public LocalDate prevMonthEnd() {
        return prevMonthBeg().with(TemporalAdjusters.lastDayOfMonth());
    }

    public LocalDate curMonthBeg() {
        return begDate.withDayOfMonth(1);
    }

    public LocalDate curMonthEnd() {
        return curMonthBeg().with(TemporalAdjusters.lastDayOfMonth());
    }

    public LocalDate nextMonthBeg() {
        return begDate.withDayOfMonth(1).plusMonths(1);
    }

    public LocalDate nextMonthEnd() {
        return nextMonthBeg().with(TemporalAdjusters.lastDayOfMonth());
    }

    public LocalDate prevYearBeg() {
        return begDate.withDayOfYear(1).minusYears(1);
    }

    public LocalDate prevYearEnd() {
        return prevYearBeg().with(TemporalAdjusters.lastDayOfYear());
    }

    public LocalDate curYearBeg() {
        return begDate.withDayOfYear(1);
    }

    public LocalDate curYearEnd() {
        return curYearBeg().with(TemporalAdjusters.lastDayOfYear());
    }

    public LocalDate nextYearBeg() {
        return begDate.withDayOfYear(1).plusYears(1);
    }

    public LocalDate nextYearEnd() {
        return nextYearBeg().with(TemporalAdjusters.lastDayOfYear());
    }
}
