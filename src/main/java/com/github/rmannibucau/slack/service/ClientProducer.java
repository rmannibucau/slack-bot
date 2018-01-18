package com.github.rmannibucau.slack.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@ApplicationScoped
public class ClientProducer {
    @Produces
    public Client client() {
        return ClientBuilder.newClient();
    }

    public void release(@Disposes final Client client) {
        client.close();
    }
}
