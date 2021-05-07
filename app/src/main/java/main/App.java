/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package main;

import engine.*;
import processing.core.PApplet;
import processing.core.PVector;

public class App extends PApplet {
    private static final int tileSize = 32, tileVerticalOffset = 48 + 100, tileHorizontalOffset = 48, maxTiles = 16;
    private int currentLevel;
    private Engine engine;

    public static void main(String[] args) {
        String[] appArgs = {"lasergame"};
        App mySketch = new App();
        PApplet.runSketch(appArgs, mySketch);
        System.out.println(new App().getGreeting());
    }

    public void settings() {
        size(tileSize * maxTiles + tileHorizontalOffset * 2, tileSize * maxTiles + tileVerticalOffset * 2);
        System.out.println(width + ", " + height);
    }

    public void setup() {
        currentLevel = 0;
        engine = new LaserEngine(currentLevel, this);
        Image.initialise(this, tileSize);

        imageMode(CENTER);
    }

    public String getGreeting() {
        return "Hello World!";
    }

    public void draw() {
        engine.updateLasers();

        background(15, 22, 41);
        stroke(0);
        strokeWeight(5);
        rect(0, 0, width, 100);
        rect(0, height, width, -100);
        strokeWeight(2);

        engine.getTiles().forEach((key, value) -> {
            Image.FLOOR.draw(tileVector(key.x(), key.y()), (key.x() + key.y()) % 4);
            value.getType().getImage().draw(tileVector(key.x(), key.y()), value.getState());
        });

        engine.getLasers().forEach(this::drawLaser);

        engine.getTiles().forEach((key, value) -> {
            if (value.getCollision() && value.getType() != Tile.Type.STONE_TARGET)
                value.getType().getImage().draw(tileVector(key.x(), key.y()), value.getState());
        });

        setMousePointer();
    }

    public void drawLaser(Laser l) {
        switch (l.getColor()) {
            case RED -> stroke(255, 0, 0, 150);
            case BLUE -> stroke(0, 0, 255, 150);
            case GREEN -> stroke(0, 255, 0, 150);
        }
        PVector[] points = l.getPoints();
        for (int i = 0; i < points.length - 1; i++) {
            PVector start = tileVector((int) points[i].x, (int) points[i].y);
            PVector stop = tileVector((int) points[i + 1].x, (int) points[i + 1].y);
            line(start.x, start.y, stop.x, stop.y);
        }
    }

    private void setMousePointer() {
        Pair<Integer, Integer> mousePos = canvasToTile(new PVector(mouseX, mouseY));

        if (mousePos != null && engine.getTiles().get(mousePos) != null
                && engine.getTiles().get(mousePos).getType().canInteract)
            cursor(HAND);
        else
            cursor(ARROW);

    }

    public void mouseReleased() {
        try {
            engine.registerInteraction(canvasToTile(new PVector(mouseX, mouseY)), mouseButton);
        } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {
        }

    }

    public void keyReleased() {
        if (key == CODED) {
            switch (keyCode) {
                case LEFT -> previousLevel();
                case DOWN -> restartLevel();
                case RIGHT -> nextLevel();
            }
        }
    }

    private PVector tileVector(int x, int y) {
        return new PVector(x * tileSize + tileHorizontalOffset, y * tileSize + tileVerticalOffset);
    }

    private Pair<Integer, Integer> canvasToTile(PVector pos) {
        int x = floor((pos.x - tileHorizontalOffset + (tileSize / 2f)) / tileSize);
        int y = floor((pos.y - tileVerticalOffset + (tileSize / 2f)) / tileSize);

        return x >= 0 && y >= 0 && x <= maxTiles && y <= maxTiles ?
                Pair.of(x, y) : null;
    }

    public void nextLevel() {
        engine = new LaserEngine(++currentLevel, this);
    }

    public void previousLevel() {
        if (currentLevel - 1 >= 0)
            engine = new LaserEngine(--currentLevel, this);
    }

    public void restartLevel() {
        engine = new LaserEngine(currentLevel, this);
    }
}