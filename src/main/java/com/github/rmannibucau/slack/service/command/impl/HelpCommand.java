package com.github.rmannibucau.slack.service.command.impl;

import static java.util.stream.Collectors.joining;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.rmannibucau.slack.service.command.api.Command;
import com.github.rmannibucau.slack.service.command.internal.CommandExtension;
import com.github.rmannibucau.slack.websocket.Message;

@Command("help")
@ApplicationScoped
public class HelpCommand implements Function<Message, String> {

    @Inject
    private CommandExtension extension;

    @Override
    public String apply(final Message message) {
        return "Commandes: " + extension.commandNames().collect(joining(", "));
    }
}
