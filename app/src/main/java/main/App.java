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

    private PFont font;

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
     * Sets up engine and images. Configures drawing settings.
     */
    public void setup() {
        engine = new LaserEngine(loadJSONArray("src/levels.json"));
        /*
            license: Public Domain
            link: https://www.fontspace.com/edge-of-the-galaxy-font-f45748
         */
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
     * Main draw loop. Calls BoardManager instance to draw the board, as well as the menu box and optionally the game over screen.
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

        // Background box
        rect(-10, height - BOTTOM_OFFSET, width + 20, height);

        // Medal
        image(Image.MEDAL.getImages().get(engine.getMedalID()),
                width - BOTTOM_OFFSET / 2f, height - BOTTOM_OFFSET / 2f,
                BOTTOM_OFFSET - 20, BOTTOM_OFFSET - 20);

        fill(255);

        // Move counter & LevelID
        textFont(font, min(width / 10f, 70));
        text("L" + nf(engine.getLevelID() + 1, 2) + " | " + nf(engine.getMoves(), 2) + "/" + nf(engine.getOptimalMoves(), 2),
                width - (BOTTOM_OFFSET + 50), height - 30);

        // Level name
        textFont(font, min(width / 15f, 50));
        text(engine.getLevelDescription(), -70, (height / 2f) + (BOTTOM_OFFSET * 0.4f));
    }

    void drawGameOver() {
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
                && engine.getCopyOfTiles().get(mousePos).getType().canInteract())
            cursor(HAND);
        else
            cursor(ARROW);
    }

    /**
     * Upon mouse release, there is an attempt at interacting with the tile board.
     */
    public void mouseReleased() {
        if (engine.isCompleted()) {
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