package com.github.rmannibucau.slack.service.command.impl;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import com.github.rmannibucau.slack.service.command.api.Command;
import com.github.rmannibucau.slack.websocket.Message;

@Command(value = "greeting", alias = { "salut", "bonjour", "ça va", "hello" })
@ApplicationScoped
public class JokeCommand implements Function<Message, String> {

    @Override
    public String apply(final Message message) {
        return "Salutation belle companie!";
    }
}
