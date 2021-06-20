package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LevelTest {
    List<Level> levels;

    @BeforeEach
    void loadLevels() {
        levels = GameEngineTest.getTestLevelList();
    }

    @Test
    void tiles() {
        assertNotNull(levels.get(0).tiles(), "Generated Level had tile map that was null");

        assertFalse(levels.get(0).tiles().isEmpty(), "Level with given tile map contents was null");

        assertTrue(levels.get(0).tiles().containsKey(Pair.of(1, 1)), "Level didn't contain given tile");

        assertEquals(levels.get(0).tiles().get(Pair.of(1,1)).getType(), Tile.Type.STONE, "Level tile type was unexpected");
    }

    @Test
    void description() {
        assertNotNull(levels.get(0).description(), "Description of first level was null");
        assertNotNull(levels.get(1).description(), "Description of level 2 was null");

        assertEquals(levels.get(0).description(), "Level 1", "Description of level 1 was unexpected");
        assertEquals(levels.get(1).description(), "Level 2", "Description of second level was unexpected");
    }

    @Test
    void minMoves() {
        assertEquals(0, levels.get(0).minMoves(), "Unexpected min moves for level 1");
        assertEquals(1, levels.get(2).minMoves(), "Unexpected min moves for level 3");
        assertEquals(999, levels.get(3).minMoves(), "Unexpected min moves for level 4");
    }
}