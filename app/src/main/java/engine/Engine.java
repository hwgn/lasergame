package engine;

import java.util.Map;
import java.util.Set;

/**
 * The interface through which the frontend may interact with the logic of the game.
 */
public interface Engine {
    /**
     * Registers the interaction with one specific tile.
     *
     * @param pos         position of the tile to be interacted with.
     * @param mouseButton value representing the mouseButton, according to PConstants.
     * @throws IllegalArgumentException  if the tile at this position cannot be interacted with.
     * @throws IndexOutOfBoundsException if there is no tile at this position.
     */
    void registerInteraction(Pair<Integer, Integer> pos, int mouseButton) throws IllegalArgumentException, IndexOutOfBoundsException;

    /**
     * Updates (reloads) lasers, including their position, path and state.
     */
    void update();

    /**
     * @return the current amount of moves taken in this round.
     */
    int getMoves();

    /**
     * @return the optimal amount of moves for the current level.
     */
    int getOptimalMoves();

    /**
     * @return the description for the current level.
     */
    String getLevelDescription();

    /**
     * @return all tiles for this level and their current state.
     */
    Map<Pair<Integer, Integer>, Tile> getCopyOfTiles();

    /**
     * @return all active lasers including positional and color information.
     */
    Set<Laser> getLasers();

    /**
     * @return the ID representing the medal currently obtained on this level.
     */
    int getMedalID();

    /**
     * @return the index / ID of the current level.
     */
    int getLevelID();

    /**
     * @return true, if the current level is completed.
     */
    boolean isCompleted();

    /**
     * Requests a level using a direction-based system.
     * <p>
     * Calling the method with "1" would make the engine load the next level (or reload the current one if no such level exists).
     * Similarly, calling the method using "-1" would load the previous level if it exists.
     * <p>
     * This enables to also move in greater steps, especially for testing.
     *
     * @param direction the value which will be added to the current index (will not cause IndexOutOfBoundExceptions)
     */
    void requestLevel(int direction);

}
