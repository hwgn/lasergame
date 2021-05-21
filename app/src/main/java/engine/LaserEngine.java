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
    private Level level;
    private Map<Pair<Integer, Integer>, Tile> tiles;
    private Set<Laser> lasers;
    private int moves, levelID = 0;
    private boolean completed = false;

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
        if(completed)
            updateMedal();
    }

    public void registerInteraction(Pair<Integer, Integer> pos, int mouseButton) {
        if (tiles.get(pos) == null || !tiles.get(pos).getType().canInteract() || completed)
            throw new IllegalStateException("This interaction is impossible at this time.");

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
        for (int i = 0; i < lasers.size(); i++) {
            lasers.stream().sorted(Comparator.comparing(l -> l.color().ordinal())).filter(Laser::isComplete)
                    .forEach(l -> tiles.values().stream()
                            .filter(t -> t.getType().equals(Tile.Type.getSwitchByColor(l.color())))
                            .forEach(t -> t.interact(0, tiles)));
            lasers = Laser.getLasers(getCopyOfTiles());
        }
        completed = lasers.stream().filter(Laser::isComplete).count() == lasers.size();
    }

    public void requestLevel(int direction) {
        if (levelID + direction < 0 || levelID + direction >= levelArray.size())
            direction = 0;
        levelID += direction;
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


    private void updateMedal() {
        levelArray.getJSONObject(levelID).setInt("medal", Arrays.stream(Medal.values())
                .filter(m -> moves - getOptimalMoves() <= m.maxMistakes)
                .mapToInt(Enum::ordinal)
                .filter(m -> m <= levelArray.getJSONObject(levelID).getInt("medal", 3))
                .findFirst().orElse(levelArray.getJSONObject(levelID).getInt("medal", 3)));
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
