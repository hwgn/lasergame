package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PConstants;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    /**
     * Change this with caution - these strings are used to test levels / engines and their functionality to a great extent,
     * one change could cause several tests to fail.
     */
    static final List<String> levelStrings = List.of(
            "Level 1|0|1.1.0.STONE:1.2.0.NULL:1.3.2.MIRROR:5.5.1.SWITCH_CYAN", // basic functionality tests can be done here
            "Level 2|0|5.5.0.LASER_RED:5.4.0.NULL:5.3.0.STONE_TARGET", // basic laser tests can be done here (does not have any intractability)
            "Level 3|1|10.5.0.LASER_RED:10.4.0.NULL:10.3.3.MIRROR:11.3.0.NULL:12.3.0.STONE_TARGET" // level with laser that does not complete immediately
    );
    static JSONArray testLevels;
    static List<Level> testLevelList;

    /**
     * Sets up the test levels needed to test various features of the level behaviour.
     * <p>
     * This must be done before each method individually, as medals are stored in the JSON data and would cause unexpected behaviour.
     */
    @BeforeEach
    void setupLevelArrays() {
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
     * Used to get these test levels in other tests
     * @return freshly generated test levels.
     */
    static List<Level> getTestLevelList() {
        GameEngineTest t = new GameEngineTest();
        t.setupLevelArrays();
        return testLevelList;
    }

    /**
     * Asserts that calling the constructor with an array of levels will return a functional map of tiles
     * with the expected data fields.
     */
    @Test
    void instantiation() {
        GameEngine engine = new GameEngine(testLevels);
        Map<Pair<Integer, Integer>, Tile> tiles = engine.getCopyOfTiles();

        assertNotNull(engine.getLevelDescription(), "Level description was null");
        assertEquals(0, engine.getOptimalMoves(), "Optimal moves were false");
        assertNotNull(tiles, "Copy of tiles was null");
        assertTrue(engine.isCompleted(), "Engine didn't realise instant completion");
    }

    @Test
    void update() {

    }

    @Test
    void registerInteraction() {
        // preparation
        GameEngine engine = new GameEngine(testLevels);

        // initial tile map
        Map<Pair<Integer, Integer>, Tile> initial = engine.getCopyOfTiles();

        // interactions
        engine.registerInteraction(Pair.of(1, 3), PConstants.LEFT);
        engine.registerInteraction(Pair.of(5, 5), PConstants.RIGHT);

        // modified tile map
        Map<Pair<Integer, Integer>, Tile> interacted = engine.getCopyOfTiles();

        // checking if interactions worked
        assertNotEquals(initial.get(Pair.of(1, 3)).getState(),
                interacted.get(Pair.of(1, 3)).getState(),
                "Interaction did not cause tile state of mirror to change");

        assertNotEquals(initial.get(Pair.of(5, 5)).getState(),
                interacted.get(Pair.of(5, 5)).getState(),
                "Interaction did not cause tile state of cyan switch to change");

        // checking if bad inputs are reacted to by engine
        assertThrows(IllegalArgumentException.class,
                () -> engine.registerInteraction(Pair.of(Integer.MAX_VALUE, Integer.MAX_VALUE), Integer.MAX_VALUE),
                "Bad interaction (bad position input) threw unexpected or no exception");

        // updating the engine, which will make it realise that its completed (as there are no lasers present)
        engine.update();

        // checking if bad states are reacted to by engine
        assertThrows(IllegalStateException.class, () -> engine.registerInteraction(Pair.of(1, 3), 0),
                "Bad interaction (bad game state) threw unexpected or no exception");
    }

    @Test
    void interactingWhenComplete() {
        GameEngine engine = new GameEngine(testLevels);
        assertThrows(IllegalArgumentException.class, () -> engine.registerInteraction(Pair.of(0, 0), 10));
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
        GameEngine engine = new GameEngine(testLevels);

        assertNotNull(engine.getCopyOfTiles());
        assertNotEquals(engine.getCopyOfTiles(), engine.getCopyOfTiles());

        Map<Pair<Integer, Integer>, Tile> firstCopyOfTiles = engine.getCopyOfTiles();
        Map<Pair<Integer, Integer>, Tile> secondCopyOfTiles = engine.getCopyOfTiles();

        assertNotEquals(0, firstCopyOfTiles.size(),
                "Tiles returned empty map copy on non-empty level");

        // clearing one tile copy
        firstCopyOfTiles.clear();
        assertEquals(0, firstCopyOfTiles.size(),
                "Clearing map did not set size to 0");

        // asserting this has had no effect on the other tile map
        assertNotEquals(0, secondCopyOfTiles.size(),
                "Clearing one copy of a map cleared an unrelated copy as well");

    }

    @Test
    void updateLasers() {
    }

    @Test
    void requestLevel() {
        GameEngine engine = new GameEngine(testLevels);

        assertEquals(0, engine.getLevelID(),
                "Initial levelID should be 0");

        engine.requestLevel(0);
        assertEquals(0, engine.getLevelID(),
                "Requesting direction 0 at ID 0 should yield ID 0");

        engine.requestLevel(-1);
        assertEquals(0, engine.getLevelID(),
                "Requesting direction -1 at ID 0 should yield ID 0");

        engine.requestLevel(1);
        assertEquals(1, engine.getLevelID(),
                "Requesting direction 1 at ID 0 should yield ID 1");

        engine.requestLevel(0);
        assertEquals(1, engine.getLevelID(),
                "Requesting direction 0 at ID 1 should yield ID 1");

        engine.requestLevel(-1);
        assertEquals(0, engine.getLevelID(),
                "Requesting direction -1 at ID 1 should yield ID 0");

        engine.requestLevel(Integer.MAX_VALUE);
        assertEquals(levelStrings.size() - 1, engine.getLevelID(),
                "Sending MAX VALUE should yield the last level ID");

        engine.requestLevel(Integer.MIN_VALUE);
        assertEquals(0, engine.getLevelID(),
                "Sending MIN VALUE should yield the first level ID");
    }

    @Test
    void getLasers() {
        GameEngine engine = new GameEngine(testLevels);

        assertTrue(engine.getLasers().isEmpty(),
                "Laser list of level with no lasers was found not to be empty");

        // switching to level with laser
        engine.requestLevel(1);
        engine.update();

        assertFalse(engine.getLasers().isEmpty(),
                "Laser list of level with a laser was found to be empty");
    }

    @Test
    void isCompleted() {
        GameEngine engine = new GameEngine(testLevels);
        engine.update();

        assertTrue(engine.isCompleted(), "Engine considered incomplete after update on level with no lasers");

        engine.requestLevel(1);
        engine.update();

        assertTrue(engine.isCompleted(), "Engine considered incomplete after update on level with one laser facing the target");
    }

    @Test
    void getMedalID() {
        GameEngine engine = new GameEngine(testLevels);
        assertEquals(0, engine.getMedalID(),
                "Engine did not immediately assign gold medal to self completing level");
    }
}