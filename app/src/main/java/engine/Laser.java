package engine;

import processing.core.PVector;

import java.util.*;

import static engine.Tile.Type.*;

/**
 * The Laser record. Stores information about lasers (such as their position and colour).
 */
public record Laser(engine.Laser.Color color, List<PVector> points, boolean isComplete) {

    /**
     * Determines and creates all lasers of a given tile map.
     *
     * @param tiles tile map used to search for and initialize lasers.
     * @return Set of all lasers.
     */
    public static Set<Laser> getLasers(Map<Pair<Integer, Integer>, Tile> tiles) {
        Set<Laser> lasers = new HashSet<>();
        tiles.entrySet().stream()
                .filter(tile -> tile.getValue().getType().isLaserSource())
                .forEach(tile -> lasers.add(determinePath(tile.getKey(), tile.getValue().getState(), tiles)));

        return lasers;
    }

    /**
     * Creates a Laser instance using a given starting position and rotation as well as the tile map the Laser is navigating through.
     *
     * @param pos      starting position.
     * @param rotation starting rotation (0 = north, 1 = east, ...)
     * @param tiles    the tile map.
     * @return instance of Laser generated using the given parameters.
     */
    private static Laser determinePath(Pair<Integer, Integer> pos, int rotation, Map<Pair<Integer, Integer>, Tile> tiles) {
        if (tiles.get(pos) == null || !tiles.get(pos).getType().isLaserSource())
            throw new IllegalArgumentException("Laser source does not exist!");

        Color color = Map.of(LASER_RED, Color.RED, LASER_BLUE, Color.BLUE, LASER_GREEN, Color.GREEN).get(tiles.get(pos).getType());

        List<PVector> points = new ArrayList<>(List.of(new PVector(pos.x(), pos.y())));
        pos = pathFinder(pos, rotation, tiles, points);
        boolean isComplete = tiles.get(pos).getType().equals(STONE_TARGET);

        return new Laser(color, points, isComplete);
    }

    /**
     * The pathfinder method will set up the points list of the laser by walking through the tiles and rotating / stopping as needed.
     *
     * @param pos      the starting position of the Laser.
     * @param rotation the initial direction the laser is facing.
     * @param tiles    the tile map.
     * @param points   the points list. This list will be appended to during the method execution.
     * @return the position the laser stopped at.
     */
    private static Pair<Integer, Integer> pathFinder(Pair<Integer, Integer> pos, int rotation, Map<Pair<Integer, Integer>, Tile> tiles, List<PVector> points) {
        while (tiles.get(pos).getLaserStep(pos, rotation) != null) {

            Pair<Integer, Integer> newPos = tiles.get(pos).getLaserStep(pos, rotation);

            if (getRotation(pos, newPos) != rotation) {
                rotation = getRotation(pos, newPos);
                points.add(new PVector(pos.x(), pos.y()));
            }
            pos = newPos;
        }

        points.add(new PVector(pos.x(), pos.y()));
        return pos;
    }

    /**
     * Gets the rotation of a laser going from start to stop.
     * <p>
     * Start and stop must be neighbors.
     *
     * @param start the start position
     * @param stop  the stop position
     * @return the value encoding the calculated direction
     */
    private static int getRotation(Pair<Integer, Integer> start, Pair<Integer, Integer> stop) {
        Pair<Integer, Integer> step = Pair.of(stop.x() - start.x(), stop.y() - start.y());

        return List.of(Pair.of(0, -1), Pair.of(1, 0), Pair.of(0, 1), Pair.of(-1, 0)).indexOf(step);
    }

    /**
     * Enum for the various colors a laser can have.
     */
    public enum Color {
        /**
         * The color red!
         */
        RED,
        /**
         * The color blue!
         */
        BLUE,
        /**
         * You guessed it, the color green!
         */
        GREEN
    }
}
