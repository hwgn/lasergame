package engine;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static engine.Tile.Type.*;
import static org.junit.jupiter.api.Assertions.*;

class TileTest {

    @Test
    void of() {
        assertEquals(Tile.of(FLOOR, 0), new Tile(FLOOR, 0), "Of method did not produce expected instance contents");
        assertEquals(Tile.of(TUNNELS_LEFT, 12345), new Tile(TUNNELS_LEFT, 12345), "Of method did not produce expected instance contents");
        assertThrows(IllegalArgumentException.class, () -> Tile.of(null, 0), "Of method with null type did not throw exception");
    }

    @Test
    void getType() {
        assertEquals(STONE, Tile.of(STONE, 1).getType(), "Newly created tile does not have expected type");
        assertEquals(SWITCH_BLUE, new Tile(SWITCH_BLUE, 0).clone().getType(), "Type did not carry over to cloned instance");
    }

    @Test
    void tileSupportsLargeValues() {
        assertDoesNotThrow(() -> new Tile(LASER_RED, Integer.MAX_VALUE), "Tile refused to have max int state");
        assertDoesNotThrow(() -> new Tile(FLOOR, Integer.MIN_VALUE), "Tile refused to have min int state");

        Tile t = new Tile(MIRROR, Integer.MAX_VALUE);
        assertDoesNotThrow(t::getState, "Tile failed to fetch max state");
        assertEquals(Integer.MAX_VALUE, t.getState(), "Tile did not store max value state");
        assertDoesNotThrow(() -> t.interact(1, null), "Interaction with tile of max value state threw exception");
        assertTrue(t.getState() < 4 && t.getState() >= 0, "Interaction didn't return state to allowed range");

        Tile u = new Tile(MIRROR, Integer.MIN_VALUE);
        assertDoesNotThrow(u::getState, "Tile failed to fetch min state");
        assertEquals(Integer.MIN_VALUE, u.getState(), "Tile did not store min value state");
        assertDoesNotThrow(() -> u.interact(1, null), "Interaction with tile of min value state threw exception");
        assertTrue(u.getState() < 4 && u.getState() >= 0, "Interaction didn't return state to allowed range");
    }

    @Test
    void resetState() {
        Tile t = new Tile(MIRROR, 1);
        Tile s = new Tile(MIRROR, 1);

        assertEquals(t, s, "I haven't even done anything yet and you guys can't get along! (Equal tiles not equal)");
        t.resetState();
        assertEquals(t, s, "Tile reset alienated tiles with no prior interaction");
        t.interact(1, Map.of());
        assertNotEquals(t, s, "Interaction did not alienate two intractable tiles");
        t.resetState();
        assertEquals(t, s, "Tile reset state did not make two previously equal tiles equal again");
    }

    @Test
    void hasCollision() {
        assertTrue(Tile.of(STONE, 0).hasCollision(), "Stone should have collision");
        assertFalse(Tile.of(FLOOR, 0).hasCollision(), "Floor shouldn't have collision");

        Tile s = Tile.of(SWITCH_YELLOW, 0);
        assertFalse(s.hasCollision(), "Retracted switch should not have collision");

        s.interact(0, Map.of(Pair.of(0, 0), s));
        assertTrue(s.hasCollision(), "Extended switch should have collision");

        s.interact(0, Map.of(Pair.of(0, 0), s));
        assertFalse(s.hasCollision(), "Extended and retracted switch should have no collision");
    }

    @Test
    void switchInteraction() {
        Tile a = new Tile(SWITCH_CYAN, 0);
        Tile b = new Tile(SWITCH_CYAN, 0);
        Tile c = new Tile(SWITCH_MAGENTA, 0);
        Tile d = new Tile(SWITCH_RED, 0);

        List<Pair<Integer, Integer>> pl = List.of(Pair.of(0, 0), Pair.of(1, 0), Pair.of(2, 0), Pair.of(3, 0));
        Map<Pair<Integer, Integer>, Tile> map = Map.of(pl.get(0), a, pl.get(1), b, pl.get(2), c, pl.get(3), d);

        assertEquals(a, b, "Tiles were instantiated equal but are no longer considered as such");

        a.interact(5, map);

        assertEquals(a, b, "Same coloured switches did not both get updated after interaction");
        assertNotEquals(a, c, "Different coloured switches both got updated");
        assertNotEquals(a, d, "Laser switch got updated from click interaction");
    }

    @Test
    void laserSwitchInteraction() {
        Tile a = new Tile(SWITCH_RED, 0);
        Tile b = new Tile(SWITCH_RED, 0);
        Tile c = new Tile(SWITCH_BLUE, 0);
        Tile d = new Tile(SWITCH_YELLOW, 0);

        List<Pair<Integer, Integer>> pl = List.of(Pair.of(0, 0), Pair.of(1, 0), Pair.of(2, 0), Pair.of(3, 0));
        Map<Pair<Integer, Integer>, Tile> map = Map.of(pl.get(0), a, pl.get(1), b, pl.get(2), c, pl.get(3), d);

        assertEquals(a, b, "Tiles were instantiated equal but are no longer considered as such");

        assertThrows(IllegalArgumentException.class, () -> a.interact(1, map), "Laser switch allowed manual interaction");
        assertDoesNotThrow(() -> a.interact(0, map), "Simulated laser interaction got thrown out");

        assertNotEquals(a, b, "Laser switches should not update simultaneously");
        assertNotEquals(a, c, "Different coloured laser switches both got updated");
        assertNotEquals(a, d, "Clickable switch got updated from laser interaction");
    }

