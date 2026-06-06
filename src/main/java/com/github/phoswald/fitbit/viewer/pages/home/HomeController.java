package com.github.phoswald.fitbit.viewer.pages.home;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.pages.BaseController;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/home")
class HomeController extends BaseController {

    @Inject
    private Template home;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getHomePage() {
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        String userId = session.map(SessionData::userId).orElse(null);
        return home.data("model", HomeViewModel.create(userId));
    }
}
