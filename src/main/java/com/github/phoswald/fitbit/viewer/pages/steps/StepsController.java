package com.github.phoswald.fitbit.viewer.pages.steps;

import java.time.LocalDate;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.fitbitapi.StepsApiClient;
import com.github.phoswald.fitbit.viewer.pages.PageController;
import com.github.phoswald.fitbit.viewer.repository.StepsEntity;
import com.github.phoswald.fitbit.viewer.repository.StepsRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/steps")
public class StepsController extends PageController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template steps;

    @Inject
    @RestClient
    private StepsApiClient stepsApiClient;

    @Inject
    private StepsRepository stepsRepository;

    @QueryParam("begDate")
    private LocalDate begDate;

    @QueryParam("endDate")
    private LocalDate endDate;

    @QueryParam("refresh")
    private boolean refresh;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getStepsPage() {
        if(begDate == null || endDate == null) {
            begDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now();
        }
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return steps.data("model", createStepsViewModel(session.get()));
        } else {
            return steps.data("model", StepsViewModel.createError("You are not logged in."));
        }
    }

    private StepsViewModel createStepsViewModel(SessionData session) {
        try {
            log.info("Querying: begDate={}, endDate={}", begDate, endDate);
            var steps = stepsRepository.loadByUserIdAndDateRange(session.userId(), begDate, endDate).stream()
                    .collect(toSortedMap(StepsEntity::getDate));
            if(!refresh && isComplete(steps, begDate, endDate)) {
                log.debug("Found {} entities", steps.size());
            } else {
                var response = stepsApiClient.getSteps(
                        "Bearer " + session.accessToken(),
                        begDate.toString(),
                        endDate.toString());
                steps = response.activitiesSteps().stream()
                        .map(entry -> StepsEntity.create(session.userId(), entry))
                        .collect(toSortedMap(StepsEntity::getDate));
                for(LocalDate date = begDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    if(!steps.containsKey(date)) {
                        steps.put(date, createEmptyDay(session.userId(), date));
                    }
                }
                log.info("Storing {} entities", steps.size());
                stepsRepository.storeAll(steps.values());
            }
            return StepsViewModel.create(begDate, endDate, steps.values());
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
