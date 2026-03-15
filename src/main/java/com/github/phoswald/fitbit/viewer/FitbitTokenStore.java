package com.github.phoswald.fitbit.viewer;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FitbitTokenStore { // TODO: move this to session/cookie

    private volatile String accessToken;

    public boolean hasToken() {
        return accessToken != null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
