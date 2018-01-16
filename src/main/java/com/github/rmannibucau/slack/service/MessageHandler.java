package com.github.rmannibucau.slack.service;

import javax.enterprise.context.ApplicationScoped;

import com.github.rmannibucau.slack.websocket.Message;

@ApplicationScoped
public class MessageHandler {
    public String createResponse(final Message message) {
        return "Got it, come back later, for now i'm bored :smile:, " +
                "for memories you said '" + message.getText() + "' ";
    }
}
