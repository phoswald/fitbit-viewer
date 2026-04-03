package com.github.phoswald.fitbit.viewer.profile;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
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

    @QueryParam("errorMessage")
    private String errorMessage;

    @CookieParam("fitbitAccessToken")
    private String accessToken;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getProfilePage() {
        if(accessToken != null) {
            try {
                var profile = profileClient.getProfile("Bearer " + accessToken);
                return this.profile.data("model", ProfileViewModel.create(profile.user()));
            } catch (Exception e) {
                log.warn("getProfile: failed to fetch steps", e);
                return profile.data("model", ProfileViewModel.createError(e.getMessage()));
            }
        } else {
            return profile.data("model", ProfileViewModel.createError(null));
        }
    }
}
