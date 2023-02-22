package com.ryanhoegg.minnowserver;

public class GameMessage {
    private final String id;
    private final String text;

    public GameMessage(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
