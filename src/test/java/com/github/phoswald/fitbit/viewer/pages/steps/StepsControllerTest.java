package com.github.phoswald.fitbit.viewer.pages.steps;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.time.LocalDate;
import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.phoswald.fitbit.viewer.auth.SessionManager;
import com.github.phoswald.fitbit.viewer.pages.BaseControllerTest;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class StepsControllerTest extends BaseControllerTest {

    @Inject
    private StepsRepository stepsRepository;

    @Test
    void getStepsPage_fromRepository_success() {
        QuarkusTransaction.requiringNew().run(this::storeEntities);

        given()
                .cookie(SessionManager.COOKIE_NAME, createCookie())
                .when()
                .param("dateBeg", "2026-09-01")
                .param("dateEnd", "2026-09-03")
                .get("/app/pages/steps")
                .then()
                .statusCode(200)
                .contentType(ContentType.HTML)
                .body(not(containsString("rstm-alert-error")))
                .body(row("2026-09-01", "9999"))
                .body(row("2026-09-02", "14616"))
                .body(row("2026-09-03", "3830"));
    }

    private void storeEntities() {
        stepsRepository.storeAll(List.of(
                createEntity(LocalDate.of(2026, 9, 1), 9999),
                createEntity(LocalDate.of(2026, 9, 2), 14616),
                createEntity(LocalDate.of(2026, 9, 3), 3830)));
    }

    private StepsEntity createEntity(LocalDate date, Integer stepCount) {
        var entity = new StepsEntity();
        entity.setUserId(USER_ID);
        entity.setDate(date);
        entity.setStepCount(stepCount);
        return entity;
    }
}
