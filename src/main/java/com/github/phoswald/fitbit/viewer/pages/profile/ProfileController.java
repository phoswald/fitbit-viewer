package com.github.phoswald.fitbit.viewer.pages.profile;

import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.heightCmOf;
import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.weightKgOf;

import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.healthapi.HealthApiClient;
import com.github.phoswald.fitbit.viewer.pages.BaseController;
import com.github.phoswald.fitbit.viewer.repository.ProfileEntity;
import com.github.phoswald.fitbit.viewer.repository.ProfileRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/profile")
public class ProfileController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template profile;

    @Inject
    private HealthApiClient healthApiClient;

    @Inject
    private ProfileRepository profileRepository;

    @QueryParam("errorMessage")
    private String errorMessage;

    @QueryParam("refresh")
    private boolean refresh;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getProfilePage() {
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return profile.data("model", createProfileViewModel(session.get()));
        } else if(errorMessage != null) {
            return profile.data("model", ProfileViewModel.createError(errorMessage));
        } else {
            return profile.data("model", ProfileViewModel.createError("You are not logged in."));
        }
    }

    private ProfileViewModel createProfileViewModel(SessionData session) {
        try {
            log.info("Querying: userId={}", session.userId());
            Optional<ProfileEntity> profile = profileRepository.loadByUserId(session.userId());
            if(!refresh && profile.isPresent()) {
                log.debug("Found entity");
            } else {
                var response = healthApiClient.getProfile(session);
                var height = healthApiClient.getFirstOfList(session, "height");
                var weight = healthApiClient.getFirstOfList(session, "weight");
                profile = Optional.of(ProfileEntity.create(session, response, heightCmOf(height), weightKgOf(weight)));
                log.info("Storing entity");
                profileRepository.store(profile.get());
            }
            return ProfileViewModel.create(profile.get(), session.userId());
        } catch (Exception e) {
            log.warn("Failed", e);
            return ProfileViewModel.createError(e.getMessage());
        }
    }
}
