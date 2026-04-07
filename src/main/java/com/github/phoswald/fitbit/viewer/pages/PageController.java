package com.github.phoswald.fitbit.viewer.pages;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;

import com.github.phoswald.fitbit.viewer.auth.SessionManager;

public abstract class PageController {

    @Inject
    protected SessionManager sessionManager;

    @CookieParam(SessionManager.COOKIE_NAME)
    protected String sessionCookie;

    protected boolean isComplete(List<?> entities, LocalDate begDate, LocalDate endDate) { // TODO remove
        return entities.size() == ChronoUnit.DAYS.between(begDate, endDate) + 1;
    }

    protected boolean isComplete(SortedMap<LocalDate, ?> entities, LocalDate begDate, LocalDate endDate) {
        return entities.size() == ChronoUnit.DAYS.between(begDate, endDate) + 1;
    }

    protected <T> SortedMap<LocalDate, T> toSortedMap(Function<T, LocalDate> getKey, List<T> list) {
        return list.stream().collect(toSortedMap(getKey));
    }

    private <T> Collector<T, ?, SortedMap<LocalDate, T>> toSortedMap(Function<T, LocalDate> getKey) {
        return Collectors.toMap(
                getKey,
                Function.identity(),
                this::rejectDuplicates,
                TreeMap::new);
    }

    private <T> T rejectDuplicates(T value1, T value2) {
        throw new IllegalStateException();
    }
}
