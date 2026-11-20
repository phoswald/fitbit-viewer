package com.github.phoswald.fitbit.viewer.pages.profile;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.phoswald.fitbit.viewer.auth.SessionManager;
import com.github.phoswald.fitbit.viewer.pages.BaseControllerTest;
import com.github.phoswald.fitbit.viewer.repository.ProfileEntity;
import com.github.phoswald.fitbit.viewer.repository.ProfileRepository;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class ProfileControllerTest extends BaseControllerTest {

    @Inject
    private ProfileRepository profileRepository;

    @Test
    void getProfilePage_fromRepository_success() {
        QuarkusTransaction.requiringNew().run(this::storeEntities);

        given()
                .cookie(SessionManager.COOKIE_NAME, createCookie())
                .when()
                .get("/app/pages/profile")
                .then()
                .statusCode(200)
                .contentType(ContentType.HTML)
                .body(not(containsString("rstm-alert-error")))
                .body(row("Display Name", "Philip O."))
                .body(row("Full Name", "Philip Oswald"))
                .body(row("Date of Birth", "1977-10-26"))
                .body(row("Age", "48"))
                .body(row("Gender", "MALE"))
                .body(row("Member since", "2019-12-04"))
                .body(row("Height", "178 cm"))
                .body(row("Weight", "85 kg"))
                .body(row("Stride Length Walking", "73.8 cm"))
                .body(row("Stride Length Running", "100.9 cm"))
                .body(row("Steps", "13474 (daily average)"))
                .body(row("User ID", USER_ID));
    }

    private void storeEntities() {
        var entity = new ProfileEntity();
        entity.setUserId(USER_ID);
        entity.setDisplayName("Philip O.");
        entity.setFullName("Philip Oswald");
        entity.setDateOfBirth("1977-10-26");
        entity.setAge(48);
        entity.setGender("MALE");
        entity.setMemberSince("2019-12-04");
        entity.setHeight(178);
        entity.setWeight(85);
        entity.setStrideLengthWalking(73.8);
        entity.setStrideLengthRunning(100.9);
        entity.setAverageDailySteps("13474");
        profileRepository.store(entity);
    }
}
