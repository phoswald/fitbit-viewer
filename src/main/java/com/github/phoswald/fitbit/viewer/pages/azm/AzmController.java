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

import com.github.phoswald.fitbit.viewer.fitbitapi.AzmApiClient;
import com.github.phoswald.fitbit.viewer.pages.PageController;

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
            try {
                log.info("getAzmPage(): begDate={}, endDate={}", begDate, endDate);
                var response = azmClient.getAzm("Bearer " + session.get().accessToken(), begDate.toString(), endDate.toString());
                return azm.data("model", AzmViewModel.create(begDate, endDate, response.activitiesActiveZoneMinutes()));
            } catch (Exception e) {
                log.warn("getAzmPage(): failed", e);
                return azm.data("model", AzmViewModel.createError(e.getMessage()));
            }
        } else {
            return azm.data("model", AzmViewModel.createError("You are not logged in."));
        }
    }
}
