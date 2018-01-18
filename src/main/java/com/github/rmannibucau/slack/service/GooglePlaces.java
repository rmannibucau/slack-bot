package com.github.rmannibucau.slack.service;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.github.rmannibucau.slack.configuration.SlackConfiguration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class GooglePlaces {

    private final static int DEFAULT_RADIUS = 600;

    @Inject
    private SlackConfiguration conf;

    public Result getNearbyRestaurant(final String location, final Integer radius,
            final String keyword) {
        final Client client = ClientBuilder.newClient();
        try {
            final WebTarget query = client.target(conf.restaurantEndpoint())
                    .queryParam("key", conf.googleApiKey())
                    .queryParam("location", conf.location())
                    .queryParam("type", "restaurant")
                    .queryParam("radius", radius == null || radius == 0 ? DEFAULT_RADIUS : radius);

            if (keyword != null && !keyword.isEmpty()) {
                query.queryParam("keyword", keyword);
            }

            return query.request(APPLICATION_JSON_TYPE)
                    .get(Result.class);
        } finally {
            client.close();
        }

    }

    @Data
    public static class Result {

        @JsonbProperty("results")
        private List<Restaurant> restaurants;

        @JsonbProperty("next_page_token")
        private String nextPageToken;

        private String status;

        @JsonbProperty("error_message")
        private String errorMessage;
    }

    @Data
    public static class Restaurant {

        @JsonbProperty("place_id")
        private String placeId;

        private String name;

        @JsonbProperty("vicinity")
        private String address;

        private Double rating;

        @JsonbProperty("opening_hours")
        private OpeningHours openingHours;

        private Geometry geometry;
    }

    @Data
    public static class OpeningHours {

        @JsonbProperty("open_now")
        private Boolean openNow;
    }

    @Data
    public static class Geometry {

        private Location location;
    }

    @Data
    public static class Location {

        private Double lat;

        private Double lng;
    }

}
