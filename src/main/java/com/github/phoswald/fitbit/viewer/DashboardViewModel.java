package com.github.phoswald.fitbit.viewer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardViewModel {

    public final String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

    public DashboardViewModel() { }
}
