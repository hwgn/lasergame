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
        boolean isComplete = pathFinder(pos, rotation, tiles, points);

        return new Laser(color, points, isComplete);
    }

    /**
     * The pathfinder method will set up the points list of the laser by walking through the tiles and rotating / stopping as needed.
     *
     * @param pos      the starting position of the Laser.
     * @param rotation the initial direction the laser is facing.
     * @param tiles    the tile map.
     * @param points   the points list. This list will be appended to during the method execution.
     * @return true, if the Laser is complete (ends on a target).
     */
    private static boolean pathFinder(Pair<Integer, Integer> pos, int rotation, Map<Pair<Integer, Integer>, Tile> tiles, List<PVector> points) {
        boolean isComplete = false;
        pos = getNextPosition(pos, rotation);

        while (true) {
            points.add(new PVector(pos.x(), pos.y()));

            if (tiles.get(pos) == null)
                break;

            if (tiles.get(pos).getType().equals(Tile.Type.MIRROR)) {
                if (tiles.get(pos).getState() == rotation) rotation = (rotation + 1) % 4;
                else if (tiles.get(pos).getState() == (rotation + 1) % 4) rotation = (3 + rotation) % 4;
                else break;

            } else if (tiles.get(pos).getCollision()) {
                isComplete = tiles.get(pos).getType().equals(Tile.Type.STONE_TARGET);
                break;
            }

            do {
                pos = getNextPosition(pos, rotation);
            } while (tiles.get(pos) != null && !tiles.get(pos).getCollision());

        }
        return isComplete;
        //TODO put this in tiles maybe
    }

    /**
     * Returns the position one step ahead of the current position, in the direction of the given rotation.
     *
     * @param pos      current position.
     * @param rotation current rotation.
     * @return new position one step into the given direction, from the given position.
     */
    private static Pair<Integer, Integer> getNextPosition(Pair<Integer, Integer> pos, int rotation) {

        Pair<Integer, Integer> move = List.of(Pair.of(0, -1), Pair.of(1, 0), Pair.of(0, 1), Pair.of(-1, 0)).get(rotation);
        return Pair.of(move.x() + pos.x(), move.y() + pos.y());

    }

    /**
     * Enum for the various colors a laser can have.
     */
    public enum Color {
        RED, BLUE, GREEN
    }
}
