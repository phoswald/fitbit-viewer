package com.github.phoswald.fitbit.viewer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardViewModel {

    public final String errorMessage;
    public final FitbitProfile profile;
    public final String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

    public DashboardViewModel(String errorMessage, FitbitProfile profile) {
        this.errorMessage = errorMessage;
        this.profile = profile;
    }
}
