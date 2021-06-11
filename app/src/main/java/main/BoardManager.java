package main;

import engine.Laser;
import engine.Pair;
import engine.Tile;
import processing.core.PVector;

import java.util.*;

import static processing.core.PApplet.*;

/**
 * The BoardManager class takes care of rendering the actual playing field.
 * <p>
 * The most important method is the execute() method, which takes the coordinates of where the map should be drawn and draws it.
 */
class BoardManager {

    /**
     * The App instance. Used to be an instance of PGraphics but due to compatibility issues and the need of fetching the tile map this has been changed.
     */
    private final App g;

    /**
     * The stored mirror rotations. This is purely visual and allows mirrors to have animations as they turn.
     *
     * @see #updateMirrors()
     */
    private HashMap<Pair<Integer, Integer>, Float> mirrorRotations;

    /**
     * The stored laser paths. This is purely visual and allows lasers to appear as if they fade in and out when their path has changed.
     * <p>
     * This being a queue, it stores the four most recent paths all lasers have taken.
     * <p>
     * Initialisation occurs in {@link #resetDynamicGraphics()} / {@link #reset()}.
     *
     * @see #storeLasers(Set)
     */
    private Queue<Set<Laser>> laserStorage;

    /**
     * The locally stored version of the tileMap, used to prevent repeated calls to other instances within a single draw cycle.
     */
    private Map<Pair<Integer, Integer>, Tile> tileMap;

    /**
     * The locally stored pair of the maximum tile count (width) in both the x and y direction.
     * <p>
     * Is updated whenever the board is reset (after level change, etc).
     *
     * @see #getMaxTiles()
     */
    private Pair<Integer, Integer> maxTiles;

    /**
     * The current edges of the game board.
     * <p>
     * Updated in every execute (draw) cycle.
     *
     * @see #execute(int, int, int, int)
     */
    private int x1, y1, x2, y2;

    /**
     * The constructor. Calls the reset method to ensure some variables are properly initialised.
     *
     * @param g the instance of App which extends PApplet. Used for drawing and minimal data retrieval.
     */
    protected BoardManager(App g) {
        this.g = g;
        reset();
    }

    /**
     * The key method of this class. Using the given coordinates, draws the board and lasers.
     * <p>
     * Additionally, it updates the laser and mirror animations.
     *
     * @param x1 the left border of the board
     * @param y1 the top border of the board
     * @param x2 the right border of the board
     * @param y2 the bottom border of the board
     */
    protected void execute(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;

        storeLasers(g.fetchLasers());
        tileMap = g.fetchTiles();
        updateMirrors();

        drawBoard();
    }

    /**
     * Draws the board of tiles as well as the lasers. View the individual steps to learn more about how the textures are layered.
     */
    private void drawBoard() {
        g.pushMatrix();
        g.translate(x1 + ((x2 - x1) / 2f), y1 + ((y2 - y1) / 2f));
        // 0, 0 is now the center of the board drawing area

        g.fill(66);
        g.noStroke();

        // Draws grey ish outline around all tiles
        tileMap.keySet().forEach(key -> {
            PVector pos = vectorOfTile(key.x(), key.y());
            g.square(pos.x - getTileSize(), pos.y - getTileSize(), getTileSize() * 2);
        });

        // Draws a floor image for all transparent tiles
        tileMap.entrySet().stream().filter(e -> Image.valueOf(e.getValue().getType().toString()).isTransparent())
                .map(Map.Entry::getKey)
                .forEach(key ->
                        Image.FLOOR.draw(vectorOfTile(key.x(), key.y()), (key.x() + key.y()) % 4));

        // Draws all tiles once
        tileMap.entrySet().stream()
                .filter(set -> !set.getValue().getType().equals(Tile.Type.MIRROR))
                .forEach((set ->
                        Image.valueOf(set.getValue().getType().toString())
                                .draw(vectorOfTile(set.getKey().x(), set.getKey().y()), set.getValue().getState())));

        // Extracts lasers from the queue, then draws them.
        laserStorage.stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(this::drawLaser);

        // Draws all mirrors in their current rotation
        mirrorRotations.keySet().forEach(this::drawMirror);

        // Draws all tiles which have collision to cover the laser
        tileMap.entrySet().stream()
                .filter(t -> t.getValue().getCollision() && !t.getValue().getType().isLaserSource())
                .filter(t -> !t.getValue().getType().equals(Tile.Type.STONE_TARGET) && !t.getValue().getType().equals(Tile.Type.MIRROR))
                .forEach(t -> Image.valueOf(t.getValue().getType().toString()).draw(vectorOfTile(t.getKey().x(), t.getKey().y()), t.getValue().getState()));

        // Reloads the previously pushed matrix
        g.popMatrix();
    }

    /**
     * Replaces one set of lasers with a new one in the laser queue. This causes the lasers to have a slight fade in/out effect.
     *
     * @param laserSet the new set of lasers to add to the storage.
     */
    private void storeLasers(Set<Laser> laserSet) {
        laserStorage.remove();
        laserStorage.add(laserSet);
    }

    /**
     * Resets the animation features and recalculates max tiles, as well as loading the tileMap once more.
     */
    protected void reset() {
        resetDynamicGraphics();
        tileMap = g.fetchTiles();
        maxTiles = getMaxTiles();
    }

    /**
     * Resets the storages for mirror and laser animation.
     */
    private void resetDynamicGraphics() {
        mirrorRotations = new HashMap<>();
        laserStorage = new LinkedList<>();

        for (int i = 0; i < 4; i++)
            laserStorage.add(Set.of());
    }

