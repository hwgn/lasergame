package engine;

import processing.data.JSONArray;

import java.util.*;

/**
 * The main Engine running the game.
 * <p>
 * Handles interactions and storage of tile data, and hands important information
 * to the frontend.
 * <p>
 * An instance of Engine is tied to one play-through of a specific level only,
 * and therefore can modify the level it is given at will, as all level data
 * will be reloaded whenever a new game is started.
 */
public class LaserEngine implements Engine {
    public static final int maxTiles = 16;
    private static Level level;
    private final Map<Pair<Integer, Integer>, Tile> tiles;
    private Set<Laser> lasers;
    private int moves = 0;
    private boolean completed = false;

    public LaserEngine(int l, JSONArray array) {
        level = Level.initialize(array)[l];
        tiles = level.tiles();
        lasers = new HashSet<>();
    }

    public void registerInteraction(Pair<Integer, Integer> pos, int mouseButton) {
        if (tiles.get(pos) == null)
            throw new IndexOutOfBoundsException("Specified position does not correspond to a tile!");
        if (!tiles.get(pos).getType().canInteract())
            throw new IllegalArgumentException("Specified Tile cannot be interacted with!");

        tiles.get(pos).interact(mouseButton, tiles);
        moves++;

    }

    public int getMoves() {
        return moves;
    }

    public int getOptimalMoves() {
        return level.minMoves();
    }

    public String getLevelDescription() {
        return level.description();
    }

    public Map<Pair<Integer, Integer>, Tile> getCopyOfTiles() {
        Map<Pair<Integer, Integer>, Tile> output = new HashMap<>();
        tiles.forEach((key, value) -> output.put(key, value.clone()));
        return output;
    }

    public void updateLasers() {
        tiles.values().stream().filter(t -> t.getType().isLaserSwitch()).forEach(Tile::resetState);
        lasers = Laser.getLasers(getCopyOfTiles());
        for(int i = 0; i < lasers.size(); i++) {
            lasers.stream().sorted(Comparator.comparing(l -> l.color().ordinal())).filter(Laser::isComplete)
                    .forEach(l -> tiles.values().stream()
                            .filter(t -> t.getType().equals(Tile.Type.getSwitchByColor(l.color())))
                            .forEach(t -> t.interact(0, tiles)));
            lasers = Laser.getLasers(getCopyOfTiles());
        }
        completed = lasers.stream().filter(Laser::isComplete).count() == lasers.size();
    }

    public Set<Laser> getLasers() {
        return lasers;
    }

    public boolean isCompleted() {
        return completed;
    }
}
