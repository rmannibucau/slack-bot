package com.github.rmannibucau.slack.service;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import com.github.rmannibucau.slack.configuration.SlackConfiguration;

import lombok.Data;

@ApplicationScoped
public class WeatherService {
    @Inject
    private Client client;

    @Inject
    private SlackConfiguration configuration;

    private String[] location;

    private volatile Weather last;
    private volatile long lastUpdate;

    @PostConstruct
    private void init() {
        location = configuration.location().split(",");
    }

    public Weather getWeather() {
        if (last != null && System.currentTimeMillis() - lastUpdate < TimeUnit.MINUTES.toMillis(15)) {
            return last;
        }
        lastUpdate = System.currentTimeMillis();
        return last = configuration.openWeatherApiKey() != null ? client.target(configuration.openWeatherEndpoint())
                .queryParam("appid", configuration.openWeatherApiKey())
                .queryParam("lat", location[0])
                .queryParam("lon", location[1])
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(OpenWeather.class) : Optional.ofNullable(client.target(configuration.weatherBitEndpoint())
                .queryParam("key", configuration.weatherBitApiKey())
                .queryParam("lat", location[0])
                .queryParam("lon", location[1])
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WeatherBit.class).getData())
                .filter(d -> !d.isEmpty())
                .map(d -> d.iterator().next())
                .orElse(new WeatherBitData());
    }

    public boolean isRisky(final Weather weather) {
        final String message = weather.toMessage();
        return Stream.of(":sunny:", ":cloud:").noneMatch(message::equals);
    }

    private static String toEmoji(final int code) {
        if (code >= 200 && code < 300) {
            return ":thunder_cloud_and_rain:";
        }
        if (code >= 300 && code < 400) {
            return ":foggy:";
        }
        if (code >= 500 && code < 600) {
            return ":rain_cloud:";
        }
        if (code >= 600 && code < 700) {
            return ":snowflake:";
        }
        if ((code >= 700 && code < 800) || (code >= 900 && code < 950)) {
            return ":volcano:";
        }
        if (code > 800 && code < 900) {
            return ":cloud:";
        }
        if (code >= 957 && code < 1000) {
            return ":tornado_cloud:";
        }
        return ":sunny:";
    }

    @Data
    public static class WeatherBitData implements Weather { // https://www.weatherbit.io/api/weather-current
        @JsonbProperty("city_name")
        private String cityName;

        @JsonbProperty("app_temp")
        private Double appTemp;

        private Double clouds;
        private Double uv;
        private Double precip;
        private Double temp;
        private Double rh; // relative humidity
        private WeatherBitCode weather;

        @Override
        public String toMessage() {
            if (weather != null && weather.code != null) {
                return toEmoji(weather.code);
            }
            if (clouds != null && clouds > 10) {
                if (rh != null && rh > 50) {
                    return ":rain_cloud:";
                }
                if (precip != null && precip > 5) {
                    return ":rain_cloud:";
                }
                return ":cloud:";
            }
            return ":sunny:";
        }

        @Override
        public String location() {
            return cityName;
        }
    }

    @Data
    public static class WeatherBitCode {
        private Integer code;
    }

    @Data
    public static class WeatherBit {
        private Collection<WeatherBitData> data;
    }

    @Data // {"temp":289.5,"humidity":89,"pressure":1013,"temp_min":287.04,"temp_max":292.04}
    public static class Main {
        private int humidity;
    }

    @Data // {"id":804,"main":"clouds","description":"overcast clouds","icon":"04n"}
    public static class Prevision {
        private int id;
        private String main;
    }

    @Data
    public static class OpenWeather implements Weather {
        private String name;
        private Main main;
        private Collection<Prevision> weather;

        @Override
        public String toMessage() {
            if (weather == null || weather.isEmpty()) {
                if (main != null && main.getHumidity() > 75) {
                    return ":rain_cloud:";
                }
                return ":sunny:";
            }
            final Prevision prevision = weather.iterator().next();
            return toEmoji(prevision.id);
        }

        @Override
        public String location() {
            return name;
        }
    }

    public interface Weather {
        String toMessage();

        String location();
    }
}
