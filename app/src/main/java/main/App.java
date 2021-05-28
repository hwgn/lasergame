/*
 * This is the frontend class, extends PApplet and is responsible for storing and initialising the engine.
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
    private static final int tilePadding = 80,
    /**
     * The space between the tile board and the lower edge of the screen.
     */
    BOTTOM_OFFSET = 150;
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
        surface.setResizable(true);
        surface.setIcon(Image.MIRROR.getImages().get(3));

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

        translate(width / 2f, (height / 2f) - BOTTOM_OFFSET);
        background(18);

        drawBoard();

        if(engine.isCompleted()) {
            fill(0, 130);
            rect(-width, -height, width * 2, height * 2);
            fill(255, 200);
            textFont(font, min(width / 18f, 70, height / 18f));
            text("Level Completed!\nTo play again, use any mouse button.\nSwitch levels with the arrow keys!", 0, -width / 8f);
        }

        drawMenuBox();

        surface.setTitle(" Laser Game - Level " + nf(engine.getLevelID() + 1, 2) + ": " + engine.getLevelDescription());
    }

    /**
     * Draws the info box containing medal, moves, level description and level id.
     */
    private void drawMenuBox() {
        strokeWeight(min(width / 200f, 4));
        stroke(255);
        fill(33);

        // Background box
        rect(-width, (height / 2f), width * 2, height);

        //rect(-10, height - (BOTTOM_OFFSET - 50), width + 20, height);
        //rect(width - 160, height - (BOTTOM_OFFSET + 50), width, height);

        // Medal
        image(Image.MEDAL.getImages().get(engine.getMedalID()), width / 2f - 73, (height / 2f) + (BOTTOM_OFFSET / 2f), 140, 140);

        fill(255);

        // Move counter & LevelID
        textFont(font, min(width / 10f, 70));
        text("L" + nf(engine.getLevelID() + 1, 2) + " | " + nf(engine.getMoves(), 2) + "/" + nf(engine.getOptimalMoves(), 2),
                -70, (height / 2f) + (BOTTOM_OFFSET * 0.85f));

        // Level name
        textFont(font, min(width / 15f, 50));
        text(engine.getLevelDescription(), -70, (height / 2f) + (BOTTOM_OFFSET * 0.4f));
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
        engine.getCopyOfTiles().keySet().forEach(key ->
                Image.FLOOR.draw(vectorOfTile(key.x(), key.y()), (key.x() + key.y()) % 4));

        // Draws all tiles once
        engine.getCopyOfTiles().forEach((key, value) ->
                Image.valueOf(value.getType().toString()).draw(vectorOfTile(key.x(), key.y()), value.getState()));

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
        for (int i = 0; i < a.length - 1; i++)
            line(a[i].x, a[i].y, a[i + 1].x, a[i + 1].y);

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
        float m = getTileSize();

        return new PVector(((x - engine.getMaxTiles().x() * 0.5f) * m),
                ((y - engine.getMaxTiles().y() * 0.5f) * m) + BOTTOM_OFFSET / 2f);
    }

    /**
     * Converts a position on the canvas into the Tile position (index) which it is pointing to.
     *
     * @param pos Position from which to get the Tile index.
     * @return The Pair representing the position which the PVector points to. Null if the given Vector points outside of the Tile field.
     */
    private Pair<Integer, Integer> tileOfVector(PVector pos) {
        float m = getTileSize();

        return Pair.of(floor(
                ((pos.x - (width - m) / 2f) / m) + (engine.getMaxTiles().x() * 0.5f)),
                floor(((pos.y + BOTTOM_OFFSET / 2f - (height - m) / 2f) / m) + (engine.getMaxTiles().y() * 0.5f)));

    }

    protected float getTileSize() {
        return (width + tilePadding * 2) / engine.getMaxTiles().x() <= (height - BOTTOM_OFFSET) / engine.getMaxTiles().y() ?
                (width - tilePadding * 2f) / engine.getMaxTiles().x() :
                ((height - tilePadding * 2f) - BOTTOM_OFFSET) / engine.getMaxTiles().y();
    }
}