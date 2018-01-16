package com.github.rmannibucau.slack.websocket;

import lombok.Data;

@Data
public class SlackError {
    private int code;
    private String msg;
}
