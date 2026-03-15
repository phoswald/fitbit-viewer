package com.github.phoswald.fitbit.viewer;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/dashboard")
public class DashboardController {

    @Inject
    private Template dashboard;

    @Inject
    private FitbitApiClient apiClient;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getDashboardPage(@CookieParam("fitbit_access_token") String fitbitAccessToken) {
        FitbitProfile profile = null;
        if(fitbitAccessToken != null) {
            profile = apiClient.getUserProfile(fitbitAccessToken);
        }
        return dashboard.data("model", new DashboardViewModel(profile));
    }
}
