package com.github.phoswald.fitbit.viewer.pages;

import java.time.LocalDate;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record DateRangeViewModel(
        LocalDate beg,
        LocalDate end,
        DatePeriod period) {

    public LocalDate prevBeg() {
        return !isDefined() ? null : period.previous(beg);
    }

    public LocalDate prevEnd() {
        return !isDefined() ? null : period.end(prevBeg());
    }

    public LocalDate nextBeg() {
        return !isDefined() ? null : period.next(beg);
    }

    public LocalDate nextEnd() {
        return !isDefined() ? null : period.end(nextBeg());
    }

    public LocalDate curDay() {
        return !isDefined() ? null : DatePeriod.DAY.beg(end);
    }

    public LocalDate curWeekBeg() {
        return !isDefined() ? null : DatePeriod.WEEK.beg(end);
    }

    public LocalDate curWeekEnd() {
        return !isDefined() ? null : DatePeriod.WEEK.end(end);
    }

    public LocalDate curMonthBeg() {
        return !isDefined() ? null : DatePeriod.MONTH.beg(end);
    }

    public LocalDate curMonthEnd() {
        return !isDefined() ? null : DatePeriod.MONTH.end(end);
    }

    public LocalDate curYearBeg() {
        return !isDefined() ? null : DatePeriod.YEAR.beg(end);
    }

    public LocalDate curYearEnd() {
        return !isDefined() ? null : DatePeriod.YEAR.end(end);
    }

    private boolean isDefined() {
        return beg != null && end != null && period != null;
    }
}
