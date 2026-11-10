package nye.progtech.model.mainmenu.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSettingsTest {
    @Test
    void defaultsToEasySinglePlayerAndAllowsImmutableChanges() {
        GameSettings defaults = new GameSettings();

        assertEquals(Difficulty.EASY, defaults.difficulty());
        assertEquals(GameMode.SINGLE_PLAYER, defaults.gameMode());
        assertEquals(Controls.WASD, defaults.controls());
        assertTrue(defaults.botEnabled());

        GameSettings hard = defaults.withDifficulty(Difficulty.HARD);
        assertEquals(Difficulty.HARD, hard.difficulty());
        assertEquals(defaults.gameMode(), hard.gameMode());

        GameSettings alternateControls = defaults.withControls(Controls.IJKL);
        assertEquals(Controls.IJKL, alternateControls.controls());
        assertEquals(defaults.difficulty(), alternateControls.difficulty());

        GameSettings twoPlayer = new GameSettings(Difficulty.MEDIUM, GameMode.TWO_PLAYER);
        assertFalse(twoPlayer.botEnabled());
        GameSettings changedMode = defaults.withGameMode(GameMode.TWO_PLAYER);
        assertEquals(GameMode.TWO_PLAYER, changedMode.gameMode());
        assertEquals(defaults.controls(), changedMode.controls());
        assertEquals(defaults.difficulty(), changedMode.difficulty());
    }

    @Test
    void rejectsNullSettings() {
        assertThrows(NullPointerException.class,
                () -> new GameSettings(null, GameMode.SINGLE_PLAYER));
        assertThrows(NullPointerException.class,
                () -> new GameSettings(Difficulty.EASY, null));
        assertThrows(NullPointerException.class,
                () -> new GameSettings(Difficulty.EASY, GameMode.SINGLE_PLAYER, null));
    }
}
