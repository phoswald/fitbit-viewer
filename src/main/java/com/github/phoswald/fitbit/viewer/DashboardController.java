package com.github.phoswald.fitbit.viewer;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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

    @QueryParam("errorMessage")
    private String errorMessage;

    @CookieParam("fitbitAccessToken")
    private String fitbitAccessToken;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getDashboardPage() {
        FitbitProfile profile = null;
        if(fitbitAccessToken != null) {
            profile = apiClient.getUserProfile(fitbitAccessToken);
        }
        return dashboard.data("model", new DashboardViewModel(errorMessage, profile));
    }
}
