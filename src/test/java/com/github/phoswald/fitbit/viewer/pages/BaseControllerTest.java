package com.github.phoswald.fitbit.viewer.pages;

import static org.hamcrest.Matchers.matchesPattern;

import java.util.regex.Pattern;

import jakarta.inject.Inject;

import org.hamcrest.Matcher;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.auth.SessionManager;

public abstract class BaseControllerTest {

    protected static final String USER_ID = "sample-user-1";

    @Inject
    protected SessionManager sessionManager;

    protected String createCookie() {
        return sessionManager.createAndSignCookie(SessionData.builder()
                .userId(USER_ID)
                .accessToken("sample-access-token")
                .build());
    }

    protected static Matcher<String> row(String label, String value) {
        String inRow = "(?:(?!</tr>).)*?"; // any content that doesn't leave the current row
        return matchesPattern(Pattern.compile(
                ".*<tr>" + inRow
                        + Pattern.quote(">" + label + "<") + inRow
                        + Pattern.quote(">" + value + "<") + inRow
                        + "</tr>.*",
                Pattern.DOTALL));
    }
}
