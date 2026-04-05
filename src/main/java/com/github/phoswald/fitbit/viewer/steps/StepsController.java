package com.github.phoswald.fitbit.viewer.steps;

import java.time.LocalDate;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;

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
@Path("/pages/steps")
public class StepsController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template steps;

    @Inject
    @RestClient
    private StepsApiClient stepsClient;

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
    public TemplateInstance getStepsPage() {
        if(begDate == null || endDate == null) {
            begDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now();
        }
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            try {
                log.info("getStepsPage(): begDate={}, endDate={}", begDate, endDate);
                var stepsResponse = stepsClient.getSteps("Bearer " + session.get().accessToken(), begDate.toString(), endDate.toString());
                return steps.data("model", StepsViewModel.create(begDate, endDate, stepsResponse.activitiesSteps()));
            } catch (Exception e) {
                log.warn("getStepsPage(): failed", e);
                return steps.data("model", StepsViewModel.createError(e.getMessage()));
            }
        } else {
            return steps.data("model", StepsViewModel.createError("You are not logged in."));
        }
    }
}
