package com.github.phoswald.fitbit.viewer.pages.cardioscore;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.min;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.fitbitapi.CardioScoreApiClient;
import com.github.phoswald.fitbit.viewer.pages.DateRangeController;
import com.github.phoswald.fitbit.viewer.repository.CardioScoreEntity;
import com.github.phoswald.fitbit.viewer.repository.CardioScoreRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/cardioscore")
public class CardioScoreController extends DateRangeController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template cardioscore;

    @Inject
    @RestClient
    private CardioScoreApiClient cardioScoreClient;

    @Inject
    private CardioScoreRepository cardioScoreRepository;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getCardioScorePage() {
        normalizeDateRange();
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return cardioscore.data("model", createCardioScoreViewModel(session.get()));
        } else {
            return cardioscore.data("model", CardioScoreViewModel.createError("You are not logged in."));
        }
    }

    private CardioScoreViewModel createCardioScoreViewModel(SessionData session) {
        try {
            log.info("Querying: dateBeg={}, dateEnd={}, datePeriod={}", dateBeg, dateEnd, datePeriod);
            var cardioScores = cardioScoreRepository.loadByUserIdAndDateRange(session.userId(), dateBeg, dateEnd).stream()
                    .collect(toSortedMap(CardioScoreEntity::getDate));
            if (!refresh && isComplete(cardioScores)) {
                log.debug("Found {} entities", cardioScores.size());
            } else {
                cardioScores.clear();
                for(var dateRange : getQueryDateRanges()) {
                    log.debug("Querying (current): dateBeg={}, dateEnd={}", dateRange.beg(), dateRange.end());
                    var response = cardioScoreClient.getCardioScore(
                            "Bearer " + session.accessToken(),
                            dateRange.beg().toString(),
                            dateRange.end().toString());
                    cardioScores.putAll(response.cardioScore().stream()
                            .map(entry -> CardioScoreEntity.create(session.userId(), entry))
                            .collect(toSortedMap(CardioScoreEntity::getDate)));
                }
                for(LocalDate date = dateBeg; !date.isAfter(dateEnd); date = date.plusDays(1)) {
                    if(!cardioScores.containsKey(date)) {
                        cardioScores.put(date, createEmptyDay(session.userId(), date));
                    }
                }
                log.info("Storing {} entities", cardioScores.size());
                cardioScoreRepository.storeAll(cardioScores.values());
            }
            return CardioScoreViewModel.create(createDateRangeViewModel(), cardioScores.values());
        } catch (Exception e) {
            log.warn("Failed", e);
            return CardioScoreViewModel.createError(e.getMessage());
        }
    }

    private List<DateRange> getQueryDateRanges() {
        var result = new ArrayList<DateRange>();
        LocalDate curBeg = dateBeg;
        while (!curBeg.isAfter(dateEnd)) {
            LocalDate curEnd = min(dateEnd, curBeg.plusMonths(1).minusDays(1));
            result.add(new DateRange(curBeg, curEnd));
            curBeg = curEnd.plusDays(1);
        }
        return result;
    }

    private CardioScoreEntity createEmptyDay(String userId, LocalDate date) {
        log.debug("Filling gap: {}", date);
        CardioScoreEntity entity = new CardioScoreEntity();
        entity.setUserId(userId);
        entity.setDate(date);
        return entity;
    }

    private record DateRange(LocalDate beg, LocalDate end) { }
}
