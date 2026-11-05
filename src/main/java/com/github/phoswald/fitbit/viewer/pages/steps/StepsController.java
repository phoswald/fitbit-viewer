package com.github.phoswald.fitbit.viewer.pages.steps;

import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.dateRange;

import java.time.LocalDate;
import java.util.SortedMap;
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
import com.github.phoswald.fitbit.viewer.pages.DateRangeController;
import com.github.phoswald.fitbit.viewer.repository.StepsEntity;
import com.github.phoswald.fitbit.viewer.repository.StepsRepository;
import com.google.api.health.v4.api.DefaultApi;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequest;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/steps")
public class StepsController extends DateRangeController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template steps;

    @Inject
    @RestClient
    private DefaultApi healthApiClient;

    @Inject
    private StepsRepository stepsRepository;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getStepsPage() {
        normalizeDateRange();
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return steps.data("model", createStepsViewModel(session.get()));
        } else {
            return steps.data("model", StepsViewModel.createError("You are not logged in."));
        }
    }

    private StepsViewModel createStepsViewModel(SessionData session) {
        try {
            log.info("Querying: dateBeg={}, dateEnd={}, datePeriod={}", dateBeg, dateEnd, datePeriod);
            SortedMap<LocalDate, StepsEntity> steps = stepsRepository.loadByUserIdAndDateRange(session.userId(), dateBeg, dateEnd).stream()
                    .collect(toSortedMap(StepsEntity::getDate));
            if(!refresh && isComplete(steps)) {
                log.debug("Found {} entities", steps.size());
            } else {
                steps = new TreeMap<>();
                for (var dateRange : getQueryDateRanges()) {
                    log.debug("Querying (current): dateBeg={}, dateEnd={}", dateRange.beg(), dateRange.end());
                    var request = new DailyRollUpDataPointsRequest()
                            .range(dateRange(dateRange.beg(), dateRange.end()))
                            .windowSizeDays(1);
                    var response = healthApiClient.healthUsersDataTypesDataPointsDailyRollUp(
                            "Bearer " + session.accessToken(),
                            "users/me/dataTypes/steps",
                            request);
                    if(response.getRollupDataPoints() != null) {
                        steps.putAll(response.getRollupDataPoints().stream()
                                .map(entry -> StepsEntity.create(session.userId(), entry))
                                .collect(toSortedMap(StepsEntity::getDate)));
                    }
                }
                for(LocalDate date = dateBeg; !date.isAfter(dateEnd); date = date.plusDays(1)) {
                    if(!steps.containsKey(date)) {
                        steps.put(date, createEmptyDay(session.userId(), date));
                    }
                }
                log.info("Storing {} entities", steps.size());
                stepsRepository.storeAll(steps.values());
            }
            return StepsViewModel.create(createDateRangeViewModel(), steps.values(), session.userId());
        } catch (Exception e) {
            log.warn("Failed", e);
            return StepsViewModel.createError(e.getMessage());
        }
    }

    private StepsEntity createEmptyDay(String userId, LocalDate date) {
        log.debug("Filling gap: {}", date);
        StepsEntity entity = new StepsEntity();
        entity.setUserId(userId);
        entity.setDate(date);
        return entity;
    }
}
