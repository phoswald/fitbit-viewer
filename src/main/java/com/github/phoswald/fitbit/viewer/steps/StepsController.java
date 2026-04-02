package com.github.phoswald.fitbit.viewer.steps;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @QueryParam("startDate")
    private String startDate;

    @QueryParam("endDate")
    private String endDate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getStepsPage() {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (endDate == null) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (accessToken != null && startDate != null && endDate != null) {
            log.debug("getStepsPage: startDate={}, endDate={}", startDate, endDate);
            try {
                var stepsResponse = stepsClient.getSteps("Bearer " + accessToken, startDate, endDate);
                return steps.data("model", StepsViewModel.create(startDate, endDate, stepsResponse));
            } catch (Exception e) {
                log.warn("getStepsPage: failed to fetch steps", e);
                return steps.data("model", StepsViewModel.createError(e.getMessage()));
            }
        } else {
            return steps.data("model", StepsViewModel.create(startDate, endDate, null));
        }
    }
}
