package com.github.phoswald.fitbit.viewer.pages.steps;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.healthapi.HealthApiClient;
import com.github.phoswald.fitbit.viewer.pages.DateRangeController;
import com.github.phoswald.fitbit.viewer.repository.StepsEntity;
import com.github.phoswald.fitbit.viewer.repository.StepsRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/steps")
public class StepsController extends DateRangeController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template steps;

    @Inject
    private HealthApiClient healthApiClient;

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
                steps.clear();
                for (var dateRange : getQueryDateRanges(Duration.ofDays(90))) {
                    var dataPoints = healthApiClient.getDailyRollup(session, "steps", dateRange.beg(), dateRange.end());
                    steps.putAll(dataPoints.stream()
                            .map(dataPoint -> StepsEntity.create(session.userId(), dataPoint))
                            .collect(toSortedMap(StepsEntity::getDate)));
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
