/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package main;

import engine.*;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.Comparator;

/**
 * Main Class.
 * <p>
 * Handles drawing of and interaction with the game. Initialises and maintains the game engine.
 */
public class App extends PApplet {
    /**
     * The size of tiles, both in width and height.
     */
    private static final int tileSize = 32,
    /**
     * The space within the tile board to its edges, where no tiles are drawn yet.
     */
    tilePadding = 48,
    /**
     * The space between the upper edge of the screen and the tile board.
     */
    tileTopOffset = 60,
    /**
     * The space between the tile board and the lower edge of the screen.
     */
    tileBottomOffset = 100,
    /**
     * The maximum tiles in each direction.
     */
    maxTiles = 16,

    /**
     * The visual border surrounding all tiles.
     */
    fieldBorder = 10;
    /**
     * The current level index.
     */
    private int currentLevel;
    /**
     * The Engine relating to the current level and playthrough.
     */
    private Engine engine;

    /**
     * Initialises Processing functionality.
     *
     * @param args Launch arguments.
     */
    public static void main(String[] args) {
        String[] appArgs = {"lasergame"};
        App mySketch = new App();
        PApplet.runSketch(appArgs, mySketch);
    }

    /**
     * Sets canvas size.
     */
    public void settings() {
        size(tileSize * maxTiles + tilePadding * 2, tileSize * maxTiles + tileTopOffset + tileBottomOffset + tilePadding * 2);
        System.out.println("Initializing game with w:" + width + ", h:" + height);
    }

    /**
     * Sets up engine and images. Configures drawing settings.
     */
    public void setup() {
        currentLevel = 0;
        engine = new LaserEngine(currentLevel, loadJSONArray("src/levels.json"));
        Image.initialise(this, tileSize);

        imageMode(CENTER);
        textAlign(CENTER);

        /*
            license: Public Domain
            link: https://www.fontspace.com/edge-of-the-galaxy-font-f45748
         */
        textFont(createFont("img/EdgeOfTheGalaxy.otf", 40));
    }

    /**
     * Main draw loop. Updates the engine as well as the frontend.
     */
    public void draw() {
        engine.updateLasers();
        setMousePointer();

        background(18);

        drawBoard();

        drawUpperBox();
        drawLowerBox();
    }

    /**
     * Draws upper box.
     */
    private void drawUpperBox() {
        stroke(255);
        fill(33);
        rect(-10, -10, width + 20, tileTopOffset + 10);

        fill(255);
        text(engine.getLevelDescription(), width / 2f, tileTopOffset - 10);

    }

    /**
     * Draws lower box.
     */
    private void drawLowerBox() {
        stroke(255);
        fill(33);
        rect(-10, height + 10, width + 20, -tileBottomOffset - 10);
        fill(255);
        text(nf(engine.getMoves(), 2) + "/" + nf(engine.getOptimalMoves(), 2), width / 4f, height - tileBottomOffset * 0.3f);
        strokeWeight(2);
    }

    /**
     * Draws board of tiles.
     * <p>
     * First draws the floor using a pattern, then all tiles at once over it.
     * <p>
     * After drawing all lasers, it now draws all tiles which have collision except for the target block, to cover
     * a potential laser hitting a wall.
     */
    private void drawBoard() {
        fill(66);
        noStroke();
        // Draws outline around all tiles
        engine.getCopyOfTiles().keySet().forEach(key -> {
            PVector pos = vectorOfTile(key.x(), key.y());
            square(pos.x - ((tileSize / 2f) + fieldBorder), pos.y - ((tileSize / 2f) + fieldBorder), tileSize + fieldBorder * 2);
        });

        // Draws a floor image for every tile present
        engine.getCopyOfTiles().keySet()
                .forEach(key -> {

                    Image.FLOOR.draw(vectorOfTile(key.x(), key.y()), (key.x() + key.y()) % 4);
                });

        // Draws all tiles once
        engine.getCopyOfTiles()
                .forEach((key, value) ->
                        Image.valueOf(value.getType().toString()).draw(vectorOfTile(key.x(), key.y()), value.getState()));

        //Sorts lasers by color to avoid flickering when lasers go across each other
        engine.getLasers().stream().sorted(Comparator.comparing(Laser::color)).forEach(this::drawLaser);

        // Draws all tiles which have collision to cover the laser
        engine.getCopyOfTiles().entrySet().stream()
                .filter(t -> t.getValue().getCollision())
                .filter(t -> t.getValue().getType() != Tile.Type.STONE_TARGET)
                .forEach(t ->
                        Image.valueOf(t.getValue().getType().toString()).draw(vectorOfTile(t.getKey().x(), t.getKey().y()), t.getValue().getState()));
    }

