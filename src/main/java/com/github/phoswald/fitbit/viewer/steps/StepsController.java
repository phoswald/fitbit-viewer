package com.github.phoswald.fitbit.viewer.steps;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.CookieParam;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.login.SessionData;
import com.github.phoswald.fitbit.viewer.login.SessionManager;
import com.github.phoswald.fitbit.viewer.repository.StepsEntity;
import com.github.phoswald.fitbit.viewer.repository.StepsRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/steps")
public class StepsController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template steps;

    @Inject
    @RestClient
    private StepsApiClient stepsApiClient;

    @Inject
    private StepsRepository stepsRepository;

    @Inject
    private SessionManager sessionManager;

    @CookieParam(SessionManager.COOKIE_NAME)
    private String sessionCookie;

    @QueryParam("begDate")
    private LocalDate begDate;

    @QueryParam("endDate")
    private LocalDate endDate;

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
            return steps.data("model", createStepsViewModel(session.get(), begDate, endDate));
        } else {
            return steps.data("model", StepsViewModel.createError("You are not logged in."));
        }
    }

    private StepsViewModel createStepsViewModel(SessionData session, LocalDate begDate, LocalDate endDate) {
        try {
            log.info("Querying: begDate={}, endDate={}", begDate, endDate);
            List<StepsEntity> entities = stepsRepository.loadByUserIdAndDateRange(session.userId(), begDate, endDate);
            if(isComplete(entities, begDate, endDate)) {
                log.debug("Found {} entities", entities.size());
            } else {
                var response = stepsApiClient.getSteps(
                        "Bearer " + session.accessToken(),
                        begDate.toString(),
                        endDate.toString());
                entities = response.activitiesSteps().stream()
                        .map(entry -> StepsEntity.create(session.userId(), entry))
                        .toList();
                log.info("Storing {} entities", entities.size());
                stepsRepository.storeAll(entities);
            }
            return StepsViewModel.create(begDate, endDate, entities);
        } catch (Exception e) {
            log.warn("Failed", e);
            return StepsViewModel.createError(e.getMessage());
        }
    }

    private boolean isComplete(List<?> entities, LocalDate begDate, LocalDate endDate) {
        return entities.size() == ChronoUnit.DAYS.between(begDate, endDate) + 1;
    }
}
