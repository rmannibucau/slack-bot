package com.github.rmannibucau.slack.configuration;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.config.Configuration;

@Configuration(prefix = "talend.slack.bot.")
public interface SlackConfiguration {
    @ConfigProperty(name = "token")
    String token();

    @ConfigProperty(name = "healthcheck.timeout", defaultValue = "30000")
    Long healthcheckTimeout();

    @ConfigProperty(name = "endpoint.connect", defaultValue = "https://slack.com/api/rtm.connect")
    String rtmConnectEndpoint();

    @ConfigProperty(name = "endpoint.postMessage", defaultValue = "https://slack.com/api/chat.postMessage")
    String postMessageEndpoint();
}