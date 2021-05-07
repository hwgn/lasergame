package engine;

import processing.core.PVector;

import java.util.*;

public class Laser {
    Color color;
    List<PVector> points;
    boolean isComplete;

    private Laser(Color color, List<PVector> points, boolean isComplete) {
        this.color = color;
        this.points = points;
        this.isComplete = isComplete;
    }

    public static Set<Laser> getLasers(Map<Pair<Integer, Integer>, Tile> tiles) {
        Set<Laser> lasers = new HashSet<>();
        tiles.forEach((pos, tile) -> {
            if (tile.getType().isLaserSource())
                lasers.add(determinePath(pos, tile.getState(), tiles));
        });
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
                if (tiles.get(pos).getState() == rotation) rotation++;
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
        switch (rotation) {
            case 0 -> pos = Pair.of(pos.x(), pos.y() - 1);
            case 1 -> pos = Pair.of(pos.x() + 1, pos.y());
            case 2 -> pos = Pair.of(pos.x(), pos.y() + 1);
            case 3 -> pos = Pair.of(pos.x() - 1, pos.y());
        }
        return pos;
    }

    public Color getColor() {
        return color;
    }

    public PVector[] getPoints() {
        return points.toArray(new PVector[0]);
    }

    public enum Color {
        RED(1), BLUE(2), GREEN(3);
        public int rotation;

        Color(int rotation) {
            this.rotation = rotation;
        }
    }
}
