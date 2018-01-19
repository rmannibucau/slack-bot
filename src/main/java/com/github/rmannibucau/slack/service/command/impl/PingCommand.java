package com.github.rmannibucau.slack.service.command.impl;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import com.github.rmannibucau.slack.service.command.api.Command;
import com.github.rmannibucau.slack.websocket.Message;

@Command("ping")
@ApplicationScoped
public class PingCommand implements Function<Message, String> {

    @Override
    public String apply(final Message message) {
        return "pong";
    }
}
