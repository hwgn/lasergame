package engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PairTest {
    @Test
    void testInstantiation() {
        Pair<Integer, Integer> intPair = new Pair<>(10, 20);

        assertNotNull(intPair);
        assertEquals(intPair.x(), 10, "Value specified does not carry over to instance variable x");
        assertEquals(intPair.y(), 20, "Value specified does not carry over to instance variable y");

    }

    @Test
    void of() {
        assertEquals(new Pair<>(1, 2), Pair.of(1, 2), "of method generated different instance compared to constructor call");
        assertEquals(new Pair<>(Integer.MAX_VALUE, Integer.MIN_VALUE), Pair.of(Integer.MAX_VALUE, Integer.MIN_VALUE), "of method generated different instance compared to constructor call when called with extreme values");
    }

    /**
     * Asserts that x and y values are properly distinguished and do not get mixed up.
     */
    @Test
    void testConsistency() {
        assertNotEquals(new Pair<>(10, 20), new Pair<>(20, 10), "x and y integers weren't distinguished from");
        assertNotEquals(new Pair<>("Hello", "World"), new Pair<>("World", "Hello"), "x and y strings weren't distinguished from");

        Pair<Integer, Integer> a = new Pair<>(1, 2);
        Pair<Integer, Integer> b = new Pair<>(1, 3);
        Pair<Integer, Integer> c = new Pair<>(1, 2);

        assertEquals(a, c, "Two instances with the same int values were not considered equal");
        assertNotEquals(a, b, "Two instances with differing int values were considered equal");
    }

    @Test
    void equals() {
        assertEquals(Pair.of(Laser.Color.BLUE, 2), Pair.of(Laser.Color.BLUE, 2), "equals couldn't handle enum instance comparison");
        assertNotEquals(Pair.of(Laser.Color.RED, 2), Pair.of(Laser.Color.BLUE, 2), "equals identified different enum instances to be the same");

        assertEquals(Pair.of(Pair.of(Pair.of(1, 2), 3), 4), Pair.of(Pair.of(Pair.of(1, 2), 3), 4), "equals method couldn't handle layered pairs");
        assertEquals(Pair.of(null, null), Pair.of(null, null), "pairs with null values not identified to be equal");
    }
}
