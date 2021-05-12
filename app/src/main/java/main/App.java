/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package main;

import engine.*;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.Comparator;

public class App extends PApplet {
    private static final int tileSize = 32, tileVerticalOffset = 48 + 100, tileHorizontalOffset = 48, maxTiles = 16;
    private int currentLevel;
    private Engine engine;

    public static void main(String[] args) {
        String[] appArgs = {"lasergame"};
        App mySketch = new App();
        PApplet.runSketch(appArgs, mySketch);
    }

    public void settings() {
        size(tileSize * maxTiles + tileHorizontalOffset * 2, tileSize * maxTiles + tileVerticalOffset * 2);
        System.out.println("Initializing game with w:" + width + ", h:" + height);
    }

    public void setup() {
        currentLevel = 0;
        engine = new LaserEngine(currentLevel, this);
        Image.initialise(this, tileSize);

        imageMode(CENTER);
    }

    public void draw() {
        engine.updateLasers();
        setMousePointer();

        background(15, 22, 41);

        drawBoard();

        drawUpperBox();
        drawLowerBox();


    }

    private void drawUpperBox() {
        stroke(0);
        strokeWeight(5);
        rect(0, 0, width, 100);
    }

    private void drawLowerBox() {
        rect(0, height, width, -100);
        strokeWeight(2);
    }

    private void drawBoard() {
        // Draws a floor image for every tile present
        engine.getTiles().keySet()
                .forEach(key -> Image.FLOOR.draw(vectorOfTile(key.x(), key.y()), (key.x() + key.y()) % 4));

        // Draws all tiles once
        engine.getTiles()
                .forEach((key, value) ->
                        Image.valueOf(value.getType().toString()).draw(vectorOfTile(key.x(), key.y()), value.getState()));

        //Sorts lasers by color to avoid flickering when lasers go across each other
        engine.getLasers().stream().sorted(Comparator.comparing(Laser::color)).forEach(this::drawLaser);

        // Draws all tiles which have collision to cover the laser
        engine.getTiles().entrySet().stream()
                .filter(t -> t.getValue().getCollision())
                .filter(t -> t.getValue().getType() != Tile.Type.STONE_TARGET)
                .forEach(t ->
                        Image.valueOf(t.getValue().getType().toString()).draw(vectorOfTile(t.getKey().x(), t.getKey().y()), t.getValue().getState()));
    }

    public void drawLaser(Laser l) {
        switch (l.color()) {
            case RED -> stroke(255, 0, 0, 150);
            case BLUE -> stroke(0, 0, 255, 150);
            case GREEN -> stroke(0, 255, 0, 150);
        }

        PVector[] points = l.points().toArray(new PVector[0]);
        for (int i = 0; i < points.length - 1; i++) {
            PVector start = vectorOfTile((int) points[i].x, (int) points[i].y);
            PVector stop = vectorOfTile((int) points[i + 1].x, (int) points[i + 1].y);
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
        if (key != CODED)
            return;

        switch (keyCode) {
            case LEFT -> previousLevel();
            case DOWN, UP -> restartLevel();
            case RIGHT -> nextLevel();
        }
    }

    /**
     * Converts a tile position on the tile map into its relative position on the canvas.
     * Does not require the position to contain a tile.
     *
     * @param x x-Position of the Tile (index).
     * @param y y-Position of the Tile (index).
     * @return The PVector pointing to the center of the given Tile position.
     */
    private PVector vectorOfTile(int x, int y) {
        return new PVector(x * tileSize + tileHorizontalOffset, y * tileSize + tileVerticalOffset);
    }

    /**
     * Converts a position on the canvas into the Tile position (index) which it is pointing to.
     *
     * @param pos Position from which to get the Tile index.
     * @return The Pair representing the position which the PVector points to.
     * Null if the given Vector points outside of the Tile field.
     */
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