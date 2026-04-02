package com.github.phoswald.fitbit.viewer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StepsViewModel {
    public final String startDate;
    public final String endDate;
    public final List<FitbitStepsEntry> steps;
    public final String errorMessage;
    public final String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

    public StepsViewModel(String startDate, String endDate, List<FitbitStepsEntry> steps, String errorMessage) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.steps = steps;
        this.errorMessage = errorMessage;
    }
}
