package engine;

import main.App;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Level {
    private final String description;
    private final Map<Pair<Integer, Integer>, Tile> tiles;
    private final int minMoves;

    Level(Map<Pair<Integer, Integer>, Tile> tiles, String description, int minMoves) {
        this.description = description;
        this.tiles = tiles;
        this.minMoves = minMoves;
    }

    /**
     * Reads all levels from the json file and replaces the static levels value with them.
     *
     * @param app App instance, needed for Processing methods regarding json files
     */
    public static Level[] initialize(App app) {
        JSONArray levelArray = app.loadJSONArray("src/levels.json");
        List<Level> levels = new ArrayList<>();

        for (int i = 0; i < levelArray.size(); i++) {
            JSONArray tileArray = levelArray.getJSONObject(i).getJSONArray("tiles");
            Map<Pair<Integer, Integer>, Tile> tiles = new HashMap<Pair<Integer, Integer>, Tile>();

            for (int k = 0; k < tileArray.size(); k++) {
                JSONObject tile = tileArray.getJSONObject(k);

                tiles.put(Pair.of(tile.getInt("x"), tile.getInt("y")),
                        Tile.of(Tile.Type.valueOf(tile.getString("type")), tile.getInt("state")));

            }
            levels.add(new Level(tiles, levelArray.getJSONObject(i).getString("description"), levelArray.getJSONObject(i).getInt("min_moves")));
        }
        return levels.toArray(new Level[0]);
    }

    public Map<Pair<Integer, Integer>, Tile> getTiles() {
        Map<Pair<Integer, Integer>, Tile> output = new HashMap<>();
        tiles.forEach((key, value) -> output.put(key.clone(), value.clone()));
        return output;
    }

    public int getMinMoves() {
        return minMoves;
    }

    public String getDescription() {
        return description;
    }
}
