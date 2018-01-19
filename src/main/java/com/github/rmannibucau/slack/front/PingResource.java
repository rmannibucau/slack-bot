package com.github.rmannibucau.slack.front;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("ping")
@ApplicationScoped
@Produces(APPLICATION_JSON)
public class PingResource {

    @GET
    public String get() {
        return "{\"value\":\"pong\"}";
    }

    @HEAD
    public String head() {
        return get();
    }
}
