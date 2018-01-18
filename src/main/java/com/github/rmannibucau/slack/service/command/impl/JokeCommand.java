package com.github.rmannibucau.slack.service.command.impl;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import com.github.rmannibucau.slack.service.command.api.Command;
import com.github.rmannibucau.slack.websocket.Message;

@Command("joke")
@ApplicationScoped
public class JokeCommand implements Function<Message, String> {
    @Override
    public String apply(final Message message) {
        return "Trouve-toi des amis pour discuter avec eux ! :trollface:\n"
                + "_Moi, je sais proposer que des restaurants_";
    }
}
