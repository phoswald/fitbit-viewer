package com.github.phoswald.fitbit.viewer.pages.azm;

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
import com.github.phoswald.fitbit.viewer.fitbitapi.ActiveZoneMinutesApiClient;
import com.github.phoswald.fitbit.viewer.pages.PageController;
import com.github.phoswald.fitbit.viewer.repository.ActiveZoneMinutesEntity;
import com.github.phoswald.fitbit.viewer.repository.ActiveZoneMinutesRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/azm")
public class ActiveZoneMinutesController extends PageController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template activeZoneMinutes;

    @Inject
    @RestClient
    private ActiveZoneMinutesApiClient azmClient;

    @Inject
    private ActiveZoneMinutesRepository azmRepository;

    @QueryParam("begDate")
    private LocalDate begDate;

    @QueryParam("endDate")
    private LocalDate endDate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getAzmPage() {
        if (begDate == null || endDate == null) {
            begDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now();
        }
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return activeZoneMinutes.data("model", createAzmViewModel(session.get(), begDate, endDate));
        } else {
            return activeZoneMinutes.data("model", ActiveZoneMinutesViewModel.createError("You are not logged in."));
        }
    }

    private ActiveZoneMinutesViewModel createAzmViewModel(SessionData session, LocalDate begDate, LocalDate endDate) {
        try {
            log.info("Querying: begDate={}, endDate={}", begDate, endDate);
            var azms = azmRepository.loadByUserIdAndDateRange(session.userId(), begDate, endDate).stream()
                    .collect(toSortedMap(ActiveZoneMinutesEntity::getDate));
            if (isComplete(azms, begDate, endDate)) {
                log.debug("Found {} entities", azms.size());
            } else {
                var response = azmClient.getAzm(
                        "Bearer " + session.accessToken(),
                        begDate.toString(),
                        endDate.toString());
                azms = response.activitiesActiveZoneMinutes().stream()
                        .map(entry -> ActiveZoneMinutesEntity.create(session.userId(), entry))
                        .collect(toSortedMap(ActiveZoneMinutesEntity::getDate));
                for(LocalDate date = begDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    if(!azms.containsKey(date)) {
                        azms.put(date, createEmptyDay(session.userId(), date));
                    }
                }
                log.info("Storing {} entities", azms.size());
                azmRepository.storeAll(azms.values());
            }
            return ActiveZoneMinutesViewModel.create(begDate, endDate, azms.values());
        } catch (Exception e) {
            log.warn("Failed", e);
            return ActiveZoneMinutesViewModel.createError(e.getMessage());
        }
    }

    private ActiveZoneMinutesEntity createEmptyDay(String userId, LocalDate date) {
        log.debug("Filling gap: {}", date);
        ActiveZoneMinutesEntity entity = new ActiveZoneMinutesEntity();
        entity.setUserId(userId);
        entity.setDate(date);
        return entity;
    }
}
