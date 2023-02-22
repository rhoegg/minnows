package com.ryanhoegg.minnowserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class Game {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String id;
    private final int boardSize;
    private final Point shark;
    private final Point minnow;

    private int sharkSpeed = 1; // steps per second
    private Date gameBegan;
    private Date sharkLastMoved;
    private boolean started = false;

    private final List<Observer> observers = new ArrayList<>();

    public Game(String id, int boardSize) {
        this.id = id;
        this.boardSize = boardSize;
        shark = new Point(ThreadLocalRandom.current().nextInt(boardSize), ThreadLocalRandom.current().nextInt(boardSize));
        minnow = new Point(ThreadLocalRandom.current().nextInt(boardSize), ThreadLocalRandom.current().nextInt(boardSize));
    }

    public String getId() {
        return this.id;
    }

    public void tick() {
        if (started) {
            moveShark();
            fixOverflow(shark);
            fixOverflow(minnow);
            checkGameOver();
        }
    }

    public void setSharkSpeed(int speed) {
        this.sharkSpeed = speed;
    }

    public void start() {
        if (this.started) {
            throw new RuntimeException("Can not start a game while it's running");
        }
        gameBegan = new Date();
        this.started = true;
        notify("START");
        notifyPositions();
    }

    public void stop() {
        this.started = false;
        gameOver();
    }

    private void notifyPositions() {
        String text = "SHARK " + shark.x + "," + shark.y + "\n"
                + "MINNOW " + minnow.x + "," + minnow.y;
        notify(text);
    }

    public void minnowRight() {
        if (started) {
            minnow.x++;
            notifyPositions();
        }
    }

    public void minnowLeft() {
        if (started) {
            minnow.x--;
            notifyPositions();
        }
    }

    public void minnowUp() {
        if (started) {
            minnow.y--;
            notifyPositions();
        }
    }

    public void minnowDown() {
        if (started) {
            minnow.y++;
            notifyPositions();
        }
    }

    private void moveShark() {
        if ( null == sharkLastMoved ||
                (1000 / sharkSpeed) <= (new Date().getTime() - sharkLastMoved.getTime())) { // milliseconds per shark step > elapsed

            int xDistance = minnow.x - shark.x;
            int yDistance = minnow.y - shark.y;
            if (Math.abs(xDistance) > Math.abs(yDistance)) {
                // horizontal move
                shark.x += Math.signum(xDistance);
            } else {
                // vertical move
                shark.y += Math.signum(yDistance);
            }
            sharkLastMoved = new Date();
            notifyPositions();
        }
    }

    private void fixOverflow(Point p) {
        // walls
        if (p.x < 0) {
            p.x = 0;
        }
        if (p.x >= boardSize) {
            p.x = boardSize - 1;
        }
        if (p.y < 0) {
            p.y = 0;
        }
        if (p.y >= boardSize) {
            p.y = boardSize - 1;
        }
    }

    private void checkGameOver() {
        if (shark.x == minnow.x && shark.y == minnow.y) {
            stop();
        }
    }

    private void gameOver() {
        long gameElapsed = new Date().getTime() - gameBegan.getTime();
        notify("GAME OVER " + gameElapsed);
    }

    private void notify(String text) {
        GameMessage message = new GameMessage(id, text);
        for (Observer o : observers) {
            o.notify(message);
        }
    }

    public void subscribe(Observer observer) {
        this.observers.add(observer);
    }
    public interface Observer {
        void notify(GameMessage message);
    }
}
