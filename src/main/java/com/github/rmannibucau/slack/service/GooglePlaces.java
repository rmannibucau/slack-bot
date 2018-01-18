package com.github.rmannibucau.slack.service;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.client.Client;
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

    @Inject
    private Client client;

    public Result getNearbyRestaurant(final String location, final Integer radius,
            final String keyword) {
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
    }

    public String getPhoto(Photo photo) {
        return client.target(conf.photoEndpoint())
                .queryParam("key", conf.googleApiKey())
                .queryParam("maxwidth", Math.min(150, photo.getWidth()))
                .queryParam("photoreference", photo.getReference())
                .request()
                .get()
                .getHeaderString("location");
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

        private Collection<Photo> photos;
    }

    @Data
    public static class Photo {

        @JsonbProperty("photo_reference")
        private String reference;

        private int height;

        private int width;
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
