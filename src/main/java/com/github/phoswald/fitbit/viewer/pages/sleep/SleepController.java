package com.github.phoswald.fitbit.viewer.pages.sleep;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.min;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.fitbitapi.SleepApiClient;
import com.github.phoswald.fitbit.viewer.pages.DateRangeController;
import com.github.phoswald.fitbit.viewer.repository.SleepEntity;
import com.github.phoswald.fitbit.viewer.repository.SleepRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/sleep")
public class SleepController extends DateRangeController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template sleep;

    @Inject
    @RestClient
    private SleepApiClient sleepClient;

    @Inject
    private SleepRepository sleepRepository;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getSleepPage() {
        normalizeDateRange();
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return sleep.data("model", createSleepViewModel(session.get()));
        } else {
            return sleep.data("model", SleepViewModel.createError("You are not logged in."));
        }
    }

    private SleepViewModel createSleepViewModel(SessionData session) {
        try {
            log.info("Querying: dateBeg={}, dateEnd={}, datePeriod={}", dateBeg, dateEnd, datePeriod);
            var sleeps = sleepRepository.loadByUserIdAndDateRange(session.userId(), dateBeg, dateEnd).stream()
                    .collect(toSortedMap(SleepEntity::getDate));
            if (!refresh && isComplete(sleeps)) {
                log.debug("Found {} entities", sleeps.size());
            } else {
                sleeps = new TreeMap<>();
                for (var dateRange : getQueryDateRanges()) {
                    log.debug("Querying (current): dateBeg={}, dateEnd={}", dateRange.beg(), dateRange.end());
                    var response = sleepClient.getSleep(
                            "Bearer " + session.accessToken(),
                            dateRange.beg().toString(),
                            dateRange.end().toString());
                    sleeps.putAll(response.sleep().stream()
                            .map(entry -> SleepEntity.create(session.userId(), entry))
                            .collect(toSortedMap(SleepEntity::getDate, SleepEntity::merge)));
                }
                for (LocalDate date = dateBeg; !date.isAfter(dateEnd); date = date.plusDays(1)) {
                    if (!sleeps.containsKey(date)) {
                        sleeps.put(date, createEmptyDay(session.userId(), date));
                    }
                }
                log.info("Storing {} entities", sleeps.size());
                sleepRepository.storeAll(sleeps.values());
            }
            return SleepViewModel.create(createDateRangeViewModel(), sleeps.values());
        } catch (Exception e) {
            log.warn("Failed", e);
            return SleepViewModel.createError(e.getMessage());
        }
    }

    private List<DateRange> getQueryDateRanges() {
        var result = new ArrayList<DateRange>();
        LocalDate curBeg = dateBeg;
        while (!curBeg.isAfter(dateEnd)) {
            LocalDate curEnd = min(dateEnd, curBeg.plusMonths(1).minusDays(1));
            result.add(new DateRange(curBeg, curEnd));
            curBeg = curEnd.plusDays(1);
        }
        return result;
    }

    private SleepEntity createEmptyDay(String userId, LocalDate date) {
        log.debug("Filling gap: {}", date);
        SleepEntity entity = new SleepEntity();
        entity.setUserId(userId);
        entity.setDate(date);
        return entity;
    }

    private record DateRange(LocalDate beg, LocalDate end) { }
}
