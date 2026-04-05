package com.github.phoswald.fitbit.viewer.pages;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;

import com.github.phoswald.fitbit.viewer.auth.SessionManager;

public abstract class PageController {

    @Inject
    protected SessionManager sessionManager;

    @CookieParam(SessionManager.COOKIE_NAME)
    protected String sessionCookie;

    protected boolean isComplete(List<?> entities, LocalDate begDate, LocalDate endDate) {
        return entities.size() == ChronoUnit.DAYS.between(begDate, endDate) + 1;
    }
}
