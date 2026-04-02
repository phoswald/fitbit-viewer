package com.github.phoswald.fitbit.viewer.profile;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

record DashboardViewModel(
        String errorMessage,
        Profile profile,
        String now
) {

    record Profile(
            String displayName,
            String fullName,
            Integer age,
            String gender,
            String avatarUrl
    ) {
    }

    static DashboardViewModel create(ProfileApiClient.UserData user) {
        return new DashboardViewModel(
                null,
                new Profile(user.displayName(), user.fullName(), user.age(), user.gender(), user.avatar()),
                getNow());
    }

    static DashboardViewModel createError(String errorMessage) {
        return new DashboardViewModel(
                errorMessage,
                null,
                getNow());
    }

    private static String getNow() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
