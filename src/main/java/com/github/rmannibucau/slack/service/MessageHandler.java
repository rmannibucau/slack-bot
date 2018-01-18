package com.github.rmannibucau.slack.service;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRANCE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.rmannibucau.slack.service.command.internal.CommandExtension;
import com.github.rmannibucau.slack.websocket.Message;

@ApplicationScoped
public class MessageHandler {

    @Inject
    private GooglePlaces googlePlaces;

    @Inject
    private CommandExtension extension;

    public String createResponse(final Message message) {
        return extension.findCommand(alias(message)).apply(message);
    }

    private String alias(final Message message) {
        // todo improve command recognition
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
        return message.getText().trim();
    }
}
