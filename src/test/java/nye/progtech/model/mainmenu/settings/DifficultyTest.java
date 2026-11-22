package nye.progtech.model.mainmenu.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DifficultyTest {
    // A nehézség a járműszámot és a megjelenített nevet határozza meg, nem a tábla méretét.
    @Test
    void definesVehicleCountsAndDisplayNames() {
        assertEquals(6, Difficulty.EASY.vehicleCount());
        assertEquals("Könnyű", Difficulty.EASY.displayName());
        assertEquals(14, Difficulty.MEDIUM.vehicleCount());
        assertEquals("Közepes", Difficulty.MEDIUM.displayName());
        assertEquals(18, Difficulty.HARD.vehicleCount());
        assertEquals("Nehéz", Difficulty.HARD.displayName());
        assertEquals(6, Difficulty.LEVELS_PER_DIFFICULTY);
    }
}
