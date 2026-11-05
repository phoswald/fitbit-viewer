package com.github.phoswald.fitbit.viewer.pages.heartrate;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.healthapi.HealthApiClient;
import com.github.phoswald.fitbit.viewer.pages.DateRangeController;
import com.github.phoswald.fitbit.viewer.repository.HeartRateEntity;
import com.github.phoswald.fitbit.viewer.repository.HeartRateRepository;
import com.google.api.health.v4.model.ReconcileDataPointsResponseDataPointsInner;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/heartrate")
public class HeartRateController extends DateRangeController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template heartrate;

    @Inject
    private HealthApiClient healthApiClient;

    @Inject
    private HeartRateRepository heartRateRepository;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getHeartRatePage() {
        normalizeDateRange();
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return heartrate.data("model", createHeartRateViewModel(session.get()));
        } else {
            return heartrate.data("model", HeartRateViewModel.createError("You are not logged in."));
        }
    }

    private HeartRateViewModel createHeartRateViewModel(SessionData session) {
        try {
            log.info("Querying: dateBeg={}, dateEnd={}, datePeriod={}", dateBeg, dateEnd, datePeriod);
            SortedMap<LocalDate, HeartRateEntity> heartRates = heartRateRepository.loadByUserIdAndDateRange(session.userId(), dateBeg, dateEnd).stream()
                    .collect(toSortedMap(HeartRateEntity::getDate));
            if(!refresh && isComplete(heartRates)) {
                log.debug("Found {} entities", heartRates.size());
            } else {
                heartRates.clear();
                for (var dateRange : getQueryDateRanges(Duration.ofDays(90))) {
                    var dataPoints = healthApiClient.getReconcile(session, "daily-resting-heart-rate", "daily_resting_heart_rate.date", dateRange.beg(), dateRange.end());
                    heartRates.putAll(dataPoints.stream()
                            .map(ReconcileDataPointsResponseDataPointsInner::getDailyRestingHeartRate)
                            .filter(Objects::nonNull)
                            .map(value -> HeartRateEntity.create(session.userId(), value))
                            .collect(toSortedMap(HeartRateEntity::getDate)));
                }
                for(LocalDate date = dateBeg; !date.isAfter(dateEnd); date = date.plusDays(1)) {
                    if(!heartRates.containsKey(date)) {
                        heartRates.put(date, createEmptyDay(session.userId(), date));
                    }
                }
                log.info("Storing {} entities", heartRates.size());
                heartRateRepository.storeAll(heartRates.values());
            }
            return HeartRateViewModel.create(createDateRangeViewModel(), heartRates.values(), session.userId());
        } catch (Exception e) {
            log.warn("Failed", e);
            return HeartRateViewModel.createError(e.getMessage());
        }
    }

    private HeartRateEntity createEmptyDay(String userId, LocalDate date) {
        log.debug("Filling gap: {}", date);
        HeartRateEntity entity = new HeartRateEntity();
        entity.setUserId(userId);
        entity.setDate(date);
        return entity;
    }
}
