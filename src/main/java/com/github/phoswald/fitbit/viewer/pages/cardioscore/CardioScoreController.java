package com.github.phoswald.fitbit.viewer.pages.cardioscore;

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

    @QueryParam("refresh")
    private boolean refresh;

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
            return cardioscore.data("model", createCardioScoreViewModel(session.get()));
        } else {
            return cardioscore.data("model", CardioScoreViewModel.createError("You are not logged in."));
        }
    }

    private CardioScoreViewModel createCardioScoreViewModel(SessionData session) {
        try {
            log.info("Querying: begDate={}, endDate={}", begDate, endDate);
            var cardioScores = cardioScoreRepository.loadByUserIdAndDateRange(session.userId(), begDate, endDate).stream()
                    .collect(toSortedMap(CardioScoreEntity::getDate));
            if (!refresh && isComplete(cardioScores, begDate, endDate)) {
                log.debug("Found {} entities", cardioScores.size());
            } else {
                var response = cardioScoreClient.getCardioScore(
                        "Bearer " + session.accessToken(),
                        begDate.toString(),
                        endDate.toString());
                cardioScores = response.cardioScore().stream()
                        .map(entry -> CardioScoreEntity.create(session.userId(), entry))
                        .collect(toSortedMap(CardioScoreEntity::getDate));
                for(LocalDate date = begDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                    if(!cardioScores.containsKey(date)) {
                        cardioScores.put(date, createEmptyDay(session.userId(), date));
                    }
                }
                log.info("Storing {} entities", cardioScores.size());
                cardioScoreRepository.storeAll(cardioScores.values());
            }
            return CardioScoreViewModel.create(begDate, endDate, cardioScores.values());
        } catch (Exception e) {
            log.warn("Failed", e);
            return CardioScoreViewModel.createError(e.getMessage());
        }
    }

    private CardioScoreEntity createEmptyDay(String userId, LocalDate date) {
        log.debug("Filling gap: {}", date);
        CardioScoreEntity entity = new CardioScoreEntity();
        entity.setUserId(userId);
        entity.setDate(date);
        return entity;
    }
}
