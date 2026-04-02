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

    @CookieParam("fitbitAccessToken")
    private String accessToken;

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
        if (accessToken != null) {
            log.debug("getStepsPage: begDate={}, endDate={}", begDate, endDate);
            try {
                var stepsResponse = stepsClient.getSteps("Bearer " + accessToken, begDate.toString(), endDate.toString());
                return steps.data("model", StepsViewModel.create(begDate, endDate, stepsResponse.activitiesSteps()));
            } catch (Exception e) {
                log.warn("getStepsPage: failed to fetch steps", e);
                return steps.data("model", StepsViewModel.createError(e.getMessage()));
            }
        } else {
            return steps.data("model", StepsViewModel.createError("You are not logged in."));
        }
    }
}
