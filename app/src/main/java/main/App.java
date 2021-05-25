/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package main;

import engine.*;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;

import java.util.Comparator;

/**
 * Main Class.
 * <p>
 * Handles drawing of and interaction with the game. Initialises and maintains the game engine.
 */
public class App extends PApplet {
    ///**
    // * The size of tiles, both in width and height.
    // */
    //tileSize = 32,
    /**
     * The space within the tile board to its edges, where no tiles are drawn yet.
     */
    private static final int tilePadding = 48,
    /**
     * The space between the tile board and the lower edge of the screen.
     */
    tileBottomOffset = 200;
    ///**
    // * The maximum tile index in each direction (meaning that the field ist 17x17).
    //*/
    //maxTiles = 16,
    ///**
    // * The visual border surrounding all tiles.
    // */
    //fieldBorder = 10;
    /**
     * The Engine relating to the current level and play-through.
     */
    private Engine engine;

    private PFont font;

    /**
     * Initialises Processing functionality.
     *
     * @param args Launch arguments.
     */
    public static void main(String[] args) {
        String[] appArgs = {"Laser Game App"};
        App mySketch = new App();
        PApplet.runSketch(appArgs, mySketch);
    }

    /**
     * Sets canvas size.
     */
    public void settings() {
        size(600, 800);
        //size(tileSize * maxTiles + tilePadding * 2, tileSize * maxTiles + tileBottomOffset + tilePadding * 2);
    }

    /**
     * Sets up engine and images. Configures drawing settings.
     */
    public void setup() {
        engine = new LaserEngine(loadJSONArray("src/levels.json"));
        font = createFont("img/EdgeOfTheGalaxy.otf", 40);

        Image.initialise(this);

        imageMode(CENTER);
        textAlign(CENTER);
        strokeWeight(3);
        surface.setResizable(true);

        /*
            license: Public Domain
            link: https://www.fontspace.com/edge-of-the-galaxy-font-f45748
         */
    }

    /**
     * Main draw loop. Updates the engine as well as the frontend.
     */
    public void draw() {
        engine.update();
        setMousePointer();

        translate(width / 2f, (height / 2f) - tileBottomOffset);
        background(18);

        drawBoard();

        drawMenuBox();

        surface.setTitle(" Laser Game - Level " + nf(engine.getLevelID() + 1, 2) + ": " + engine.getLevelDescription());
    }

