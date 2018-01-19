package com.github.rmannibucau.slack.service.command.impl;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.rmannibucau.slack.service.WeatherService;
import com.github.rmannibucau.slack.service.command.api.Command;
import com.github.rmannibucau.slack.websocket.Message;

@Command(value = "qui est le plus beau", alias = "qui est le plus fort")
@ApplicationScoped
public class WhosTheBestCommand implements Function<Message, String> {
    @Inject
    private WeatherService service;

    @Override
    public String apply(final Message message) {
        return "C'est toi mon maître";
    }
}
