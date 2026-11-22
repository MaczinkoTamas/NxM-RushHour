package nye.progtech.model.mainmenu.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSettingsTest {
    // A with... metódusok új beállításokat adnak vissza, a többi értéket változatlanul hagyva.
    @Test
    void defaultsToEasySinglePlayerAndAllowsImmutableChanges() {
        GameSettings defaults = new GameSettings();

        assertEquals(new GameSettings(Difficulty.EASY, GameMode.SINGLE_PLAYER),
                defaults);
        assertTrue(defaults.botEnabled());

        GameSettings hard = defaults.withDifficulty(Difficulty.HARD);
        assertEquals(new GameSettings(Difficulty.HARD, GameMode.SINGLE_PLAYER),
                hard);

        GameSettings twoPlayer = new GameSettings(Difficulty.MEDIUM, GameMode.TWO_PLAYER);
        assertFalse(twoPlayer.botEnabled());
        GameSettings changedMode = defaults.withGameMode(GameMode.TWO_PLAYER);
        assertEquals(new GameSettings(Difficulty.EASY, GameMode.TWO_PLAYER),
                changedMode);
    }

    // Mindhárom beállítás kötelező, ezért egyik sem lehet null.
    @Test
    void rejectsNullSettings() {
        assertThrows(NullPointerException.class,
                () -> new GameSettings(null, GameMode.SINGLE_PLAYER));
        assertThrows(NullPointerException.class,
                () -> new GameSettings(Difficulty.EASY, null));
    }
}
