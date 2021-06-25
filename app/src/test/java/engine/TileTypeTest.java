package engine;

import org.junit.jupiter.api.Test;

import static engine.Laser.Color.*;
import static engine.Tile.Type.*;
import static org.junit.jupiter.api.Assertions.*;

class TileTypeTest {

    @Test
    void getSwitchByColor() {
        assertEquals(SWITCH_BLUE, Tile.Type.getSwitchByColor(BLUE), "Unexpected output when requesting BLUE laser switch");
        assertEquals(SWITCH_RED, Tile.Type.getSwitchByColor(RED), "Unexpected output when requesting RED laser switch");
        assertEquals(SWITCH_GREEN, Tile.Type.getSwitchByColor(GREEN), "Unexpected output when requesting GREEN laser switch");
    }

    @Test
    void isLaserSwitch() {
        assertTrue(SWITCH_BLUE.isLaserSwitch(), "BLUE Laser switch not identified as such");
        assertTrue(SWITCH_RED.isLaserSwitch(), "RED Laser switch not identified as such");
        assertTrue(SWITCH_GREEN.isLaserSwitch(), "GREEN Laser switch not identified as such");

        assertFalse(SWITCH_YELLOW.isLaserSwitch(), "YELLOW switch identified as laser switch");
        assertFalse(SWITCH_CYAN.isLaserSwitch(), "CYAN switch identified as laser switch");
        assertFalse(SWITCH_MAGENTA.isLaserSwitch(), "MAGENTA switch identified as laser switch");
    }

    @Test
    void isLaserSource() {
        assertTrue(LASER_BLUE.isLaserSource(), "BLUE laser source not identified as such");
        assertTrue(LASER_RED.isLaserSource(), "RED laser source not identified as such");
        assertTrue(LASER_GREEN.isLaserSource(), "GREEN laser source not identified as such");

        assertFalse(STONE.isLaserSource(), "STONE identified as laser source");
        assertFalse(FLOOR.isLaserSource(), "FLOOR identified as laser source");
        assertFalse(SWITCH_RED.isLaserSource(), "RED LASER SWITCH identified as laser source");
        assertFalse(REDIRECT.isLaserSource(), "REDIRECT identified as laser source");
    }
}
