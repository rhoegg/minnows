package com.ryanhoegg.minnowserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WebSocketHandler extends AbstractWebSocketHandler implements Game.Observer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Game> games = new HashMap<>();
    private final Map<String, WebSocketSession> sessions = new HashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        if (games.containsKey(session.getId())) {
            Game game = games.get(session.getId());
            String[] tokens = message.getPayload().split(" ");
            switch (tokens[0].toUpperCase()) {
                case "HELLO", "HELP" -> session.sendMessage(new TextMessage("START STOP LEFT RIGHT UP DOWN"));
                case "START" -> game.start();
                case "LEFT" -> game.minnowLeft();
                case "RIGHT" -> game.minnowRight();
                case "UP" -> game.minnowUp();
                case "DOWN" -> game.minnowDown();
                case "STOP" -> game.stop();
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        Game game = new Game(session.getId(), 400);
        game.setSharkSpeed(4);
        game.subscribe(this);
        games.put(session.getId(), game);
        sessions.put(session.getId(), session);
        session.sendMessage(new TextMessage("HELLO"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        games.get(session.getId()).stop();
        games.remove(session.getId());
        sessions.remove(session.getId());
    }

    @Scheduled(fixedDelay = 100)
    public void gameTick() {
        for (Game g : games.values()) {
            g.tick();
        }
    }

    @Override
    public void notify(GameMessage message) {
        WebSocketSession session = sessions.get(message.getId());
        try {
            session.sendMessage(new TextMessage(message.getText()));
        } catch (IOException e) {
            log.error("Couldn't send message to web socket for session {}", message.getId());
        }
    }
}
