package com.github.phoswald.fitbit.viewer.pages.profile;

import java.util.Optional;

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
import com.github.phoswald.fitbit.viewer.fitbitapi.ProfileApiClient;
import com.github.phoswald.fitbit.viewer.pages.PageController;
import com.github.phoswald.fitbit.viewer.repository.ProfileEntity;
import com.github.phoswald.fitbit.viewer.repository.ProfileRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/profile")
public class ProfileController extends PageController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template profile;

    @Inject
    @RestClient
    private ProfileApiClient profileClient;

    @Inject
    private ProfileRepository profileRepository;

    @QueryParam("errorMessage")
    private String errorMessage;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getProfilePage() {
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return profile.data("model", createProfileViewModel(session.get()));
        } else {
            return profile.data("model", ProfileViewModel.createError(errorMessage));
        }
    }

    private ProfileViewModel createProfileViewModel(SessionData session) {
        try {
            log.info("Querying: userId={}", session.userId());
            var entity = profileRepository.loadByUserId(session.userId());
            if(entity.isPresent()) {
                log.debug("Found entity");
            } else {
                var response = profileClient.getProfile("Bearer " + session.accessToken());
                entity = Optional.of(ProfileEntity.create(response.user()));
                log.info("Storing entity");
                profileRepository.store(entity.get());
            }
            return ProfileViewModel.create(entity.get());
        } catch (Exception e) {
            log.warn("Failed", e);
            return ProfileViewModel.createError(e.getMessage());
        }
    }
}
