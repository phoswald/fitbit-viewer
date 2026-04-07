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
            var entities = toSortedMap(ActiveZoneMinutesEntity::getDate,
                    azmRepository.loadByUserIdAndDateRange(session.userId(), begDate, endDate));
            if (isComplete(entities, begDate, endDate)) {
                log.debug("Found {} entities", entities.size());
            } else {
                var response = azmClient.getAzm(
                        "Bearer " + session.accessToken(),
                        begDate.toString(),
                        endDate.toString());
                entities = toSortedMap(ActiveZoneMinutesEntity::getDate,
                        response.activitiesActiveZoneMinutes().stream()
                        .map(entry -> ActiveZoneMinutesEntity.create(session.userId(), entry))
                        .toList());
                for(LocalDate date = begDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    if(!entities.containsKey(date)) {
                        entities.put(date, createEmptyDay(session.userId(), date));
                    }
                }
                log.info("Storing {} entities", entities.size());
                azmRepository.storeAll(entities.values());
            }
            return ActiveZoneMinutesViewModel.create(begDate, endDate, entities.values());
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
