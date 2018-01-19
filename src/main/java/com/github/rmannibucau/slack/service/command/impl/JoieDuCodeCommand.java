package com.github.rmannibucau.slack.service.command.impl;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.rmannibucau.slack.service.JoiesDuCodeService;
import com.github.rmannibucau.slack.service.command.api.Command;
import com.github.rmannibucau.slack.websocket.Message;

@Command(value = "joie du code", alias = { "fait moi rire", "blague", "joies du code", "joie du code" })
@ApplicationScoped
public class JoieDuCodeCommand implements Function<Message, String> {

    @Inject
    private JoiesDuCodeService joiesDuCodeService;

    @Override
    public String apply(final Message message) {
        final JoiesDuCodeService.Gif gif = joiesDuCodeService.random();
        if (gif == null) {
            return "Je ne suis pas inspiré pour le moment, demande moi plus tard :zzz:";
        }

        return "*" + gif.getMessage() + "*\n" + gif.getGif();
    }
}
