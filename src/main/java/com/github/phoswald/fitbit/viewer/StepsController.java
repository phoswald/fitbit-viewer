package com.github.phoswald.fitbit.viewer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/steps")
public class StepsController {

    private static final Logger log = LoggerFactory.getLogger(StepsController.class);

    @Inject
    private Template steps;

    @Inject
    private FitbitApiClient apiClient;

    @QueryParam("startDate") private String startDate;
    @QueryParam("endDate") private String endDate;
    @CookieParam("fitbitAccessToken") private String fitbitAccessToken;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getStepsPage() {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (endDate == null) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (fitbitAccessToken != null && startDate != null && endDate != null) {
            log.debug("getStepsPage: startDate={}, endDate={}", startDate, endDate);
            try {
                var stepsList = apiClient.getSteps(fitbitAccessToken, startDate, endDate);
                return steps.data("model", new StepsViewModel(startDate, endDate, stepsList, null));
            } catch (Exception e) {
                log.warn("getStepsPage: failed to fetch steps", e);
                return steps.data("model", new StepsViewModel(startDate, endDate, null, e.getMessage()));
            }
        }
        return steps.data("model", new StepsViewModel(startDate, endDate, null, null));
    }
}
