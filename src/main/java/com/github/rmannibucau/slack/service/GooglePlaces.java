package com.github.rmannibucau.slack.service;

import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import com.github.rmannibucau.slack.configuration.SlackConfiguration;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class GooglePlaces {

    private final static int RAIN_DEFAULT_RADIUS = 600;
    private final static int DEFAULT_RADIUS = 800;

    @Inject
    private WeatherService weather;

    @Inject
    private SlackConfiguration conf;

    @Inject
    private Client client;

    @Getter
    private volatile Result last;

    // not a ThreadLocalRandom since we have a low concurrency
    private final Random random = new Random(System.currentTimeMillis());

    public Restaurant select(final Result result) {
        return result.getRestaurants().get(random.nextInt(result.getRestaurants().size()));
    }

    public String toMessage(final Restaurant choice) {
        final WeatherService.Weather weather = this.weather.getWeather();
        String response = "*" + choice.getName() + "*";
        if (choice.getRating() != null && choice.getRating() > 0) {
            response += "\nRating: (" + choice.getRating() + ") "
                    + IntStream.rangeClosed(0, choice.getRating().intValue())
                    .mapToObj(i -> ":star:")
                    .collect(joining(""));
        }
        response += "\nMétéo: " + Optional.ofNullable(weather.location()).orElse("-") + " " + weather.toMessage();
        response += "\n_" + choice.getAddress() + "_";
        if (choice.getGeometry() != null && choice.getGeometry().getLocation() != null) {
            response += "\nhttps://www.google.fr/maps/@" + choice.getGeometry().getLocation().getLat() + ","
                    + choice.getGeometry().getLocation().getLng() + ",20z";// zoom level
        }
        response += Optional.ofNullable(choice.getPhotos())
                .filter(p -> !p.isEmpty())
                .map(p -> p.iterator().next())
                .map(p -> "\n" + getPhoto(p))
                .orElse("");

        return response;
    }

    public Result getNearbyRestaurant(final String location, final Integer radius,
                                      final String userQuery) {
        WebTarget query = client.target(conf.restaurantEndpoint())
                .queryParam("key", conf.googleApiKey())
                .queryParam("location", conf.location())
                .queryParam("type", "restaurant")
                .queryParam("radius", radius == null || radius == 0 ? getDefaultRadius(weather.getWeather()) : radius);

        if (userQuery != null && !userQuery.isEmpty()) {
            query = query.queryParam("query", userQuery);
        }

        final Result result = query.request(APPLICATION_JSON_TYPE).get(Result.class);
        if (result.getRestaurants() != null && !result.getRestaurants().isEmpty() && "OK".equals(result.getStatus())) {
            last = result;
        }
        return result;
    }

    private int getDefaultRadius(final WeatherService.Weather weather) {
        try {
            return this.weather.isRisky(weather) ? RAIN_DEFAULT_RADIUS : DEFAULT_RADIUS;
        } catch (final WebApplicationException wae) {
            log.warn(wae.getMessage());
            return DEFAULT_RADIUS;
        }
    }

    private String getPhoto(final Photo photo) {
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
