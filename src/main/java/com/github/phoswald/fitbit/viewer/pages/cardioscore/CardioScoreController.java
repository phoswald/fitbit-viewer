package com.github.phoswald.fitbit.viewer.pages.cardioscore;

import java.time.LocalDate;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;

import com.github.phoswald.fitbit.viewer.auth.SessionManager;
import com.github.phoswald.fitbit.viewer.fitbitapi.CardioScoreApiClient;

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
@Path("/pages/cardioscore")
public class CardioScoreController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template cardioscore;

    @Inject
    @RestClient
    private CardioScoreApiClient cardioScoreClient;

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
    public TemplateInstance getCardioScorePage() {
        if (begDate == null || endDate == null) {
            begDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now();
        }
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            try {
                log.info("getCardioScorePage(): begDate={}, endDate={}", begDate, endDate);
                var response = cardioScoreClient.getCardioScore("Bearer " + session.get().accessToken(), begDate.toString(), endDate.toString());
                return cardioscore.data("model", CardioScoreViewModel.create(begDate, endDate, response.cardioScore()));
            } catch (Exception e) {
                log.warn("getCardioScorePage(): failed", e);
                return cardioscore.data("model", CardioScoreViewModel.createError(e.getMessage()));
            }
        } else {
            return cardioscore.data("model", CardioScoreViewModel.createError("You are not logged in."));
        }
    }
}
