package com.github.phoswald.fitbit.viewer.pages.azm;

import java.time.LocalDate;
import java.util.List;

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
import com.github.phoswald.fitbit.viewer.fitbitapi.AzmApiClient;
import com.github.phoswald.fitbit.viewer.pages.PageController;
import com.github.phoswald.fitbit.viewer.repository.AzmEntity;
import com.github.phoswald.fitbit.viewer.repository.AzmRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/azm")
public class AzmController extends PageController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template azm;

    @Inject
    @RestClient
    private AzmApiClient azmClient;

    @Inject
    private AzmRepository azmRepository;

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
            return azm.data("model", createAzmViewModel(session.get(), begDate, endDate));
        } else {
            return azm.data("model", AzmViewModel.createError("You are not logged in."));
        }
    }

    private AzmViewModel createAzmViewModel(SessionData session, LocalDate begDate, LocalDate endDate) {
        try {
            log.info("Querying: begDate={}, endDate={}", begDate, endDate);
            List<AzmEntity> entities = azmRepository.loadByUserIdAndDateRange(session.userId(), begDate, endDate);
            if (isComplete(entities, begDate, endDate)) {
                log.debug("Found {} entities", entities.size());
            } else {
                var response = azmClient.getAzm(
                        "Bearer " + session.accessToken(),
                        begDate.toString(),
                        endDate.toString());
                entities = response.activitiesActiveZoneMinutes().stream()
                        .map(entry -> AzmEntity.create(session.userId(), entry))
                        .toList();
                log.info("Storing {} entities", entities.size());
                azmRepository.storeAll(entities);
            }
            return AzmViewModel.create(begDate, endDate, entities);
        } catch (Exception e) {
            log.warn("Failed", e);
            return AzmViewModel.createError(e.getMessage());
        }
    }
}
