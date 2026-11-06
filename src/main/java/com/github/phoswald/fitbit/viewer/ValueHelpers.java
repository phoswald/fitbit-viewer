package com.github.phoswald.fitbit.viewer;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;

public class ValueHelpers {

    // *** Parsing

    public static Integer parseInteger(String value) {
        return value == null ? null : Integer.valueOf(value);
    }

    public static OffsetDateTime parseOffsetDateTime(String time, String utcOffset) {
        // Format: "2026-11-05T10:18:00Z" and "3600s"
        return time == null || utcOffset == null ? null : OffsetDateTime.parse(time)
                .withOffsetSameInstant(ZoneOffset.ofTotalSeconds((int) parseDuration(utcOffset).toSeconds()));
    }

    public static Duration parseDuration(String value) {
        // Format: "1800s" or "3.5s"
        return Duration.parse("PT" + value);
    }

    // *** Conversion

    public static Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    public static <T> Function<T, Double> toDouble(Function<T, Integer> function) {
        return object -> {
            Integer value = function.apply(object);
            return value == null ? null : value.doubleValue();
        };
    }

    public static Double toMinutes(Duration duration) {
        return duration == null ? null : (double) duration.toMinutes();
    }

    // *** Component Access

    public static LocalDate dateOf(OffsetDateTime value) {
        return value == null ? null : value.toLocalDate();
    }

    // *** Computation

    public static Double minutesBetween(OffsetDateTime beg, OffsetDateTime end) {
        if (beg != null && end != null) {
            return Duration.between(beg, end).toSeconds() / 60.0;
        } else {
            return null;
        }
    }

    public static Integer add(Integer value1, Integer value2) {
        if (value1 != null || value2 != null) {
            return (value1 == null ? 0 : value1.intValue()) + (value2 == null ? 0 : value2.intValue());
        } else {
            return null;
        }
    }

    public static Double add(Double value1, Integer value2) {
        if (value1 != null || value2 != null) {
            return (value1 == null ? 0 : value1.doubleValue()) + (value2 == null ? 0 : value2.intValue());
        } else {
            return null;
        }
    }

    public static <T> Function<T, Double> add(Function<T, Double> accessor1, Integer value2) {
        return obj -> add(accessor1.apply(obj), value2);
    }

    public static Double multiplyBy(Double value, double factor) {
        return value == null ? null : value * factor;
    }

    public static Double divideBy(Integer value, int divisor) {
        return value == null ? null : value.doubleValue() / divisor;
    }

    public static Double divideBy(Double value, int divisor) {
        return value == null ? null : value / divisor;
    }

    public static Double divideBy(Double value, Double divisor) {
        return value == null || divisor == null || divisor == 0 ? null : value / divisor;
    }

    public static <T> Function<T, Double> divideBy(Function<T, Double> function, int divisor) {
        return object -> {
            Double value = function.apply(object);
            return value == null ? null : value / divisor;
        };
    }

    public static Integer min(Integer value1, Integer value2) {
        if (value1 != null && value2 != null) {
            return Math.min(value1, value2);
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static Integer max(Integer value1, Integer value2) {
        if (value1 != null && value2 != null) {
            return Math.max(value1, value2);
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static Double min(Double value1, Double value2) {
        if (value1 != null && value2 != null) {
            return Math.min(value1, value2);
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static Double max(Double value1, Double value2) {
        if (value1 != null && value2 != null) {
            return Math.max(value1, value2);
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static LocalDate min(LocalDate value1, LocalDate value2) {
        if (value1 != null && value2 != null) {
            return value1.isBefore(value2) ? value1 : value2;
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static OffsetDateTime min(OffsetDateTime value1, OffsetDateTime value2) {
        if (value1 != null && value2 != null) {
            return value1.isBefore(value2) ? value1 : value2;
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static OffsetDateTime max(OffsetDateTime value1, OffsetDateTime value2) {
        if (value1 != null && value2 != null) {
            return value1.isAfter(value2) ? value1 : value2;
        } else {
            return value1 != null ? value1 : value2;
        }
    }

    public static Integer round(Double value) {
        return value == null ? null : (int) Math.round(value);
    }

    public static Integer maskZero(Integer value) {
        return value == null || value.intValue() == 0 ? null : value;
    }

    public static Double maskZero(Double value) {
        return value == null || value.doubleValue() == 0 ? null : value;
    }
}
