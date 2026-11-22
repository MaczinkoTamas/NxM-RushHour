package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrientationTest {
    // A járművek csak vízszintesen vagy függőlegesen állhatnak a rácson.
    @Test
    void providesHorizontalAndVerticalOrientations() {
        assertEquals(2, Orientation.values().length);
        assertEquals(Orientation.HORIZONTAL, Orientation.valueOf("HORIZONTAL"));
        assertEquals(Orientation.VERTICAL, Orientation.valueOf("VERTICAL"));
    }
}
