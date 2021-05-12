package engine;

import processing.core.PVector;

import java.util.*;

public record Laser(engine.Laser.Color color, List<PVector> points, boolean isComplete) {

    public static Set<Laser> getLasers(Map<Pair<Integer, Integer>, Tile> tiles) {
        Set<Laser> lasers = new HashSet<>();
        tiles.entrySet().stream()
                .filter(tile -> tile.getValue().getType().isLaserSource())
                .forEach(tile -> lasers.add(determinePath(tile.getKey(), tile.getValue().getState(), tiles)));

        return lasers;
    }

    private static Laser determinePath(Pair<Integer, Integer> pos, int rotation, Map<Pair<Integer, Integer>, Tile> tiles) {
        if (tiles.get(pos) == null)
            throw new IllegalArgumentException("Laser source does not exist!");

        Color color = switch (tiles.get(pos).getType()) {
            case LASER_RED -> Color.RED;
            case LASER_BLUE -> Color.BLUE;
            case LASER_GREEN -> Color.GREEN;
            default -> throw new IllegalArgumentException("Unexpected tile type: " + tiles.get(pos).getType() + "!");
        };

        List<PVector> points = new ArrayList<>();
        boolean isComplete = false;

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
            } while (tiles.get(pos) != null && !tiles.get(pos).getCollision()
                    && pos.x() <= LaserEngine.maxTiles && pos.y() <= LaserEngine.maxTiles);

        }

        return new Laser(color, points, isComplete);
    }

    private static Pair<Integer, Integer> getNextPosition(Pair<Integer, Integer> pos, int rotation) {

        return switch (rotation) {
            case 0 -> Pair.of(pos.x(), pos.y() - 1);
            case 1 -> Pair.of(pos.x() + 1, pos.y());
            case 2 -> Pair.of(pos.x(), pos.y() + 1);
            case 3 -> Pair.of(pos.x() - 1, pos.y());
            default -> throw new IllegalStateException("Unexpected value: " + rotation);
        };

    }

    /**
     * Enum for the various colors a laser can have.
     */
    public enum Color {
        RED, BLUE, GREEN
    }
}
