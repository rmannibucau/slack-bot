package com.github.rmannibucau.slack.websocket;

import static java.util.Locale.ROOT;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.enterprise.context.Dependent;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.github.rmannibucau.slack.service.MessageHandler;

import org.apache.johnzon.websocket.mapper.JohnzonTextDecoder;
import org.apache.johnzon.websocket.mapper.JohnzonTextEncoder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ClientEndpoint(decoders = SlackClient.MessageDecoder.class, encoders = JohnzonTextEncoder.class)
public class SlackClient {

    private final String botId;

    private final String postMessageEndpoint;

    private final String token;

    private final MessageHandler handler;

    private final AtomicLong messageId = new AtomicLong();

    private Client client;

    private WebTarget target;

    private List<String> defaultChannels;

    public SlackClient(final MessageHandler handler, final String botId, final String postMessageEndpoint,
            final String token, final List<String> defaultChannels) {
        this.botId = botId;
        this.postMessageEndpoint = postMessageEndpoint;
        this.token = token;
        this.handler = handler;
        this.defaultChannels = defaultChannels;
    }

    @OnOpen
    public void onOpen(final Session session) {
        client = ClientBuilder.newClient();
        target = client.target(postMessageEndpoint);
        defaultChannels.forEach(c -> send("Hello guys", null, c));

    }

    @OnMessage
    public synchronized void onMessage(final Session session, final Message message) {
        switch (ofNullable(message.getType()).orElse("reply")) {
        case "error":
            log.error("Error {}: {}", message.getError().getCode(), message.getError().getMsg());
            break;
        case "message":
            if (message.getUser() != null && !message.getUser().toLowerCase(ROOT).contains("slackbot")
                    && message.getText() != null && !botId.equals(message.getUser()) && message.getText()
                    .contains(botId)) {
                onUserMessage(message);
            }
            break;
        default:
            log.debug("{}", message);
        }
    }

    @OnError
    public void onError(final Throwable error) {
        log.error(error.getMessage(), error);
    }

    @OnClose
    public void onClose(final Session session, final CloseReason reason) {
        defaultChannels.forEach(c -> send("Bye bye", null, c));
        log.debug("Closing session {} cause {}", session.getId(), reason.getCloseCode());
        if (client != null) {
            client.close();
        }
    }

    private void onUserMessage(final Message message) {
        // sanitize
        message.setText(message.getText().replace("<@" + botId + ">", "").trim());
        send(handler.createResponse(message)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                /*+ " // cc <@" + message.getUser() + ">" */, message.getId(), message.getChannel());
    }

    private void send(final String message, final Long id, final String chan) {
        final Message response = new Message();
        response.setId(ofNullable(id).orElseGet(messageId::incrementAndGet));
        response.setUser(botId);
        response.setAs_user(true);
        response.setType("message");
        response.setChannel(chan);
        response.setText(message);
        log.debug("Sending {}", response);
        final PostMessageResponse postMessageResponse = target.request(APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token)
                .post(entity(response, APPLICATION_JSON_TYPE), PostMessageResponse.class);
        if (!postMessageResponse.isOk()) {
            log.error("Can't post message {}: {}", response, postMessageResponse.getError());
        }
    }

    @Dependent
    public static class MessageDecoder extends JohnzonTextDecoder {

        public MessageDecoder() {
            super(Message.class);
        }

        /*
         * to debug the available messages
         *
         * @Override
         * public Object decode(Reader stream) throws DecodeException {
         * try (final StringWriter w = new StringWriter()) {
         * IOUtils.copy(stream, w, 8192);
         * w.flush();
         * String value = w.toString();
         * log.info(value);
         * return super.decode(new StringReader(value));
         * } catch (final IOException e) {
         * throw new IllegalStateException(e);
         * }
         * }
         */
    }
}
