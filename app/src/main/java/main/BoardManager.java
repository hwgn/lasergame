package main;

import engine.Laser;
import engine.Pair;
import engine.Tile;
import processing.core.PVector;

import java.util.*;

import static processing.core.PApplet.*;

public class BoardManager {
    private final App g;
    private HashMap<Pair<Integer, Integer>, Float> mirrorRotations;
    private Queue<Set<Laser>> laserStorage;
    private Map<Pair<Integer, Integer>, Tile> tileMap;
    private int x1, y1, x2, y2;

    BoardManager(App g) {
        this.g = g;
        resetDynamicGraphics();
    }

    protected void reset() {
        resetDynamicGraphics();
        tileMap = g.fetchTiles();
    }

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
     * Draws board of tiles.
     * <p>
     * First draws the floor using a pattern, then all tiles at once over it.
     * <p>
     * After drawing all lasers, it now draws all tiles which have collision except for the target block, to cover
     * a potential laser hitting a wall.
     */
    private void drawBoard() {
        g.pushMatrix();
        g.translate(x1 + ((x2 - x1) / 2f), y1 + ((y2 - y1) / 2f));

        g.fill(66);
        g.noStroke();

        // Draws outline around all tiles
        tileMap.keySet().forEach(key -> {
            PVector pos = vectorOfTile(key.x(), key.y());
            g.square(pos.x - getTileSize(), pos.y - getTileSize(), getTileSize() * 2);
        });

        // Draws a floor image for every tile present
        tileMap.keySet().forEach(key ->
                Image.FLOOR.draw(vectorOfTile(key.x(), key.y()), (key.x() + key.y()) % 4));

        // Draws all tiles once
        tileMap.entrySet().stream()
                .filter(set -> !set.getValue().getType().equals(Tile.Type.MIRROR))
                .forEach((set ->
                        Image.valueOf(set.getValue().getType().toString())
                                .draw(vectorOfTile(set.getKey().x(), set.getKey().y()), set.getValue().getState())));

        // Sorts lasers by color to avoid flickering when lasers go across each other
        laserStorage.stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Laser::color))
                .forEach(this::drawLaser);

        // Draws all mirrors
        mirrorRotations.keySet().forEach(this::drawMirror);

        // Draws all tiles which have collision to cover the laser
        tileMap.entrySet().stream()
                .filter(t -> t.getValue().getCollision() && !t.getValue().getType().isLaserSource())
                .filter(t -> !t.getValue().getType().equals(Tile.Type.STONE_TARGET) && !t.getValue().getType().equals(Tile.Type.MIRROR))
                .forEach(t -> Image.valueOf(t.getValue().getType().toString()).draw(vectorOfTile(t.getKey().x(), t.getKey().y()), t.getValue().getState()));

        g.popMatrix();
    }

    private void storeLasers(Set<Laser> laserSet) {
        laserStorage.add(laserSet);
        laserStorage.remove();
    }

    private void resetDynamicGraphics() {
        mirrorRotations = new HashMap<>();
        laserStorage = new ArrayDeque<>();

        for (int i = 0; i < 4; i++)
            laserStorage.add(Set.of());
    }

    private void drawMirror(Pair<Integer, Integer> pos) {
        if (mirrorRotations.get(pos) == null)
            throw new IllegalArgumentException("Tried to access undefined mirror position");

        PVector p = vectorOfTile(pos.x(), pos.y());
        g.pushMatrix();
        g.translate(p.x, p.y);
        g.rotate(radians(mirrorRotations.get(pos)));
        Image.MIRROR.draw(new PVector(0, 0), 0);
        g.popMatrix();
    }

    private void updateMirrors() {
        tileMap.entrySet().stream()
                .filter(set -> set.getValue().getType().equals(Tile.Type.MIRROR))
                .forEach(set -> mirrorRotations.put(set.getKey(), mirrorRotations.getOrDefault(set.getKey(), set.getValue().getState() * 90f)));

        /*
        https://math.stackexchange.com/questions/1366869/calculating-rotation-direction-between-two-angles
         */
        mirrorRotations.entrySet().stream()
                .filter(set -> set.getValue() != tileMap.get(set.getKey()).getState() * 90f)
                .forEach(set -> {
                    float delta = (360f + ((tileMap.get(set.getKey()).getState() * 90f) - set.getValue())) % 360f;

                    if (delta > 180)
                        set.setValue(abs(delta) < 18f ? tileMap.get(set.getKey()).getState() * 90f : set.getValue() - 22.5f);
                    else
                        set.setValue(abs(delta) < 18f ? tileMap.get(set.getKey()).getState() * 90f : set.getValue() + 22.5f);
                });
    }

    protected boolean mirrorsFinished() {
        return mirrorRotations.entrySet().stream()
                .filter(set -> set.getValue() != tileMap.get(set.getKey()).getState() * 90f)
                .findAny().isEmpty();
    }

    /**
     * Draws a specific laser.
     *
     * @param l the Laser.
     */
    private void drawLaser(Laser l) {
        switch (l.color()) {
            case RED -> g.stroke(255, 0, 0, 30 + g.random(30));
            case BLUE -> g.stroke(0, 0, 255, 30 + g.random(30));
            case GREEN -> g.stroke(0, 255, 0, 30 + g.random(30));
        }

        /*
        https://stackoverflow.com/questions/67643914/how-can-i-iterate-over-two-items-of-a-stream-at-once?noredirect=1#comment119564123_67643914
         */
        PVector[] a = l.points().stream()
                .map(v -> vectorOfTile((int) v.x, (int) v.y))
                .toArray(PVector[]::new);

        for (int i = 0; i < a.length - 1; i++)
            g.line(a[i].x, a[i].y, a[i + 1].x, a[i + 1].y);

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

        return new PVector(((x - getMaxTiles().x() * 0.5f) * m), ((y - getMaxTiles().y() * 0.5f) * m));
    }

    /**
     * Converts an absolute position on the canvas into the Tile position (index) which it is pointing to.
     *
     * @param pos Relative position from which to get the Tile index.
     * @return The Pair representing the position which the PVector points to.
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

        float x = rX / m;
        float y = rY / m;

        return Pair.of(floor(x), floor(y));
    }

    protected float getTileSize() {
        Pair<Integer, Integer> maxTiles = getMaxTiles();

        return min((x2 - x1) / maxTiles.x(), (y2 - y1) / maxTiles.y());
    }

    private Pair<Integer, Integer> getMaxTiles() {
        return tileMap.keySet().stream()
                .reduce(Pair.of(0, 0), (m, p) -> m = Pair.of(Integer.max(m.x(), p.x()), Integer.max(m.y(), p.y())));
    }
}
