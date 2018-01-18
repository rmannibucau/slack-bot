package com.github.rmannibucau.slack.service.command.impl;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import com.github.rmannibucau.slack.service.command.api.Command;
import com.github.rmannibucau.slack.websocket.Message;

@Command("joke")
@ApplicationScoped
public class EmptyCommand implements Function<Message, String> {
    @Override
    public String apply(final Message message) {
        return "Oui ! Comment je peux t'aider ?\n"
                + "_Pour le moment je peux te proposer des restaurants_\n"
                + "Tu peux me demander: '_on mange où ?_'";
    }
}
