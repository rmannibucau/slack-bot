package com.github.rmannibucau.slack.service;

import javax.enterprise.context.ApplicationScoped;

import com.github.rmannibucau.slack.websocket.Message;

@ApplicationScoped
public class MessageHandler {
    public String createResponse(final Message message) {
        return "Si tu veux une implémentation correcte fait une PR sur https://github.com/rmannibucau/slack-bot";
    }
}
