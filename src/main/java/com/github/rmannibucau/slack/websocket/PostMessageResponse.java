package com.github.rmannibucau.slack.websocket;

import lombok.Data;

@Data
public class PostMessageResponse {

    private boolean ok;

    private String error;
}
