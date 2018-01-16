package com.github.rmannibucau.slack.websocket;

import lombok.Data;

@Data
public class Message {
    private Long id;
    private String type;
    private String user;
    private String text;
    private String ts;
    private String event_ts;
    private String channel;
    private boolean as_user;
    private SlackError error;
}
