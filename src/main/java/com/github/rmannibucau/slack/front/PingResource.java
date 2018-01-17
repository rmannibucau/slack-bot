package com.github.rmannibucau.slack.front;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("ping")
@ApplicationScoped
public class PingResource {

    @GET
    @Produces(APPLICATION_JSON)
    public String ping() {
        return "{\"value\":\"pong\"}";
    }
}