    /**
     * Draws the info box containing medal, moves, level description and level id.
     */
    private void drawMenuBox() {
        stroke(255);
        fill(33);

        // Background box
        rect(-width, (height / 2f), width * 2, height);

        rect(-10, height - (tileBottomOffset - 50), width + 20, height);
        rect(width - 160, height - (tileBottomOffset + 50), width, height);

        // Medal
        image(Image.MEDAL.getImages().get(engine.getMedalID()), width - 80, height - (tileBottomOffset - 30), 140, 140);

        fill(255);

        // Move counter
        textFont(font, 60);
        text(nf(engine.getMoves(), 2) + "/" + nf(engine.getOptimalMoves(), 2), width - 80, height - 25);

        // Level name
        textFont(font, 35);
        text(engine.getLevelDescription(), (width / 2f) - 80, height - 30);

        // Level ID or Level Complete text
        textFont(font, 30);
        if (engine.isCompleted())
            text("Level Cleared!\nUse arrow keys to switch levels", (width / 2f) - 80, height - 115);
        else
            text(nf(engine.getLevelID() + 1, 2), (width / 2f) - 80, height - 80);

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
            square(pos.x - getTileSize(), pos.y - getTileSize(), getTileSize() * 2);
        });

        // Draws a floor image for every tile present
        engine.getCopyOfTiles().keySet()
                .forEach(key -> Image.FLOOR.draw(vectorOfTile(key.x(), key.y()), (key.x() + key.y()) % 4));

        // Draws all tiles once
        engine.getCopyOfTiles()
                .forEach((key, value) -> Image.valueOf(value.getType().toString()).draw(vectorOfTile(key.x(), key.y()), value.getState()));

        //Sorts lasers by color to avoid flickering when lasers go across each other
        engine.getLasers().stream().sorted(Comparator.comparing(Laser::color)).forEach(this::drawLaser);

        // Draws all tiles which have collision to cover the laser
        engine.getCopyOfTiles().entrySet().stream()
                .filter(t -> t.getValue().getCollision())
                .filter(t -> t.getValue().getType() != Tile.Type.STONE_TARGET && !t.getValue().getType().isLaserSource())
                .forEach(t -> Image.valueOf(t.getValue().getType().toString()).draw(vectorOfTile(t.getKey().x(), t.getKey().y()), t.getValue().getState()));
    }

    /**
     * Draws a specific laser.
     *
     * @param l the Laser.
     */
    private void drawLaser(Laser l) {
        switch (l.color()) {
            case RED -> stroke(255, 0, 0, 150 + random(50));
            case BLUE -> stroke(0, 0, 255, 150 + random(50));
            case GREEN -> stroke(0, 255, 0, 150 + random(50));
        }

        /*
        https://stackoverflow.com/questions/67643914/how-can-i-iterate-over-two-items-of-a-stream-at-once?noredirect=1#comment119564123_67643914
         */
        PVector[] a = l.points().stream().map(v -> vectorOfTile((int) v.x, (int) v.y)).toArray(PVector[]::new);
        for (int i = 0; i < a.length - 1; i++) {
            line(a[i].x, a[i].y, a[i + 1].x, a[i + 1].y);
        }
    }

    /**
     * Updates mouse pointer.
     * <p>
     * If the mouse is above an intractable tile, the cursor changes to HAND.
     */
    private void setMousePointer() {
        Pair<Integer, Integer> mousePos = tileOfVector(new PVector(mouseX, mouseY));

        if (engine.getCopyOfTiles().get(mousePos) != null
                && engine.getCopyOfTiles().get(mousePos).getType().canInteract()) cursor(HAND);
        else cursor(ARROW);

    }

    /**
     * Upon mouse release, there is an attempt at interacting with the tile board.
     */
    public void mouseReleased() {
        if (engine.isCompleted()) {
            engine.requestLevel(0);
            return;
        }
        try {
            engine.registerInteraction(tileOfVector(new PVector(mouseX, mouseY)), mouseButton);
        } catch (IllegalStateException | IllegalArgumentException ignored) {
        }
    }

    /**
     * Upon key release, if the key was an arrow key, the level is reset or changed.
     */
    public void keyReleased() {
        if (key != CODED)
            return;

        switch (keyCode) {
            case LEFT -> engine.requestLevel(-1);
            case DOWN, UP -> engine.requestLevel(0);
            case RIGHT -> engine.requestLevel(1);
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
        //float maxTiles = max(engine.getMaxTiles().x(), engine.getMaxTiles().y());
        float m = getTileSize();
        //return new PVector((((x - (engine.getMaxTiles().x() * 0.5f)) * m) + width - m / 2f),
                //(((y - (engine.getMaxTiles().y() * 0.5f)) * m) + height - m / 2f));

        return new PVector(((x - engine.getMaxTiles().y() * 0.5f) * m), // - (width - m) / 4f),
                ((y - engine.getMaxTiles().y() * 0.5f) * m) + tileBottomOffset / 2f);
    }

    //private PVector vectorOfTile(int x, int y) {
    //    return new PVector(x * tileSize + tilePadding, y * tileSize + tilePadding);
    //}

    /**
     * Converts a position on the canvas into the Tile position (index) which it is pointing to.
     *
     * @param pos Position from which to get the Tile index.
     * @return The Pair representing the position which the PVector points to. Null if the given Vector points outside of the Tile field.
     */
    private Pair<Integer, Integer> tileOfVector(PVector pos) {
        //float maxTiles = max(engine.getMaxTiles().x(), engine.getMaxTiles().y());
        float m = getTileSize();

        return Pair.of(floor(
                ((pos.x - (width - m) / 2f) / m) + (engine.getMaxTiles().x() * 0.5f)),
                floor(((pos.y + tileBottomOffset / 2f - (height - m) / 2f) / m) + (engine.getMaxTiles().y() * 0.5f)));

        //return Pair.of(floor(((pos.x + width / 2f) - tilePadding * 0.5f) / m), floor(((pos.y + height / 2f) - tilePadding * 0.5f) / m));
    }

    protected float getTileSize() {
        //float maxTiles = max(engine.getMaxTiles().x(), engine.getMaxTiles().y());
        return width / engine.getMaxTiles().x() <= (height - tileBottomOffset) / engine.getMaxTiles().y() ?
                (width - tilePadding * 2f) / engine.getMaxTiles().x() :
                (height - (tilePadding * 2f) - tileBottomOffset) / engine.getMaxTiles().y();
    }

    //protected float getTileSize() {
    //    float maxTiles = max(engine.getMaxTiles().x(), engine.getMaxTiles().y());
    //    return width <= (height - tileBottomOffset) ? (width - tilePadding * 2f) / maxTiles : (height - (tilePadding * 2f) - tileBottomOffset) / maxTiles;
    //}

    //private Pair<Integer, Integer> canvasToTile(PVector pos) {
    //    int x = floor((pos.x - tilePadding + (tileSize / 2f)) / tileSize);
    //    int y = floor((pos.y - tilePadding + (tileSize / 2f)) / tileSize);
//
    //    return x >= 0 && y >= 0 && x <= maxTiles && y <= maxTiles ?
    //            Pair.of(x, y) : null;
    //}

}