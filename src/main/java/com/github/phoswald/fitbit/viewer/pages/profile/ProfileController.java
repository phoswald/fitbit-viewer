package com.github.phoswald.fitbit.viewer.pages.profile;

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

import com.github.phoswald.fitbit.viewer.fitbitapi.ProfileApiClient;
import com.github.phoswald.fitbit.viewer.pages.PageController;

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

    @QueryParam("errorMessage")
    private String errorMessage;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getProfilePage() {
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            try {
                log.info("Querying: userId={}", session.get().userId());
                var profile = profileClient.getProfile("Bearer " + session.get().accessToken());
                return this.profile.data("model", ProfileViewModel.create(profile.user()));
            } catch (Exception e) {
                log.warn("Failed", e);
                return profile.data("model", ProfileViewModel.createError(e.getMessage()));
            }
        } else {
            return profile.data("model", ProfileViewModel.createError(errorMessage));
        }
    }
}
