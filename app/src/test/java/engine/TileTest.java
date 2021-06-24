package engine;

import org.junit.jupiter.api.Test;

import static engine.Tile.Type.FLOOR;
import static engine.Tile.Type.TUNNELS_LEFT;
import static org.junit.jupiter.api.Assertions.*;

class TileTest {

    @Test
    void of() {
        assertEquals(Tile.of(FLOOR, 0), new Tile(FLOOR, 0), "Of method did not produce expected instance contents");
        assertEquals(Tile.of(TUNNELS_LEFT, 12345), new Tile(TUNNELS_LEFT, 12345));
    }

    @Test
    void getType() {
    }

    @Test
    void getState() {
    }

    @Test
    void resetState() {
    }

    @Test
    void hasCollision() {
    }

    @Test
    void interact() {
    }

    @Test
    void getLaserStep() {
    }

    @Test
    void testClone() {
    }
}