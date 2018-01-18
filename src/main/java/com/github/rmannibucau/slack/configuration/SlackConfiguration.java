package com.github.rmannibucau.slack.configuration;

import java.util.List;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.config.Configuration;

@Configuration(prefix = "talend.slack.bot.")
public interface SlackConfiguration {

    @ConfigProperty(name = "token")
    String token();

    @ConfigProperty(name = "google.api.key")
    String googleApiKey();

    @ConfigProperty(name = "location", defaultValue = "47.204176,-1.5685397") //default from Nantes office
    String location();

    @ConfigProperty(name = "healthcheck.timeout", defaultValue = "30000")
    Long healthcheckTimeout();

    @ConfigProperty(name = "endpoint.connect", defaultValue = "https://slack.com/api/rtm.connect")
    String rtmConnectEndpoint();

    @ConfigProperty(name = "endpoint.postMessage", defaultValue = "https://slack.com/api/chat.postMessage")
    String postMessageEndpoint();

    @ConfigProperty(name = "endpoint.restaurant", defaultValue = "https://maps.googleapis.com/maps/api/place/nearbysearch/json")
    String restaurantEndpoint();

    @ConfigProperty(name = "endpoint.ping")
    String pingEndpoint();

    @ConfigProperty(name = "channels")
    List<String> defaultChannels();
}
