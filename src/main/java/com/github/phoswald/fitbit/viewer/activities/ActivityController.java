package com.github.phoswald.fitbit.viewer.activities;

import java.time.LocalDate;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.login.SessionManager;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/activities")
public class ActivityController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template activities;

    @Inject
    @RestClient
    private ActivityApiClient activityClient;

    @Inject
    private SessionManager sessionManager;

    @CookieParam(SessionManager.COOKIE_NAME)
    private String sessionCookie;

    @QueryParam("begDate")
    private LocalDate begDate;

    @QueryParam("limit")
    @DefaultValue("20")
    private int limit;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getActivitiesPage() {
        if (begDate == null) {
            begDate = LocalDate.now().minusDays(30);
        }
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            try {
                log.info("getActivitiesPage(): begDate={}, limit={}", begDate, limit);
                var response = activityClient.getActivities("Bearer " + session.get().accessToken(), begDate.toString(), null,"asc", limit, 0);
                return activities.data("model", ActivityViewModel.create(begDate, limit, response.activities()));
            } catch (Exception e) {
                log.warn("getActivitiesPage(): failed", e);
                return activities.data("model", ActivityViewModel.createError(e.getMessage()));
            }
        } else {
            return activities.data("model", ActivityViewModel.createError("You are not logged in."));
        }
    }
}
