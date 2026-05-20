package com.github.phoswald.fitbit.viewer;

import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.qute.TemplateGlobal;

class TemplateGlobals {

    @TemplateGlobal
    static String basePath() {
        return ConfigProvider.getConfig()
                .getOptionalValue("quarkus.http.root-path", String.class)
                .orElse("");
    }
}
