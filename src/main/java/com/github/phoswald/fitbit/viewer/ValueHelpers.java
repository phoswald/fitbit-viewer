package com.github.phoswald.fitbit.viewer;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class ValueHelpers {

    public static LocalDate parseDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    public static OffsetDateTime parseDateTime(String value) {
        return value == null ? null : OffsetDateTime.parse(value);
    }

    public static OffsetDateTime plusMillis(OffsetDateTime value, Long delta) {
        return value == null || delta == null ? null : value.plusSeconds(delta / 1000);
    }

    public static LocalDate dateOf(OffsetDateTime value) {
        return value == null ? null : value.toLocalDate();
    }

    public static Double minutesBetween(OffsetDateTime beg, OffsetDateTime end) {
        if(beg != null && end != null) {
            return Duration.between(beg, end).toSeconds() / 60.0;
        } else {
            return null;
        }
    }

    public static Double divideBy(Long value, int divisor) {
        return value == null ? null : value.doubleValue() / divisor;
    }

    public static Integer max(Integer value1, Integer value2) {
        if(value1 != null && value2 != null) {
            return  Math.max(value1, value2);
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static Double min(Double value1, Double value2) {
        if(value1 != null && value2 != null) {
            return  Math.min(value1, value2);
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static Double max(Double value1, Double value2) {
        if(value1 != null && value2 != null) {
            return  Math.max(value1, value2);
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static OffsetDateTime min(OffsetDateTime value1, OffsetDateTime value2) {
        if(value1 != null && value2 != null) {
            return value1.isBefore(value2) ? value1 : value2;
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static OffsetDateTime max(OffsetDateTime value1, OffsetDateTime value2) {
        if(value1 != null && value2 != null) {
            return value1.isAfter(value2) ? value1 : value2;
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static Integer maskZero(Integer value) {
        return value == null || value.intValue() == 0 ? null : value;
    }

    public static Double maskZero(Double value) {
        return value == null || value.doubleValue() == 0 ? null : value;
    }
}
