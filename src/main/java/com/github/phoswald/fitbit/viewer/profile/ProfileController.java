package com.github.phoswald.fitbit.viewer.profile;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;

import com.github.phoswald.fitbit.viewer.login.SessionManager;

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
@Path("/pages/profile")
public class ProfileController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template profile;

    @Inject
    @RestClient
    private ProfileApiClient profileClient;

    @Inject
    private SessionManager sessionManager;

    @CookieParam(SessionManager.COOKIE_NAME)
    private String sessionCookie;

    @QueryParam("errorMessage")
    private String errorMessage;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getProfilePage() {
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            try {
                var profile = profileClient.getProfile("Bearer " + session.get().accessToken());
                return this.profile.data("model", ProfileViewModel.create(profile.user()));
            } catch (Exception e) {
                log.warn("getProfile: failed to fetch steps", e);
                return profile.data("model", ProfileViewModel.createError(e.getMessage()));
            }
        } else {
            return profile.data("model", ProfileViewModel.createError(errorMessage));
        }
    }
}
