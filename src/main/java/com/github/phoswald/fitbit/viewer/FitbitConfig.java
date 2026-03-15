package com.github.phoswald.fitbit.viewer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class FitbitConfig {

    @Inject
    @ConfigProperty(name = "fitbit.client-id")
    private String clientId;

    @Inject
    @ConfigProperty(name = "fitbit.client-secret")
    private String clientSecret;

    @Inject
    @ConfigProperty(name = "fitbit.redirect-uri")
    private String redirectUri;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
