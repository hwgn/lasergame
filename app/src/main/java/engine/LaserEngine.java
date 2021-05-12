package engine;

import main.App;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LaserEngine implements Engine {
    public static final int maxTiles = 16;
    private static Level[] levels;
    private final int currentLevel;
    private final Map<Pair<Integer, Integer>, Tile> tiles;
    private Set<Laser> lasers;
    private int moves = 0;

    public LaserEngine(int l, App app) {
        levels = Level.initialize(app);
        tiles = levels[l].getTiles();
        currentLevel = l;
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
        return levels[currentLevel].minMoves();
    }

    public String getLevelDescription() {
        return levels[currentLevel].description();
    }

    public Map<Pair<Integer, Integer>, Tile> getTiles() {
        Map<Pair<Integer, Integer>, Tile> output = new HashMap<>();
        tiles.forEach((key, value) -> output.put(key, value.clone()));
        return output;
    }

    public void updateLasers() {
        lasers = Laser.getLasers(getTiles());
        tiles.values().stream().filter(t -> t.getType().isLaserSwitch()).forEach(Tile::resetState);
        lasers.stream().filter(Laser::isComplete)
                .forEach(l -> tiles.values().stream()
                        .filter(t -> t.getType().equals(Tile.Type.getSwitchByColor(l.color())))
                        .forEach(t -> t.interact(0, tiles)));
    }

    public Set<Laser> getLasers() {
        return lasers;
    }
}
