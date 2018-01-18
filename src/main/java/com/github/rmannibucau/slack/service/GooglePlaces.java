package com.github.rmannibucau.slack.service;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rmannibucau.slack.configuration.SlackConfiguration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class GooglePlaces {

    private static final String api = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

    private static final String PARAM_LOCATION = "location";

    private static final String PARAM_RADIUS = "radius";

    private static final String PARAM_TYPE = "type";

    private static final String PARAM_KEYWORD = "keyword";

    private static final String PARAM_KEY = "key";

    private final static int DEFAULT_RADIUS = 600; //include insula :)

    //google api codes status
    private final static String STATUS_OK = "OK";

    private final static String STATUS_ZERO_RESULTS = "ZERO_RESULTS";

    private final static String STATUS_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";

    private final static String STATUS_REQUEST_DENIED = "REQUEST_DENIED";

    private final static String STATUS_INVALID_REQUEST = "INVALID_REQUEST";

    @Inject
    private SlackConfiguration conf;

    private ObjectMapper objectMapper = new ObjectMapper();

    public List<Restaurant> getNearbyRestaurant(final String location, final Integer radius, final String keyword) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(buildRequestParams(api, location, radius, keyword));
            urlConnection = HttpURLConnection.class.cast(url.openConnection());
            urlConnection.setRequestMethod("GET");
            try {
                Map<String, Object> response = objectMapper.readValue(urlConnection.getInputStream(), HashMap.class);
                if (response.containsKey("status")) {
                    if (STATUS_OK.equalsIgnoreCase(String.valueOf(response.get("status")))) {
                        if (response.containsKey("results")) {
                            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                            return results.stream().map(this::newRestaurant).collect(toList());
                        }
                    } else {
                        throw new RuntimeException("status: " + response.get("status")
                                + "error: " + response.get("error_message"));
                    }
                }
            } catch (final IOException e) {
                log.error("Problem with Google places api response", e);
                final byte[] buffer = new byte[9380]; // clear error stream
                try (final InputStream inputStream = urlConnection.getErrorStream()) {
                    while (inputStream.read(buffer) >= 0)
                        ;
                } catch (final IOException e1) {
                    throw new IllegalStateException(e1);
                }
            }
        } catch (IOException e) {
            log.error("unexpected exception", e);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return emptyList();
    }

    private Restaurant newRestaurant(final Map<String, Object> r) {
        Restaurant restaurant = new Restaurant();
        restaurant.setPlaceId(String.valueOf(r.get("place_id")));
        restaurant.setName(String.valueOf(r.get("name")));
        restaurant.setAddress(String.valueOf(r.get("vicinity")));
        if (r.containsKey("rating")) {
            restaurant.setRating(Double.parseDouble(String.valueOf(r.get("rating"))));
        }
        if (r.containsKey("geometry")) {
            final Map<String, Object> geo = (Map<String, Object>) r.get("geometry");
            if (geo.containsKey("location")) {
                final Map<String, Object> loc = (Map<String, Object>) geo.get("location");
                restaurant.setLat(String.valueOf(loc.get("lat")));
                restaurant.setLng(String.valueOf(loc.get("lng")));
            }
        }

        if (r.containsKey("opening_hours")) {
            Map<String, Object> openingHours = (Map<String, Object>) r.get("opening_hours");
            if (openingHours.containsKey("open_now")) {
                restaurant.setIsOpenNow(
                        Boolean.parseBoolean(String.valueOf(openingHours.get("open_now"))));
            }
        }

        return restaurant;
    }

    private String buildRequestParams(final String api, final String location, final Integer radius, final String
            keyword) {
        StringBuilder sb = new StringBuilder(api);
        sb.append("?")
                .append(PARAM_KEY)
                .append("=")
                .append(conf.googleApiKey());
        sb.append("&")
                .append(PARAM_TYPE)
                .append("=")
                .append("restaurant");

        //
        int r = radius == null || radius == 0 ? DEFAULT_RADIUS : radius;
        sb.append("&")
                .append(PARAM_RADIUS)
                .append("=")
                .append(r);

        String loc = location == null || location.isEmpty() ? conf.location() : location;
        sb.append("&")
                .append(PARAM_LOCATION)
                .append("=")
                .append(loc);

        if (keyword != null && !keyword.isEmpty()) {
            sb.append("&")
                    .append(PARAM_KEYWORD)
                    .append("=")
                    .append(keyword);
        }

        return sb.toString();
    }

    @Data
    public static class Restaurant {

        private String placeId;

        private String name;

        private String address;

        private Double rating;

        private Boolean isOpenNow;

        private String lat;

        private String lng;
    }

}
