package engine;

import processing.data.JSONArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The main Engine running the game.
 * <p>
 * Handles interactions and storage of tile data, and hands important information
 * to the frontend.
 * <p>
 * Also handles storage of game data such as the current moves taken and achieved medals. Medals are stored for the entirety of a session.
 */
public final class GameEngine implements Engine {
    /**
     * The Level array. Used for loading all levels and later storing medal data.
     */
    private final JSONArray levelArray;
    /**
     * True, if the current game has been completed. Used to display game over popups in frontend.
     */
    private boolean completed;
    /**
     * The amount of moves taken in this round.
     */
    private int moves,
    /**
     * The current level ID.
     *
     * @see #requestLevel(int)
     */
    levelID = 0;
    /**
     * The current level.
     *
     * @see #levelSetup()
     */
    private Level level;
    /**
     * The map of tiles in their current state.
     */
    private Map<Pair<Integer, Integer>, Tile> tiles;
    /**
     * The currently stored version of the lasers.
     *
     * @see #updateLasers()
     */
    private Set<Laser> lasers;

    /**
     * Instantiates the Engine.
     *
     * @param array the {@link JSONArray} storing the level data
     */
    public GameEngine(JSONArray array) {
        this.levelArray = array;
        levelSetup();
    }

    /**
     * Sets up the current level and resets game data.
     */
    private void levelSetup() {
        level = Level.initialize(levelArray)[levelID];
        tiles = level.tiles();
        completed = false;
        moves = 0;
        update();
    }

    /**
     * Updates lasers and medal, if appropriate.
     */
    public void update() {
        updateLasers();
        if (completed)
            updateMedal();
    }

    /**
     * If possible, registers an interaction with a tile and updates the move count and lasers.
     *
     * @param pos         position of the tile to be interacted with.
     * @param mouseButton value representing the mouseButton, according to PConstants.
     * @throws IllegalArgumentException if the given position does not contain a tile
     * @throws IllegalStateException    if the game is complete
     */
    public void registerInteraction(Pair<Integer, Integer> pos, int mouseButton) {
        if (tiles.get(pos) == null)
            throw new IllegalArgumentException("This position does not contain a tile.");

        if (completed)
            throw new IllegalStateException("The game cannot register interactions when completed.");

        tiles.get(pos).interact(mouseButton, tiles);
        moves++; // only done up if interact didn't throw an exception
        update();

    }

    /**
     * Getter for the move count.
     *
     * @return the amount of moves taken in this round
     */
    public int getMoves() {
        return moves;
    }

    /**
     * Getter for the optimal amount of moves for this level.
     *
     * @return the optimal amount of moves (determined when creating the levels)
     */
    public int getOptimalMoves() {
        return level.minMoves();
    }

    /**
     * Getter for the level description.
     *
     * @return the level description
     */
    public String getLevelDescription() {
        return level.description();
    }

    /**
     * Copies the tile map and returns it.
     *
     * @return copy of the tile map
     */
    public Map<Pair<Integer, Integer>, Tile> getCopyOfTiles() {
        Map<Pair<Integer, Integer>, Tile> output = new HashMap<>();
        tiles.forEach((key, value) -> output.put(key, value.clone()));
        return output;
    }

    /**
     * Updates the lasers and the {@link #completed} variable.
     * <p>
     * This needs to be done multiple times (see the for loop) as otherwise switches may not be updated in time.
     */
    public void updateLasers() {
        tiles.values().stream().filter(t -> t.getType().isLaserSwitch()).forEach(Tile::resetState);
        lasers = Laser.getLasers(getCopyOfTiles());

        for (int i = 0; i < lasers.size(); i++) {
            lasers.stream()
                    .filter(Laser::isComplete)
                    .forEach(l -> tiles.values().stream()
                            .filter(t -> t.getType().equals(Tile.Type.getSwitchByColor(l.color())))
                            .forEach(t -> t.interact(0, tiles)));

            lasers = Laser.getLasers(getCopyOfTiles());
        }

        completed = lasers.stream().filter(Laser::isComplete).count() == lasers.size();
    }

    /**
     * Requests a specific level
     *
     * @param shift the position offset the new level is located at.
     *              <p>
     *              1 would be the next, -1 the previous, and 0 the same level reloaded.
     */
    public void requestLevel(int shift) {
        if (shift >= levelArray.size() || levelID + shift >= levelArray.size())
            levelID = levelArray.size() - 1;
        else if (levelID + shift <= 0)
            levelID = 0;
        else
            levelID += shift;

        levelSetup();
    }

    /**
     * Getter for the laser set.
     *
     * @return the laser set
     */
    public Set<Laser> getLasers() {
        return lasers;
    }

    /**
     * Getter for the completion state.
     *
     * @return true, if current round is completed
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Getter for the medal ID of the current level.
     * <p>
     * 0 - gold, 1 - silver, 2 - bronze, 3 - none.
     *
     * @return the medal ID.
     */
    public int getMedalID() {
        return levelArray.getJSONObject(levelID).getInt("medal", 3);
    }

    /**
     * Getter for the current level ID.
     *
     * @return the current level ID
     */
    public int getLevelID() {
        return levelID;
    }

    /**
     * Updates the current medal and stores it.
     */
    private void updateMedal() {
        levelArray.getJSONObject(levelID).setInt("medal", Arrays.stream(Medal.values())
                .filter(m -> moves - getOptimalMoves() <= m.maxMistakes)
                .mapToInt(Enum::ordinal)
                .filter(m -> m <= levelArray.getJSONObject(levelID).getInt("medal", 3))
                .findFirst().orElse(levelArray.getJSONObject(levelID).getInt("medal", 3)));
    }

    /**
     * The Medal enum.
     */
    private enum Medal {
        /**
         * Gold medal.
         */
        GOLD(0),
        /**
         * Silver medal.
         */
        SILVER(4),
        /**
         * Bronze medal.
         */
        BRONZE(9),
        /**
         * No medal. What a pity.
         */
        NONE(Integer.MAX_VALUE);

        /**
         * The max mistakes allowed to achieve this medal.
         */
        public final int maxMistakes;

        /**
         * Instantiates a new medal.
         *
         * @param maxMistakes the max mistakes
         */
        Medal(int maxMistakes) {
            this.maxMistakes = maxMistakes;
        }
    }
}
