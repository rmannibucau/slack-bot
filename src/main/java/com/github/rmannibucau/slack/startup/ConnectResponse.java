package com.github.rmannibucau.slack.startup;

import lombok.Data;

@Data
public class ConnectResponse {

    private boolean ok;

    private String url;

    private String error;

    private Self self;

    @Data
    public static class Self {

        private String id;
    }
}
