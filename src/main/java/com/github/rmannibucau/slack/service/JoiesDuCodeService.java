package com.github.rmannibucau.slack.service;

import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class JoiesDuCodeService {

    private final Random random = new Random(System.currentTimeMillis());

    private String api = "https://lesjoiesducode.fr/page";

    @Inject
    private Client client;

    public Gif random() {
        try {
            final int pageNumber = random.nextInt(500);
            final String response = client.target(api + "/" + pageNumber)
                    .request(MediaType.TEXT_HTML)
                    .get(String.class);

            final Document doc = Jsoup.parse(response);
            if (doc != null) {
                final Elements posts = doc.select("div.blog-post");
                if (posts != null && posts.size() != 0) {
                    final int pickedPost = random.nextInt(posts.size());
                    final Element choice = posts.get(pickedPost);

                    final String message = choice.select("h1.blog-post-title").select("a").text();
                    final String gif = choice.select("div.blog-post-content").select("img[src$=.gif]").attr("src");

                    return new Gif(message, gif);
                }
            }
        } catch (Throwable e) {
            log.error("unxpected", e); //no-op
        }

        return null;
    }

    @Data
    @AllArgsConstructor
    public static class Gif {

        private String message;

        private String gif;
    }

}
