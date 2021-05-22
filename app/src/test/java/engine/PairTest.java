package engine;

import engine.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class PairTest {
    @Test void testInstantiation() {
        Pair<Integer, Integer> intPair = new Pair<>(10, 20);
        assertNotNull(intPair);
        assertEquals(intPair.x(), 10, "");
        assertEquals(intPair.y(), 20, "");


        assertEquals(Pair.of("Hello World", 'a'), new Pair<String, Character>("Hello World", 'a'), "");
    }
}
