package com.github.rmannibucau.slack.startup;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Form;

import com.github.rmannibucau.slack.configuration.SlackConfiguration;
import com.github.rmannibucau.slack.service.MessageHandler;
import com.github.rmannibucau.slack.websocket.SlackClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Dependent
@WebListener
public class SlackConnector implements ServletContextListener {

    private final AtomicReference<Session> session = new AtomicReference<>();

    @Inject
    private SlackConfiguration configuration;

    @Inject
    private MessageHandler handler;

    private Runnable onClose;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(group, r, SlackConnector.class.getName() + "-healthcheck");
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setDaemon(false);
            return thread;
        });
        final ScheduledFuture<?> scheduled = ses.scheduleAtFixedRate(() -> {
            final Session currentSession = session.get();
            if (currentSession == null || !isConnected(currentSession)) {
                synchronized (SlackConnector.this) {
                    final Session session = this.session.get();
                    if (session != null && session.isOpen()) {
                        return;
                    }
                    log.info("Trying to connect to slack");
                    doConnect(container);
                }
            }
        }, 0, configuration.healthcheckTimeout(), MILLISECONDS);

        // heroku keep up free dyno loop
        final String pingEndpoint = configuration.pingEndpoint();
        final ScheduledFuture<?> healthCheck = pingEndpoint != null ? ses.scheduleAtFixedRate(() -> {
            final Client client = ClientBuilder.newClient();
            try {
                client.target(pingEndpoint).request(APPLICATION_JSON_TYPE).get(String.class);
            } finally {
                client.close();
            }
        }, 0, 25, MINUTES) : null;

        onClose = () -> {
            Stream.of(scheduled, healthCheck).filter(Objects::nonNull).forEach(f -> f.cancel(true));
            ses.shutdownNow();
            ofNullable(session.getAndSet(null)).ifPresent(s -> {
                try {
                    s.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "bye"));
                } catch (final IOException e) {
                    log.error(e.getMessage(), e);
                }
            });
        };
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        onClose.run();
    }

    private void doConnect(final WebSocketContainer container) {
        final ConnectResponse response = getWsUrl();
        try {
            session.set(container.connectToServer(new SlackClient(handler, response.getSelf().getId(),
                            configuration.postMessageEndpoint(),
                            configuration.token(),
                            Optional.ofNullable(configuration.defaultChannels()).orElseGet(Collections::emptyList)),
                    URI.create(response.getUrl())));
        } catch (final DeploymentException | IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    private ConnectResponse getWsUrl() {
        final Client client = ClientBuilder.newClient();
        final ConnectResponse response;
        try {
            response = client.target(configuration.rtmConnectEndpoint()).request(APPLICATION_JSON_TYPE)
                    .post(entity(
                            new Form().param("token", configuration.token()).param("batch_presence_aware", "false"),
                            APPLICATION_FORM_URLENCODED_TYPE), ConnectResponse.class);
            if (!response.isOk()) {
                throw new IllegalStateException("Can't connect: " + response);
            }
        } finally {
            client.close();
        }
        return response;
    }

    private synchronized boolean isConnected(final Session currentSession) {
        return currentSession.isOpen();
    }
}
