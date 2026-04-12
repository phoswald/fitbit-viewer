package com.github.phoswald.fitbit.viewer.pages.heartrate;

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
import com.github.phoswald.fitbit.viewer.fitbitapi.HeartRateApiClient;
import com.github.phoswald.fitbit.viewer.pages.PageController;
import com.github.phoswald.fitbit.viewer.repository.HeartRateEntity;
import com.github.phoswald.fitbit.viewer.repository.HeartRateRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/heartrate")
public class HeartRateController extends PageController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template heartrate;

    @Inject
    @RestClient
    private HeartRateApiClient heartRateApiClient;

    @Inject
    private HeartRateRepository heartRateRepository;

    @QueryParam("begDate")
    private LocalDate begDate;

    @QueryParam("endDate")
    private LocalDate endDate;

    @QueryParam("refresh")
    private boolean refresh;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getHeartRatePage() {
        if (begDate == null || endDate == null) {
            begDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now();
        }
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return heartrate.data("model", createHeartRateViewModel(session.get()));
        } else {
            return heartrate.data("model", HeartRateViewModel.createError("You are not logged in."));
        }
    }

    private HeartRateViewModel createHeartRateViewModel(SessionData session) {
        try {
            log.info("Querying: begDate={}, endDate={}", begDate, endDate);
            var heartRates = heartRateRepository.loadByUserIdAndDateRange(session.userId(), begDate, endDate).stream()
                    .collect(toSortedMap(HeartRateEntity::getDate));
            if(!refresh && isComplete(heartRates, begDate, endDate)) {
                log.debug("Found {} entities", heartRates.size());
            } else {
                var response = heartRateApiClient.getHeartRate(
                        "Bearer " + session.accessToken(),
                        begDate.toString(),
                        endDate.toString());
                heartRates = response.activitiesHeart().stream()
                        .map(entry -> HeartRateEntity.create(session.userId(), entry))
                        .collect(toSortedMap(HeartRateEntity::getDate));
                for(LocalDate date = begDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    if(!heartRates.containsKey(date)) {
                        heartRates.put(date, createEmptyDay(session.userId(), date));
                    }
                }
                log.info("Storing {} entities", heartRates.size());
                heartRateRepository.storeAll(heartRates.values());
            }
            return HeartRateViewModel.create(begDate, endDate, heartRates.values());
        } catch (Exception e) {
            log.warn("Failed", e);
            return HeartRateViewModel.createError(e.getMessage());
        }
    }

    private HeartRateEntity createEmptyDay(String userId, LocalDate date) {
        log.debug("Filling gap: {}", date);
        HeartRateEntity entity = new HeartRateEntity();
        entity.setUserId(userId);
        entity.setDate(date);
        return entity;
    }
}
