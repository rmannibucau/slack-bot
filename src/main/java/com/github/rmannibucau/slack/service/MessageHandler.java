package com.github.rmannibucau.slack.service;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRANCE;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.rmannibucau.slack.websocket.Message;

@ApplicationScoped
public class MessageHandler {

    @Inject
    private GooglePlaces googlePlaces;

    public String createResponse(final Message message) {
        switch (getCommand(message)) {
        case "empty":
            return "Oui ! Comment je peux t'aider ?\n"
                    + "_Pour le moment je peux te proposer des restaurants_\n"
                    + "Tu peux me demander: '_on mange où ?_'";
        case "joke":
            return "Trouve-toi des amis pour discuter avec eux ! :trollface:\n"
                    + "_Moi, je sais proposer que des restaurants_";
        case "findRestaurant":
            //todo improve with :
            // - weather service to pass a more accurate radius..
            //- with history of previous provided restaurant
            //- support key words like : pizza, burger, sushi...
            //- use the rating
            final List<GooglePlaces.Restaurant> restaurants = googlePlaces.getNearbyRestaurant(null, null, null);
            if (restaurants == null || restaurants.isEmpty()) {
                return "Désolé, je n'ai pas trouvé de bonne proposition répondant à tes critères :cold_sweat:";
            }
            int randomRestaurant = ThreadLocalRandom.current().nextInt(0, restaurants.size() + 1);
            final GooglePlaces.Restaurant choice = restaurants.get(randomRestaurant);
            String repsonse = "*" + choice.getName() + "*\n"
                    + "_" + choice.getAddress() + "_\n"
                    + "Rating: " + choice.getRating();
            if (choice.getLat() != null && choice.getLng() != null) {
                repsonse += "\nhttps://www.google.fr/maps/@" + choice.getLat() + "," + choice.getLng() + ",20z";
            }

            return repsonse;
        case "notSupportedYet":
        default:
            return "Je ne comprends pas ! :sleepy: \nSi tu veux m'améliorer, fait une PR sur https://github.com/rmannibucau/slack-bot";
        }

    }

    private String getCommand(final Message message) {
        //todo improve command recognition
        if (message.getText().isEmpty()) {
            return "empty";
        } else if (message.getText().toLowerCase(FRANCE).contains("salut")
                || message.getText().toLowerCase(FRANCE).contains("bonjour")
                || (message.getText().toLowerCase(FRANCE).contains("ça va")
                || message.getText().toLowerCase(ENGLISH).contains("hello"))) {
            return "joke";
        } else if (message.getText().toLowerCase(FRANCE).contains("on mange ou")
                || message.getText().toLowerCase(FRANCE).contains("on mange où")) {
            return "findRestaurant";
        }

        return "notSupportedYet";
    }
}
