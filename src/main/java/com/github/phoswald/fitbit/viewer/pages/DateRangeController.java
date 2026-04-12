package com.github.phoswald.fitbit.viewer.pages;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.SortedMap;

import jakarta.ws.rs.QueryParam;

public abstract class DateRangeController extends BaseController {

    @QueryParam("dateBeg")
    protected LocalDate dateBeg;

    @QueryParam("dateEnd")
    protected LocalDate dateEnd;

    @QueryParam("datePeriod")
    protected DatePeriod datePeriod;

    @QueryParam("refresh")
    protected boolean refresh;

    protected void normalizeDateRange() {
        LocalDate today = LocalDate.now();
        if(datePeriod == null) {
            datePeriod = DatePeriod.WEEK;
        }
        if(dateEnd == null) {
            dateEnd = datePeriod.end(today);
        }
        if(dateBeg == null) {
            dateBeg = datePeriod.beg(dateEnd);
        }
        if(dateEnd.isAfter(today)) {
            dateEnd = today;
        }
        if(dateBeg.isAfter(dateEnd)) {
            dateBeg = dateEnd;
        }
    }

    protected boolean isComplete(SortedMap<LocalDate, ?> entities) {
        return entities.size() == ChronoUnit.DAYS.between(dateBeg, dateEnd) + 1;
    }

    protected DateRangeViewModel createDateRangeViewModel() {
        return new DateRangeViewModelBuilder()
                .beg(dateBeg)
                .end(dateEnd)
                .period(datePeriod)
                .build();
    }
}
