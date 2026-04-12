package com.github.phoswald.fitbit.viewer.pages;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public enum DatePeriod {

    DAY {
        @Override
        public LocalDate beg(LocalDate current) {
            return current;
        }

        @Override
        public LocalDate end(LocalDate current) {
            return current;
        }

        @Override
        public LocalDate previous(LocalDate current) {
            return current.minusDays(1);
        }

        @Override
        public LocalDate next(LocalDate current) {
            return current.plusDays(1);
        }
    },
    WEEK {
        @Override
        public LocalDate beg(LocalDate current) {
            return current.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        @Override
        public LocalDate end(LocalDate current) {
            return current.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        }

        @Override
        public LocalDate previous(LocalDate current) {
            return current.minusWeeks(1);
        }

        @Override
        public LocalDate next(LocalDate current) {
            return current.plusWeeks(1);
        }
    },
    MONTH {
        @Override
        public LocalDate beg(LocalDate current) {
            return current.withDayOfMonth(1);
        }

        @Override
        public LocalDate end(LocalDate current) {
            return current.with(TemporalAdjusters.lastDayOfMonth());
        }

        @Override
        public LocalDate previous(LocalDate current) {
            return current.minusMonths(1);
        }

        @Override
        public LocalDate next(LocalDate current) {
            return current.plusMonths(1);
        }
    },
    YEAR {
        @Override
        public LocalDate beg(LocalDate current) {
            return current.withDayOfYear(1);
        }

        @Override
        public LocalDate end(LocalDate current) {
            return current.with(TemporalAdjusters.lastDayOfYear());
        }

        @Override
        public LocalDate previous(LocalDate current) {
            return current.minusYears(1);
        }

        @Override
        public LocalDate next(LocalDate current) {
            return current.plusYears(1);
        }
    };

    public abstract LocalDate beg(LocalDate current);

    public abstract LocalDate end(LocalDate current);

    public abstract LocalDate previous(LocalDate current);

    public abstract LocalDate next(LocalDate current);
}
