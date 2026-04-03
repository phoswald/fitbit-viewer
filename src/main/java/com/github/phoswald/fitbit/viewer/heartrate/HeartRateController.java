package com.github.phoswald.fitbit.viewer.heartrate;

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
@Path("/pages/heartrate")
public class HeartRateController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template heartrate;

    @Inject
    @RestClient
    private HeartRateApiClient heartRateClient;

    @CookieParam("fitbitAccessToken")
    private String accessToken;

    @QueryParam("begDate")
    private LocalDate begDate;

    @QueryParam("endDate")
    private LocalDate endDate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getHeartRatePage() {
        if (begDate == null || endDate == null) {
            begDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now();
        }
        if (accessToken != null) {
            log.debug("getHeartRatePage: begDate={}, endDate={}", begDate, endDate);
            try {
                var response = heartRateClient.getHeartRate("Bearer " + accessToken, begDate.toString(), endDate.toString());
                return heartrate.data("model", HeartRateViewModel.create(begDate, endDate, response.activitiesHeart()));
            } catch (Exception e) {
                log.warn("getHeartRatePage: failed to fetch heart rate", e);
                return heartrate.data("model", HeartRateViewModel.createError(e.getMessage()));
            }
        } else {
            return heartrate.data("model", HeartRateViewModel.createError("You are not logged in."));
        }
    }
}
