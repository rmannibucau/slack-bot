package com.github.rmannibucau.slack.configuration;

import java.util.List;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.apache.deltaspike.core.api.config.Configuration;

@Configuration(prefix = "talend.slack.bot.")
public interface SlackConfiguration {

    @ConfigProperty(name = "slack.api.token")
    String slackToken();

    @ConfigProperty(name = "google.api.key")
    String googleApiKey();

    @ConfigProperty(name = "openweather.api.key")
    String openWeatherApiKey();

    @ConfigProperty(name = "openweather.api.key")
    String weatherBitApiKey();

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

    @ConfigProperty(name = "endpoint.photo", defaultValue = "https://maps.googleapis.com/maps/api/place/photo")
    String photoEndpoint();

    @ConfigProperty(name = "endpoint.openweather", defaultValue = "https://api.openweathermap.org/data/2.5/weather")
    String openWeatherEndpoint();

    @ConfigProperty(name = "endpoint.openweather", defaultValue = "https://api.weatherbit.io/v2.0/current")
    String weatherBitEndpoint();

    @ConfigProperty(name = "endpoint.ping")
    String pingEndpoint();

    @ConfigProperty(name = "channels")
    List<String> defaultChannels();
}
