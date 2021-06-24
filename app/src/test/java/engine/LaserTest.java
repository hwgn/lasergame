package engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import processing.core.PConstants;
import processing.data.JSONArray;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LaserTest {

    static JSONArray levelList;

    @BeforeAll
    static void setUp() {
        levelList = GameEngineTest.getTestLevels();
    }

    @Test
    void getLasers() {
        GameEngine engine = new GameEngine(levelList);
        assertTrue(engine.getLasers().isEmpty(), "Level with no lasers didn't return empty list");

        engine.requestLevel(2);
        assertFalse(engine.getLasers().isEmpty(), "Level with lasers returned empty list");

        engine.requestLevel(-1);
        assertFalse(engine.getLasers().isEmpty(), "Self completing level with lasers returned empty list");
    }

    @Test
    void color() {
        GameEngine engine = new GameEngine(levelList);
        engine.requestLevel(2);
        assertEquals(Laser.Color.RED, engine.getLasers().stream().findFirst().orElseThrow().color(), "Color from red laser source was not red");

        engine.requestLevel(1);
        assertEquals(Laser.Color.BLUE, engine.getLasers().stream().findFirst().orElseThrow().color(), "Color from blue laser source was not blue");
    }

    @Test
    void points() {
        GameEngine engine = new GameEngine(levelList);

        engine.requestLevel(1);
        assertEquals(List.of(Pair.of(5, 5), Pair.of(5, 3)), engine.getLasers().stream().findFirst().orElseThrow().points(), "Unexpected laser path");

        engine.requestLevel(1);
        assertEquals(List.of(Pair.of(10, 5), Pair.of(10, 3)), engine.getLasers().stream().findFirst().orElseThrow().points(), "Unexpected laser path");

        engine.registerInteraction(Pair.of(10, 3), PConstants.RIGHT);
        assertEquals(List.of(Pair.of(10, 5), Pair.of(10, 3), Pair.of(12, 3)), engine.getLasers().stream().findFirst().orElseThrow().points(), "Unexpected laser path");
    }

    @Test
    void isComplete() {
        GameEngine engine = new GameEngine(levelList);

        engine.requestLevel(1);
        assertTrue(engine.getLasers().stream().findFirst().orElseThrow().isComplete(), "Self completing laser not considered complete");

        engine.requestLevel(1);
        assertFalse(engine.getLasers().stream().findFirst().orElseThrow().isComplete(), "Laser considered itself complete before reaching target");

        engine.registerInteraction(Pair.of(10, 3), PConstants.RIGHT);
        assertTrue(engine.getLasers().stream().findFirst().orElseThrow().isComplete(), "Laser not considered complete after reaching target through laser");
    }
}