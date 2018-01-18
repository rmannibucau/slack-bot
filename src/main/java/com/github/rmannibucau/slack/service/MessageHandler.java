package com.github.rmannibucau.slack.service;

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
        return extension.findCommand(message.getText().trim()).apply(message);
    }
}
