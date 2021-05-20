package engine;

import java.util.Map;
import java.util.Set;

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
    void updateLasers();

    /**
     * Returns the current amount of moves taken.
     *
     * @return the current amount of moves taken.
     */
    int getMoves();
    // Returns the amount of moves taken.

    /**
     * Returns the optimal amount of moves for this level.
     *
     * @return the optimal amount of moves for this level.
     */
    int getOptimalMoves();

    /**
     * Returns the description for this level.
     *
     * @return the description for this level.
     */
    String getLevelDescription();

    /**
     * Returns all tiles for this level in their current state.
     *
     * @return all tiles for this level.
     */
    Map<Pair<Integer, Integer>, Tile> getCopyOfTiles();

    /**
     * Returns all lasers active in their current state.
     *
     * @return all active lasers.
     */
    Set<Laser> getLasers();

    boolean isCompleted();

}
