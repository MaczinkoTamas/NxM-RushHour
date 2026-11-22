package nye.progtech.model.mainmenu.settings;

import nye.progtech.ui.MainMenu;
import nye.progtech.ui.PlayMenu;
import nye.progtech.ui.SettingsMenu;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsMenuTest {
    // A menü kezeli a nehézségváltást; a vezérlés- és játékmódváltás a kiválasztott szintet megtartja.
    @Test
    void changesDifficultyAndShowsItsLevels() throws IOException {
        MainMenu mainMenu = new MainMenu();
        SettingsMenu settingsMenu = mainMenu.openSettingsMenu();

        assertEquals(Difficulty.EASY, settingsMenu.selectedDifficulty());
        assertEquals(Difficulty.LEVELS_PER_DIFFICULTY, settingsMenu.levelNames().size());

        SettingsMenu mediumSettings = settingsMenu.nextDifficulty();
        assertEquals(Difficulty.MEDIUM, mediumSettings.selectedDifficulty());
        assertEquals(Difficulty.LEVELS_PER_DIFFICULTY, mediumSettings.levelNames().size());
        assertEquals(Difficulty.HARD, mediumSettings.nextDifficulty().selectedDifficulty());
        assertEquals(Difficulty.EASY, mediumSettings.previousDifficulty().selectedDifficulty());

        SettingsMenu twoPlayer = settingsMenu.toggleGameMode();
        assertEquals(GameMode.TWO_PLAYER, twoPlayer.settings().gameMode());
        assertEquals(Difficulty.EASY, twoPlayer.selectedDifficulty());
        assertEquals(GameMode.SINGLE_PLAYER, twoPlayer.toggleGameMode().settings().gameMode());
    }

    // A főmenübe mentett nehézségnek a pályaválasztó megnyitásakor is érvényesülnie kell.
    @Test
    void appliedDifficultyIsUsedWhenOpeningPlayMenu() throws IOException {
        MainMenu mainMenu = new MainMenu();
        SettingsMenu updatedSettings = mainMenu.openSettingsMenu()
                .selectDifficulty(Difficulty.HARD);
        MainMenu updatedMainMenu = mainMenu.withSettings(updatedSettings);

        PlayMenu playMenu = updatedMainMenu.openPlayMenu();

        assertEquals(Difficulty.HARD, playMenu.selectedDifficulty());
        assertEquals(updatedSettings.levelNames(), playMenu.levelNames());
    }
}
