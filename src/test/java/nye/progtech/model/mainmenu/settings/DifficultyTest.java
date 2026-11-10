package nye.progtech.model.mainmenu.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DifficultyTest {
    @Test
    void definesBoardSizesVehicleCountsAndDisplayNames() {
        assertEquals(6, Difficulty.EASY.boardSize());
        assertEquals(6, Difficulty.EASY.vehicleCount());
        assertEquals("Könnyű", Difficulty.EASY.displayName());
        assertEquals(8, Difficulty.MEDIUM.boardSize());
        assertEquals(14, Difficulty.MEDIUM.vehicleCount());
        assertEquals("Közepes", Difficulty.MEDIUM.displayName());
        assertEquals(10, Difficulty.HARD.boardSize());
        assertEquals(18, Difficulty.HARD.vehicleCount());
        assertEquals("Nehéz", Difficulty.HARD.displayName());
        assertEquals(6, Difficulty.LEVELS_PER_DIFFICULTY);
    }
}
