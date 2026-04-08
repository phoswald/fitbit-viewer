package com.github.phoswald.fitbit.viewer.pages.activities;

import java.time.LocalDate;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.fitbitapi.ActivityApiClient;
import com.github.phoswald.fitbit.viewer.pages.PageController;
import com.github.phoswald.fitbit.viewer.repository.ActivityEntity;
import com.github.phoswald.fitbit.viewer.repository.ActivityRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/activities")
public class ActivityController extends PageController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template activities;

    @Inject
    @RestClient
    private ActivityApiClient activityClient;

    @Inject
    private ActivityRepository activityRepository;

    @QueryParam("begDate")
    private LocalDate begDate;

    @QueryParam("limit")
    @DefaultValue("20")
    private int limit;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getActivitiesPage() {
        if (begDate == null) {
            begDate = LocalDate.now().minusDays(30);
        }
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return activities.data("model", createActivityViewModel(session.get(), begDate, limit));
        } else {
            return activities.data("model", ActivityViewModel.createError("You are not logged in."));
        }
    }

    private ActivityViewModel createActivityViewModel(SessionData session, LocalDate begDate, int limit) {
        try {
            log.info("Querying: begDate={}, limit={}", begDate, limit);
            var response = activityClient.getActivities(
                    "Bearer " + session.accessToken(),
                    begDate.toString(),
                    null,"asc",
                    limit, 0);
            var entities = response.activities().stream()
                    .map(entry -> ActivityEntity.create(session.userId(), entry))
                    .toList();
            log.info("Storing {} entities", entities.size());
            activityRepository.storeAll(entities);
            return ActivityViewModel.create(begDate, limit, entities);
        } catch (Exception e) {
            log.warn("Failed", e);
            return ActivityViewModel.createError(e.getMessage());
        }
    }
}
