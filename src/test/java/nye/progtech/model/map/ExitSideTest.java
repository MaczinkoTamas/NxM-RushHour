package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExitSideTest {
    // Az enum a négy égtáji kijáratot tartalmazza, még ha a pályák jelenleg csak oldalsó kijáratot használnak.
    @Test
    void providesAllFourExitSides() {
        assertEquals(4, ExitSide.values().length);
        assertEquals(ExitSide.RIGHT, ExitSide.valueOf("RIGHT"));
        assertEquals(ExitSide.LEFT, ExitSide.valueOf("LEFT"));
        assertEquals(ExitSide.TOP, ExitSide.valueOf("TOP"));
        assertEquals(ExitSide.BOTTOM, ExitSide.valueOf("BOTTOM"));
    }
}
