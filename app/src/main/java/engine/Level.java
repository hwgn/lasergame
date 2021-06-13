package engine;

import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Level record. Stores information about Levels which it can retrieve using the {@link #initialize(JSONArray)}-Method.
 */
record Level(Map<Pair<Integer, Integer>, Tile> tiles,
             String description, int minMoves) {

    /**
     * Reads all levels from the given {@link JSONArray} and returns them in an array.
     *
     * @param levelArray {@link JSONArray} storing the levels file.
     * @return Array of all levels in their initial state.
     */
    public static Level[] initialize(JSONArray levelArray) {
        List<Level> levels = new ArrayList<>();

        for (int i = 0; i < levelArray.size(); i++) {
            JSONArray tileArray = levelArray.getJSONObject(i).getJSONArray("tiles");
            Map<Pair<Integer, Integer>, Tile> tiles = new HashMap<>();

            for (int k = 0; k < tileArray.size(); k++) {
                JSONObject tile = tileArray.getJSONObject(k);

                tiles.put(Pair.of(tile.getInt("x"), tile.getInt("y")),
                        Tile.of(Tile.Type.valueOf(tile.getString("type")), tile.getInt("state")));
            }

            levels.add(new Level(tiles, levelArray.getJSONObject(i).getString("description"), levelArray.getJSONObject(i).getInt("min_moves")));
        }

        return levels.toArray(new Level[0]);
    }
}
