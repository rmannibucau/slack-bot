package com.github.rmannibucau.slack.service.command.impl;

import com.github.rmannibucau.slack.service.GooglePlaces;
import com.github.rmannibucau.slack.service.command.api.Command;
import com.github.rmannibucau.slack.websocket.Message;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

@ApplicationScoped
@Command(value = "next", alias = { "autre", "suivant" })
public class OtherRestaurantCommand implements Function<Message, String> {

    @Inject
    private GooglePlaces googlePlaces;

    @Inject
    private RestaurantCommand restaurantCommand;

    @Override
    public String apply(final Message message) {
        return ofNullable(googlePlaces.getLast())
                .map(googlePlaces::select)
                .map(googlePlaces::toMessage)
                .orElseGet(() -> {
                    final Message msg = new Message();
                    msg.setText("restaurant");
                    return restaurantCommand.apply(msg);
                });
    }
}
