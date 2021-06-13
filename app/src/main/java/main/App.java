package main;

import engine.*;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;

import java.util.Map;
import java.util.Set;

/**
 * Main Class.
 * <p>
 * Handles drawing of and interaction with the game. Initialises and maintains the game engine.
 */
public class App extends PApplet {

    /**
     * The space within the tile board to its edges, where no tiles are drawn yet.
     */
    private static final int TILE_PADDING = 96,

    /**
     * The space between the tile board and the lower edge of the screen.
     */
    BOTTOM_OFFSET = 150;

    /**
     * The Engine relating to the current level and play-through.
     */
    private Engine engine;

    /**
     * The font used in this game.
     * <p>
     * Font source: https://www.fontspace.com/edge-of-the-galaxy-font-f45748, licensed in Public Domain.
     */
    private PFont font;

    /**
     * The board manager instance. Used to draw and animate the board.
     */
    private BoardManager boardManager;

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
     * Sets up and initialises the {@link LaserEngine} instance, {@link BoardManager} instance, the {@link Image} enum and other visual functionality needed by processing.
     */
    public void setup() {
        engine = new LaserEngine(loadJSONArray("src/levels.json"));
        font = createFont("img/EdgeOfTheGalaxy.otf", 40);
        boardManager = new BoardManager(this);

        Image.initialise(this);
        boardManager.reset();

        imageMode(CENTER);
        textAlign(CENTER);
        frameRate(30);

        surface.setResizable(true);
        surface.setIcon(Image.MIRROR.getImages().get(3));
    }

    /**
     * Main draw loop. Calls {@link BoardManager} instance to draw and visually update the board, and draws the menu box and optionally the game over screen.
     * Also updates the mouse pointer and the window title.
     */
    public void draw() {
        setMousePointer();

        background(18);

        boardManager.execute(TILE_PADDING, TILE_PADDING, width - TILE_PADDING, height - (BOTTOM_OFFSET + TILE_PADDING));

        if (engine.isCompleted() && boardManager.mirrorsFinished())
            drawGameOver();

        drawMenuBox();

        surface.setTitle(" Laser Game - Level " + nf(engine.getLevelID() + 1, 2) + ": " + engine.getLevelDescription());
    }

    /**
     * Draws the info box containing medal, moves, level description and level id.
     */
    private void drawMenuBox() {
        strokeWeight(min(width / 150f, 4));
        stroke(255);
        fill(33);

        // Layout boxes & Lines
        rect(-10, height - BOTTOM_OFFSET, width + 20, height);
        rect(width - (BOTTOM_OFFSET * 1.8f), height - BOTTOM_OFFSET * 0.9f, BOTTOM_OFFSET * 0.8f, BOTTOM_OFFSET * 0.8f);
        line(width - (BOTTOM_OFFSET * 1.8f), height - BOTTOM_OFFSET * 0.5f, width - (BOTTOM_OFFSET), height - BOTTOM_OFFSET * 0.5f);
        rect((BOTTOM_OFFSET * 0.1f), height - BOTTOM_OFFSET * 0.5f, width - BOTTOM_OFFSET * 2f, BOTTOM_OFFSET * 0.4f);

        // Medal
        image(Image.MEDAL.getImages().get(engine.getMedalID()),
                width - BOTTOM_OFFSET / 2f, height - BOTTOM_OFFSET / 2f,
                BOTTOM_OFFSET * 0.8f, BOTTOM_OFFSET * 0.8f);

        // Move counter
        fill(255);
        textFont(font, 50);
        text(engine.getMoves(),
                width - (BOTTOM_OFFSET * 1.4f), height - BOTTOM_OFFSET * 0.55f);
        text(engine.getOptimalMoves(),
                width - (BOTTOM_OFFSET * 1.4f), height - BOTTOM_OFFSET * 0.15f);

        // Level name
        textFont(font, min(max((width - BOTTOM_OFFSET * 2.1f), 1) / 12f, 53));
        text(engine.getLevelDescription(), (width - BOTTOM_OFFSET * 1.8f) / 2f, height - BOTTOM_OFFSET * 0.15f);
        text("Level " + (engine.getLevelID() + 1), (width - BOTTOM_OFFSET * 1.8f) / 2f, height - BOTTOM_OFFSET * 0.55f);
    }

    /**
     * Draws the game over overlay.
     * <p>
     * Uses a rect instead of background() as for some reason transparency does not work as intended when drawing a background.
     */
    private void drawGameOver() {
        pushMatrix();
        translate(width / 2f, (height - BOTTOM_OFFSET) / 2f);

        fill(0, 130);
        rect(-width, -height, width * 2, height * 2);
        fill(255, 200);
        textFont(font, min(width / 18f, 70, height / 18f));
        text("Level Completed!\nTo play again, use any mouse button.\nSwitch levels with the arrow keys!", 0, -(height) / 7f);

        popMatrix();
    }

    /**
     * Updates mouse pointer.
     * <p>
     * If the mouse is above an intractable tile, the cursor changes to HAND.
     */
    private void setMousePointer() {
        Pair<Integer, Integer> mousePos = boardManager.tileOfVector(new PVector(mouseX, mouseY));

        if (engine.getCopyOfTiles().get(mousePos) != null
                && engine.getCopyOfTiles().get(mousePos).getType().canInteract()
                && !engine.isCompleted())
            cursor(HAND);
        else
            cursor(ARROW);
    }

    /**
     * Upon mouse release, there is an attempt at interacting with the tile board.
     */
    public void mouseReleased() {
        if (engine.isCompleted() && boardManager.mirrorsFinished()) {
            requestLevel(0);
            return;
        }
        try {
            engine.registerInteraction(boardManager.tileOfVector(new PVector(mouseX, mouseY)), mouseButton);
        } catch (IllegalStateException | IllegalArgumentException ignored) {
        }
    }

    /**
     * Upon key release, if the key was an arrow key, the level is reset or changed.
     */
    public void keyReleased() {
        if (key != CODED)
            return;

        requestLevel(switch (keyCode) {
            case LEFT -> -1;
            case RIGHT -> 1;
            default -> 0;
        });
    }

    private void requestLevel(int direction) {
        engine.requestLevel(direction);
        boardManager.reset();
    }

    protected Map<Pair<Integer, Integer>, Tile> fetchTiles() {
        return engine.getCopyOfTiles();
    }

    protected Set<Laser> fetchLasers() {
        return engine.getLasers();
    }

    protected float getTileSize() {
        return boardManager.getTileSize();
    }
}