    /**
     * Draws a mirror with its animated rotation.
     * <p>
     * Because the rotation storage of the mirrors is separate to that of tiles in general (the tileMap),
     * there are some assertions to ensure expected behavior.
     *
     * @param pos the position of the mirror to draw.
     *            Throws an exception if given position does not have a mirror stored in the mirrorRotations map (set up by the {@link #reset()} method) or there is no mirror present at this position.
     */
    private void drawMirror(Pair<Integer, Integer> pos) {
        if (mirrorRotations.get(pos) == null)
            throw new IllegalArgumentException("Tried to access undefined mirror position");

        if (tileMap.get(pos) == null || !tileMap.get(pos).getType().equals(Tile.Type.MIRROR))
            throw new IllegalArgumentException("Tried to draw mirror at a position which is not a mirror");

        PVector p = vectorOfTile(pos.x(), pos.y());
        g.pushMatrix();
        g.translate(p.x, p.y);
        g.rotate(radians(mirrorRotations.get(pos)));
        Image.MIRROR.draw(new PVector(0, 0), 0);
        g.popMatrix();
    }

    /**
     * Rotationally moves mirrors as appropriate.
     * <p>
     * Mirrors that are not yet facing the direction that their state declares are moved by 22.5 degrees each turn in the direction closest to their target.
     * <p>
     * Source of the rotational calculation: https://math.stackexchange.com/questions/1366869/calculating-rotation-direction-between-two-angles
     */
    private void updateMirrors() {
        tileMap.entrySet().stream()
                .filter(set -> set.getValue().getType().equals(Tile.Type.MIRROR))
                .forEach(set -> mirrorRotations.put(set.getKey(), mirrorRotations.getOrDefault(set.getKey(), set.getValue().getState() * 90f)));

        mirrorRotations.entrySet().stream()
                .filter(set -> set.getValue() != tileMap.get(set.getKey()).getState() * 90f)
                .forEach(set -> {
                    float delta = (360f + ((tileMap.get(set.getKey()).getState() * 90f) - set.getValue())) % 360f;

                    if (delta > 180)
                        set.setValue(abs(delta) < 22.5f ? tileMap.get(set.getKey()).getState() * 90f : set.getValue() - 22.5f);
                    else
                        set.setValue(abs(delta) < 22.5f ? tileMap.get(set.getKey()).getState() * 90f : set.getValue() + 22.5f);
                });
    }

    protected boolean mirrorsFinished() {
        return mirrorRotations.entrySet().stream()
                .filter(set -> set.getValue() != tileMap.get(set.getKey()).getState() * 90f)
                .findAny().isEmpty();
    }

    /**
     * Draws a specific laser along the points, with the specified color.
     *
     * @param l the Laser.
     */
    private void drawLaser(Laser l) {
        switch (l.color()) {
            case RED -> g.stroke(255, 0, 0, 30 + g.random(30));
            case BLUE -> g.stroke(0, 0, 255, 30 + g.random(30));
            case GREEN -> g.stroke(0, 255, 0, 30 + g.random(30));
        }

        PVector[] a = l.points().stream()
                .map(v -> vectorOfTile((int) v.x, (int) v.y))
                .toArray(PVector[]::new);

        for (int i = 0; i < a.length - 1; i++)
            g.line(a[i].x, a[i].y, a[i + 1].x, a[i + 1].y);
    }


    /**
     * Converts a tile position on the tile map into its relative position on the board canvas.
     * Does not require the position to contain a tile.
     * <p>
     * This method will return the vector needed to draw on the smaller board canvas, not the entire canvas!
     * It also assumes the center to be 0, 0. In short, don't use this anywhere else.
     *
     * @param x x-Position of the Tile (index).
     * @param y y-Position of the Tile (index).
     * @return The PVector pointing to the center of the given Tile position.
     */
    private PVector vectorOfTile(int x, int y) {
        float m = getTileSize();

        return new PVector(((x - maxTiles.x() * 0.5f) * m), ((y - maxTiles.y() * 0.5f) * m));
    }

    /**
     * Converts an absolute position on the canvas into the tile position (index) which it is pointing to.
     * <p>
     * This method assumes the vector given is one from the larger canvas - not just the board canvas.
     * It is mostly used to calculate mouse interaction.
     *
     * @param pos Relative position from which to get the tile index.
     * @return The pair representing the position which the PVector points to.
     * @see App#mouseReleased()
     */
    protected Pair<Integer, Integer> tileOfVector(PVector pos) {
        float m = getTileSize();

        if (pos.x < x1 || pos.x > x2 || pos.y < y1 || pos.y > y2)
            return null;

        PVector zero = vectorOfTile(0, 0);

        float cX = x1 + ((x2 - x1) / 2f);
        float cY = y1 + ((y2 - y1) / 2f);

        float rX = pos.x - (zero.x + cX) + m / 2f;
        float rY = pos.y - (zero.y + cY) + m / 2f;

        return Pair.of(floor(rX / m), floor(rY / m));
    }

    /**
     * Calculates the size of tiles for the given (physical) board size as well as the amount of tiles in both the x- and y-dimension.
     *
     * @return the tile size for the current board size and level.
     */
    protected float getTileSize() {
        return min((x2 - x1) / maxTiles.x(), (y2 - y1) / maxTiles.y(), 50);
    }

    /**
     * Calculates the maximum tiles in both the x- and y-dimension.
     *
     * @return the pair of maximum tiles in the x and y direction.
     */
    private Pair<Integer, Integer> getMaxTiles() {
        return tileMap.keySet().stream()
                .reduce(Pair.of(0, 0), (m, p) -> m = Pair.of(Integer.max(m.x(), p.x()), Integer.max(m.y(), p.y())));
    }
}
