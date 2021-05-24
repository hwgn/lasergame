package engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import processing.core.PConstants;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import static org.junit.jupiter.api.Assertions.*;

class LaserEngineTest {

    static JSONArray testLevels;
    static List<Level> testLevelList;
    static List<String> levelStrings = List.of(
            "Level 1|1|1.1.0.STONE:1.2.0.NULL:1.3.2.MIRROR", // basic functionality tests can be done here
            "Level 2|2|5.5.0.LASER_RED:5.4.0.NULL:5.3.0.STONE_TARGET" // basic laser tests can be done here
            );

    /**
     * Sets up the test levels needed to test various features of the level behaviour.
     */
    @BeforeAll
    static void setupLevelArrays() {
        Collector<JSONObject, JSONArray, JSONArray> jsonObjCollector = Collector
                .of(JSONArray::new, JSONArray::append, (arr1, arr2) -> {
                    for (int i = 0; i < arr1.size(); i++)
                        arr2.append(arr1.getJSONObject(i));
                    return arr2;
                });


        testLevels = levelStrings.stream()
                .map(s -> {
                    String[] sections = s.split("\\|"); // divider between level data fields

                    JSONArray tiles = Arrays.stream(sections[2]
                            .split(":")) // divider between tiles
                            .map(t -> {
                                String[] tileInfo = t.split("\\."); // divider between tile data fields
                                JSONObject tile = new JSONObject();
                                tile.setInt("x", Integer.parseInt(tileInfo[0]));
                                tile.setInt("y", Integer.parseInt(tileInfo[1]));
                                tile.setInt("state", Integer.parseInt(tileInfo[2]));
                                tile.setString("type", tileInfo[3]);
                                return tile;
                            })
                            .collect(jsonObjCollector);

                    JSONObject output = new JSONObject();
                    output.setString("description", sections[0]);
                    output.setInt("min_moves", Integer.parseInt(sections[1]));
                    output.setJSONArray("tiles", tiles);
                    return output;
                }).collect(jsonObjCollector);

        testLevelList = Arrays.stream(Level.initialize(testLevels)).toList();
    }

    /**
     * Asserts that calling the constructor with an array of levels will return a functional map of tiles
     * with the expected data fields.
     */
    @Test
    void basicFunctionality() {
        LaserEngine engine = new LaserEngine(testLevels);
        Map<Pair<Integer, Integer>, Tile> tiles = engine.getCopyOfTiles();

        // Testing for data validity
        assertNotNull(tiles, "Tiles mustn't be null");
        assertNotEquals(0, tiles.size(), "Tiles mustn't be empty");

        // Testing for type consistency
        assertEquals(tiles.get(Pair.of(1, 1)).getType(),
                testLevelList.get(0).tiles().get(Pair.of(1, 1)).getType(),
                "Tile at 1.1 was not identified to be the same in JSONArray as in initialised Tiles");

        // Testing for state consistency
        assertEquals(tiles.get(Pair.of(1, 3)).getState(),
                testLevelList.get(0).tiles().get(Pair.of(1, 3)).getState(),
                "Tile state at 1.3 was not the same between JSONArray and initialised Tiles");
    }

    @Test
    void update() {

    }

    @Test
    void registerInteraction() {
        LaserEngine engine = new LaserEngine(testLevels);
        Map<Pair<Integer, Integer>, Tile> initial = engine.getCopyOfTiles();
        engine.registerInteraction(Pair.of(1, 3), PConstants.LEFT);
        Map<Pair<Integer, Integer>, Tile> interacted = engine.getCopyOfTiles();

        assertNotEquals(initial.get(Pair.of(1, 3)).getState(),
                interacted.get(Pair.of(1, 3)).getState(),
                "Interaction did not cause tile state of mirror to change");
    }

    @Test
    void getMoves() {
    }

    @Test
    void getOptimalMoves() {
    }

    @Test
    void getLevelDescription() {
    }

    @Test
    void getCopyOfTiles() {
    }

    @Test
    void updateLasers() {
    }

    @Test
    void requestLevel() {
    }

    @Test
    void getLasers() {
    }

    @Test
    void isCompleted() {
    }

    @Test
    void getMedalID() {
    }
}