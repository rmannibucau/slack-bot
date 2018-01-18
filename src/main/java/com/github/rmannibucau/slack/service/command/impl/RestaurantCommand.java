package com.github.rmannibucau.slack.service.command.impl;

import static java.util.stream.Collectors.joining;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.rmannibucau.slack.service.GooglePlaces;
import com.github.rmannibucau.slack.service.command.api.Command;
import com.github.rmannibucau.slack.websocket.Message;

@Command(value = "restaurant", alias = { "on mange ou", "on mange où" })
@ApplicationScoped
public class RestaurantCommand implements Function<Message, String> {

    @Inject
    private GooglePlaces googlePlaces;

    @Override
    public String apply(final Message message) {
        final int keywordIndex = message.getText().indexOf(" recherche ");
        final GooglePlaces.Result result = googlePlaces.getNearbyRestaurant(null,
                null,
                keywordIndex >= 0 ? message.getText().substring(keywordIndex).trim() : null);
        switch (result.getStatus()) {
        case "ZERO_RESULTS":
            return "Désolé, je n'ai pas trouvé de bonne proposition répondant à tes critères :cold_sweat:";
        case "OVER_QUERY_LIMIT":
            return "Je ne peux plus chercher, j'ai dépassé mes limites aujourd'hui :tired_face:";
        case "OK":
            // todo improve with :
            // - weather service to pass a more accurate radius..
            // - with history of previous provided restaurant
            // - support key words like : pizza, burger, sushi...
            // - use the rating
            int randomRestaurant = ThreadLocalRandom.current().nextInt(0, result.getRestaurants().size() + 1);
            final GooglePlaces.Restaurant choice = result.getRestaurants().get(randomRestaurant);
            String response = "*" + choice.getName() + "*";
            if (choice.getRating() != null && choice.getRating() > 0) {
                response += "\nRating: (" + choice.getRating() + ") "
                        + IntStream.range(0, choice.getRating().intValue())
                        .mapToObj(i -> ":star:")
                        .collect(joining(""));
            }
            response += "\n_" + choice.getAddress() + "_";
            if (choice.getGeometry() != null && choice.getGeometry().getLocation() != null) {
                response += "\nhttps://www.google.fr/maps/@" + choice.getGeometry().getLocation().getLat() + ","
                        + choice.getGeometry().getLocation().getLng() + ",20z";// zoom level
            }
            response += Optional.ofNullable(choice.getPhotos())
                    .filter(p -> !p.isEmpty())
                    .map(p -> p.iterator().next())
                    .map(p -> "\n" + googlePlaces.getPhoto(p))
                    .orElse("");

            return response;
        case "REQUEST_DENIED":
        case "INVALID_REQUEST":
        default:
            return "Ta demande est invalide :sweat_smile:";
        }
    }
}