    @Test
    void mirrorInteraction() {
        Tile m = new Tile(MIRROR, 0);
        Tile s = new Tile(MIRROR, 0);

        assertEquals(s, m, "Two Mirrors facing the same way were considered unequal");

        m.interact(0, Map.of());

        assertNotEquals(s, m, "Rotated mirror considered equal to static one");

        for (int i = 0; i < 3; i++)
            m.interact(0, Map.of());

        assertEquals(s, m, "Mirror who did full rotation not considered equal to static one");
    }

    @Test
    void getLaserStep() {
        Tile a = Tile.of(FLOOR, 0);
        Tile b = Tile.of(SWITCH_YELLOW, 0);
        Tile c = Tile.of(STONE, 0);
        Tile d = Tile.of(SWITCH_YELLOW, 1);

        assertEquals(Pair.of(4, 1), a.getLaserStep(Pair.of(3, 1), 1), "Non collision tile did not return next step in given direction");
        assertEquals(Pair.of(4, 4), b.getLaserStep(Pair.of(4, 3), 2), "Retracted switch did not return next step in given direction");

        assertNull(c.getLaserStep(Pair.of(2, 2), 1), "Collision tile did not return null");
        assertNull(d.getLaserStep(Pair.of(4, 3), 2), "Extended switch did not return null");
    }

    @Test
    void laserStepAcrossMirror() {
        Tile m = Tile.of(MIRROR, 1);

        assertEquals(Pair.of(4, 6), m.getLaserStep(Pair.of(4, 5), 1), "Laser step did not yield expected direction for mirror reflection EAST -> SOUTH");
        assertEquals(Pair.of(3, 5), m.getLaserStep(Pair.of(4, 5), 0), "Laser step did not yield expected direction for mirror reflection NORTH -> WEST");
        assertNull(m.getLaserStep(Pair.of(4, 5), 2), "Laser step did not hit against a badly rotated mirror");
        assertNull(m.getLaserStep(Pair.of(4, 5), 3), "Laser step did not hit against a badly rotated mirror");
    }

    @Test
    void laserStepAcrossRedirect() {
        Tile r = Tile.of(REDIRECT, 0);

        assertEquals(Pair.of(4, 3), r.getLaserStep(Pair.of(3, 3), 0), "Redirect unexpected result NORTH");
        assertEquals(Pair.of(3, 4), r.getLaserStep(Pair.of(3, 3), 1), "Redirect unexpected result EAST");
        assertEquals(Pair.of(2, 3), r.getLaserStep(Pair.of(3, 3), 2), "Redirect unexpected result SOUTH");
        assertEquals(Pair.of(3, 2), r.getLaserStep(Pair.of(3, 3), 3), "Redirect unexpected result WEST");
    }

    @Test
    void laserStepAcrossTunnels() {
        Tile t = Tile.of(TUNNELS_RIGHT, 1);

        assertEquals(Pair.of(2, 3), t.getLaserStep(Pair.of(3, 3), 0), "Tunnels Right unexpected result NORTH");
        assertEquals(Pair.of(3, 4), t.getLaserStep(Pair.of(3, 3), 1), "Tunnels Right unexpected result EAST");
        assertEquals(Pair.of(4, 3), t.getLaserStep(Pair.of(3, 3), 2), "Tunnels Right unexpected result SOUTH");
        assertEquals(Pair.of(3, 2), t.getLaserStep(Pair.of(3, 3), 3), "Tunnels Right unexpected result WEST");
    }

    @Test
    void testClone() {
        Tile t = Tile.of(TUNNELS_LEFT, 1);
        t.interact(0, Map.of());

        Tile t2 = t.clone();

        assertEquals(t, t2, "Cloned tile was not considered equal");

        t.resetState();

        assertNotEquals(t, t2, "Tiles were considered equal after reset of interaction");

        t2.resetState();

        assertEquals(t, t2, "Cloned tile was not considered equal after both reset state");
    }

    @Test
    void getNextPosition() {
        assertEquals(Pair.of(1, 0), Tile.getNextPosition(Pair.of(1, 1), 0), "Unexpected next position for NORTH");
        assertEquals(Pair.of(2, 1), Tile.getNextPosition(Pair.of(1, 1), 1), "Unexpected next position for EAST");
        assertEquals(Pair.of(1, 2), Tile.getNextPosition(Pair.of(1, 1), 2), "Unexpected next position for SOUTH");
        assertEquals(Pair.of(0, 1), Tile.getNextPosition(Pair.of(1, 1), 3), "Unexpected next position for WEST");
    }
}