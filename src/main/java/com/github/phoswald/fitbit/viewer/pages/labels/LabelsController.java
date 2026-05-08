package com.github.phoswald.fitbit.viewer.pages.labels;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.pages.BaseController;
import com.github.phoswald.fitbit.viewer.repository.ActivityRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/labels")
class LabelsController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template labels;

    @Inject
    private ActivityRepository activityRepository;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getLabelsPage() {
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return labels.data("model", createLabelsViewModel(session.get()));
        } else {
            return labels.data("model", LabelsViewModel.createError("You are not logged in."));
        }
    }

    private LabelsViewModel createLabelsViewModel(SessionData session) {
        try {
            var summaries = activityRepository.loadLabelSummariesByUserId(session.userId());
            return LabelsViewModel.create(summaries);
        } catch (Exception e) {
            log.warn("Failed", e);
            return LabelsViewModel.createError(e.getMessage());
        }
    }
}
