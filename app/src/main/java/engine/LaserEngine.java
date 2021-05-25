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
    private final JSONArray levelArray;
    private boolean completed = false;
    private int moves, levelID = 0;
    private Level level;
    private Map<Pair<Integer, Integer>, Tile> tiles;
    private Set<Laser> lasers;

    public LaserEngine(JSONArray array) {
        this.levelArray = array;
        levelSetup();
    }

    private void levelSetup() {
        level = Level.initialize(levelArray)[levelID];
        tiles = level.tiles();
        completed = false;
        moves = 0;
    }

    public void update() {
        updateLasers();
        if (completed)
            updateMedal();
    }

    public void registerInteraction(Pair<Integer, Integer> pos, int mouseButton) throws IllegalArgumentException, IllegalStateException {
        if (tiles.get(pos) == null)
            throw new IllegalArgumentException("This position does not contain a tile.");

        if (completed)
            throw new IllegalStateException("The game cannot register interactions when completed.");

        tiles.get(pos).interact(mouseButton, tiles);
        moves++; // only done up if interact didn't throw an exception

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

        for (int i = 0; i < lasers.size(); i++) {
            lasers.stream().sorted(Comparator.comparing(l -> l.color().ordinal())).filter(Laser::isComplete)
                    .forEach(l -> tiles.values().stream()
                            .filter(t -> t.getType().equals(Tile.Type.getSwitchByColor(l.color())))
                            .forEach(t -> t.interact(0, tiles)));

            lasers = Laser.getLasers(getCopyOfTiles());
        }

        completed = lasers.stream().filter(Laser::isComplete).count() == lasers.size();
    }

    public void requestLevel(int shift) {
        if (shift >= levelArray.size() || levelID + shift >= levelArray.size())
            levelID = levelArray.size() - 1;
        else if (levelID + shift <= 0)
            levelID = 0;
        else
            levelID += shift;

        levelSetup();
    }

    public Set<Laser> getLasers() {
        return lasers;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getMedalID() {
        return levelArray.getJSONObject(levelID).getInt("medal", 3);
    }

    public int getLevelID() {
        return levelID;
    }

    private void updateMedal() {
        levelArray.getJSONObject(levelID).setInt("medal", Arrays.stream(Medal.values())
                .filter(m -> moves - getOptimalMoves() <= m.maxMistakes)
                .mapToInt(Enum::ordinal)
                .filter(m -> m <= levelArray.getJSONObject(levelID).getInt("medal", 3))
                .findFirst().orElse(levelArray.getJSONObject(levelID).getInt("medal", 3)));
    }

    public Pair<Integer, Integer> getMaxTiles() {
        return tiles.keySet().stream().reduce(Pair.of(0, 0), (m, p) -> m = Pair.of(Integer.max(m.x(), p.x()), Integer.max(m.y(), p.y())));
    }

    private enum Medal {
        GOLD(0),
        SILVER(4),
        BRONZE(9),
        NONE(Integer.MAX_VALUE);

        public final int maxMistakes;

        Medal(int maxMistakes) {
            this.maxMistakes = maxMistakes;
        }
    }
}