    /**
     * Draws a specific laser.
     *
     * @param l the Laser.
     */
    public void drawLaser(Laser l) {
        switch (l.color()) {
            case RED -> stroke(255, 0, 0, 150 + random(50));
            case BLUE -> stroke(0, 0, 255, 150 + random(50));
            case GREEN -> stroke(0, 255, 0, 150 + random(50));
        }

        final PVector[] points = l.points().toArray(new PVector[0]);
        for (int i = 0; i < points.length - 1; i++) {
            PVector start = vectorOfTile((int) points[i].x, (int) points[i].y);
            PVector stop = vectorOfTile((int) points[i + 1].x, (int) points[i + 1].y);
            line(start.x, start.y, stop.x, stop.y);
        }
    }

    /**
     * Updates mouse pointer.
     * <p>
     * If the mouse is above an intractable tile, the cursor changes to HAND.
     */
    private void setMousePointer() {
        Pair<Integer, Integer> mousePos = canvasToTile(new PVector(mouseX, mouseY));

        if (mousePos != null && engine.getCopyOfTiles().get(mousePos) != null
                && engine.getCopyOfTiles().get(mousePos).getType().canInteract())
            cursor(HAND);
        else
            cursor(ARROW);

    }

    /**
     * Upon mouse release, there is an attempt at interacting with the tile board.
     */
    public void mouseReleased() {
        try {
            engine.registerInteraction(canvasToTile(new PVector(mouseX, mouseY)), mouseButton);
        } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {
        }
    }

    /**
     * Upon key release, if the key was an arrow key, the level is reset or changed.
     */
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
        return new PVector(x * tileSize + tilePadding, y * tileSize + tileTopOffset + tilePadding);
    }

    /**
     * Converts a position on the canvas into the Tile position (index) which it is pointing to.
     *
     * @param pos Position from which to get the Tile index.
     * @return The Pair representing the position which the PVector points to. Null if the given Vector points outside of the Tile field.
     */
    private Pair<Integer, Integer> canvasToTile(PVector pos) {
        int x = floor((pos.x - tilePadding + (tileSize / 2f)) / tileSize);
        int y = floor((pos.y - (tileTopOffset + tilePadding) + (tileSize / 2f)) / tileSize);

        return x >= 0 && y >= 0 && x <= maxTiles && y <= maxTiles ?
                Pair.of(x, y) : null;
    }

    /**
     * Loads next level, provided there is one. Otherwise restarts current level.
     */
    public void nextLevel() {
        if (loadJSONArray("src/levels.json").size() > currentLevel + 1)
            engine = new LaserEngine(++currentLevel, loadJSONArray("src/levels.json"));
        else
            restartLevel();
    }

    /**
     * Loads previous level, provided there is one. Otherwise restarts current level.
     */
    public void previousLevel() {
        if (currentLevel - 1 >= 0)
            engine = new LaserEngine(--currentLevel, loadJSONArray("src/levels.json"));
        else
            restartLevel();
    }

    /**
     * Restarts level.
     */
    public void restartLevel() {
        engine = new LaserEngine(currentLevel, loadJSONArray("src/levels.json"));
    }
}