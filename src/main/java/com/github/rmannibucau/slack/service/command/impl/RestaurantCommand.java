package com.github.rmannibucau.slack.service.command.impl;

import com.github.rmannibucau.slack.service.GooglePlaces;
import com.github.rmannibucau.slack.service.command.api.Command;
import com.github.rmannibucau.slack.websocket.Message;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Stream;

@Command(value = "restaurant", alias = { "on mange ou", "on mange où" })
@ApplicationScoped
public class RestaurantCommand implements Function<Message, String> {

    @Inject
    private GooglePlaces googlePlaces;

    private String[] texts;

    @PostConstruct
    private void init() {
        final Command command = getClass().getAnnotation(Command.class);
        texts = Stream.concat(Stream.of(command.value()), Stream.of(command.alias()))
                .toArray(String[]::new);
    }

    @Override
    public String apply(final Message message) {
        final GooglePlaces.Result result = googlePlaces.getNearbyRestaurant(null,
                null,
                Stream.of(texts).filter(t -> message.getText().contains(t)).findAny()
                        .map(t -> message.getText().replace(t, "").trim()
                                .replace(" de ", "")
                                .replace(" des ", ""))
                        .orElse(null));
        switch (result.getStatus()) {
        case "ZERO_RESULTS":
            return "Désolé, je n'ai pas trouvé de bonne proposition répondant à tes critères :cold_sweat:";
        case "OVER_QUERY_LIMIT":
            return "Je ne peux plus chercher, j'ai dépassé mes limites aujourd'hui :tired_face:";
        case "OK":
            if (result.getRestaurants() == null || result.getRestaurants().isEmpty()) {
                return "J'ai pas trouvé de restaurants :(";
            }
            return googlePlaces.toMessage(googlePlaces.select(result));
        case "REQUEST_DENIED":
        case "INVALID_REQUEST":
        default:
            return "Ta demande est invalide :sweat_smile:";
        }
    }
}
