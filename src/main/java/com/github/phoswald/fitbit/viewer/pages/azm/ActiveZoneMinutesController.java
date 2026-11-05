package com.github.phoswald.fitbit.viewer.pages.azm;

import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.dateRange;

import java.time.Duration;
import java.time.LocalDate;
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
import com.github.phoswald.fitbit.viewer.repository.ActiveZoneMinutesEntity;
import com.github.phoswald.fitbit.viewer.repository.ActiveZoneMinutesRepository;
import com.google.api.health.v4.api.DefaultApi;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequest;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/azm")
public class ActiveZoneMinutesController extends DateRangeController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template activeZoneMinutes;

    @Inject
    @RestClient
    private DefaultApi healthApiClient;

    @Inject
    private ActiveZoneMinutesRepository azmRepository;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getAzmPage() {
        normalizeDateRange();
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return activeZoneMinutes.data("model", createAzmViewModel(session.get()));
        } else {
            return activeZoneMinutes.data("model", ActiveZoneMinutesViewModel.createError("You are not logged in."));
        }
    }

    private ActiveZoneMinutesViewModel createAzmViewModel(SessionData session) {
        try {
            log.info("Querying: dateBeg={}, dateEnd={}, datePeriod={}", dateBeg, dateEnd, datePeriod);
            SortedMap<LocalDate, ActiveZoneMinutesEntity> azms = azmRepository.loadByUserIdAndDateRange(session.userId(), dateBeg, dateEnd).stream()
                    .collect(toSortedMap(ActiveZoneMinutesEntity::getDate));
            if (!refresh && isComplete(azms)) {
                log.debug("Found {} entities", azms.size());
            } else {
                azms.clear();
                for (var dateRange : getQueryDateRanges(Duration.ofDays(90))) {
                    log.debug("Querying (current): dateBeg={}, dateEnd={}", dateRange.beg(), dateRange.end());
                    var request = new DailyRollUpDataPointsRequest()
                            .range(dateRange(dateRange.beg(), dateRange.end()))
                            .windowSizeDays(1);
                    var response = healthApiClient.healthUsersDataTypesDataPointsDailyRollUp(
                            "Bearer " + session.accessToken(),
                            "users/me/dataTypes/active-zone-minutes",
                            request);
                    if(response.getRollupDataPoints() != null) {
                        azms.putAll(response.getRollupDataPoints().stream()
                                .map(entry -> ActiveZoneMinutesEntity.create(session.userId(), entry))
                                .collect(toSortedMap(ActiveZoneMinutesEntity::getDate)));
                    }
                }
                for(LocalDate date = dateBeg; !date.isAfter(dateEnd); date = date.plusDays(1)) {
                    if(!azms.containsKey(date)) {
                        azms.put(date, createEmptyDay(session.userId(), date));
                    }
                }
                log.info("Storing {} entities", azms.size());
                azmRepository.storeAll(azms.values());
            }
            return ActiveZoneMinutesViewModel.create(createDateRangeViewModel(), azms.values(), session.userId());
        } catch (Exception e) {
            log.warn("Failed", e);
            return ActiveZoneMinutesViewModel.createError(e.getMessage());
        }
    }

    private ActiveZoneMinutesEntity createEmptyDay(String userId, LocalDate date) {
        log.debug("Filling gap: {}", date);
        ActiveZoneMinutesEntity entity = new ActiveZoneMinutesEntity();
        entity.setUserId(userId);
        entity.setDate(date);
        return entity;
    }
}
