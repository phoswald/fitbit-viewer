package com.github.phoswald.fitbit.viewer.pages.sleep;

import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.dateFilter;
import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.reconcile;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.SortedMap;

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
import com.github.phoswald.fitbit.viewer.pages.DateRangeController;
import com.github.phoswald.fitbit.viewer.repository.SleepEntity;
import com.github.phoswald.fitbit.viewer.repository.SleepRepository;
import com.google.api.health.v4.api.DefaultApi;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInner;

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
    private DefaultApi healthApiClient;

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
            SortedMap<LocalDate, SleepEntity> sleeps = sleepRepository.loadByUserIdAndDateRange(session.userId(), dateBeg, dateEnd).stream()
                    .collect(toSortedMap(SleepEntity::getDate));
            if (!refresh && isComplete(sleeps)) {
                log.debug("Found {} entities", sleeps.size());
            } else {
                sleeps.clear();
                // The API returns at most 25 sleep sessions per page and paging is not implemented, so 10 days leave room for naps
                for (var dateRange : getQueryDateRanges(Duration.ofDays(10))) {
                    log.debug("Querying (current): dateBeg={}, dateEnd={}", dateRange.beg(), dateRange.end());
                    var dataPoints = reconcile(
                            healthApiClient,
                            "Bearer " + session.accessToken(),
                            "users/me/dataTypes/sleep",
                            dateFilter("sleep.interval.civil_end_time", dateRange.beg(), dateRange.end()));
                    sleeps.putAll(dataPoints.stream()
                            .map(ReconcileDataPointsResponseDataPointsInner::getSleep)
                            .filter(Objects::nonNull)
                            .map(value -> SleepEntity.create(session.userId(), value))
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
            return SleepViewModel.create(createDateRangeViewModel(), sleeps.values(), session.userId());
        } catch (Exception e) {
            log.warn("Failed", e);
            return SleepViewModel.createError(e.getMessage());
        }
    }

    private SleepEntity createEmptyDay(String userId, LocalDate date) {
        log.debug("Filling gap: {}", date);
        SleepEntity entity = new SleepEntity();
        entity.setUserId(userId);
        entity.setDate(date);
        return entity;
    }
}
