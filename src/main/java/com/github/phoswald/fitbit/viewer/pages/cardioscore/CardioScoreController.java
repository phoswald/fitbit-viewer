package com.github.phoswald.fitbit.viewer.pages.cardioscore;

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
import com.github.phoswald.fitbit.viewer.fitbitapi.CardioScoreApiClient;
import com.github.phoswald.fitbit.viewer.pages.PageController;
import com.github.phoswald.fitbit.viewer.repository.CardioScoreEntity;
import com.github.phoswald.fitbit.viewer.repository.CardioScoreRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/cardioscore")
public class CardioScoreController extends PageController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template cardioscore;

    @Inject
    @RestClient
    private CardioScoreApiClient cardioScoreClient;

    @Inject
    private CardioScoreRepository cardioScoreRepository;

    @QueryParam("begDate")
    private LocalDate begDate;

    @QueryParam("endDate")
    private LocalDate endDate;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getCardioScorePage() {
        if (begDate == null || endDate == null) {
            begDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now();
        }
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return cardioscore.data("model", createCardioScoreViewModel(session.get(), begDate, endDate));
        } else {
            return cardioscore.data("model", CardioScoreViewModel.createError("You are not logged in."));
        }
    }

    private CardioScoreViewModel createCardioScoreViewModel(SessionData session, LocalDate begDate, LocalDate endDate) {
        try {
            log.info("Querying: begDate={}, endDate={}", begDate, endDate);
            List<CardioScoreEntity> entities = cardioScoreRepository.loadByUserIdAndDateRange(session.userId(), begDate, endDate);
            if (isComplete(entities, begDate, endDate)) {
                log.debug("Found {} entities", entities.size());
            } else {
                var response = cardioScoreClient.getCardioScore(
                        "Bearer " + session.accessToken(),
                        begDate.toString(),
                        endDate.toString());
                entities = response.cardioScore().stream()
                        .map(entry -> CardioScoreEntity.create(session.userId(), entry))
                        .toList();
                log.info("Storing {} entities", entities.size());
                cardioScoreRepository.storeAll(entities);
            }
            return CardioScoreViewModel.create(begDate, endDate, entities);
        } catch (Exception e) {
            log.warn("Failed", e);
            return CardioScoreViewModel.createError(e.getMessage());
        }
    }
}
