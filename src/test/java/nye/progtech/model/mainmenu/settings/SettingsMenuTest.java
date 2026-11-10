package nye.progtech.model.mainmenu.settings;

import nye.progtech.model.mainmenu.MainMenu;
import nye.progtech.model.mainmenu.play.PlayMenu;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsMenuTest {
    @Test
    void changesDifficultyAndShowsItsLevels() throws IOException {
        MainMenu mainMenu = new MainMenu();
        SettingsMenu settingsMenu = mainMenu.openSettingsMenu();

        assertEquals(Difficulty.EASY, settingsMenu.selectedDifficulty());
        assertEquals(6, settingsMenu.levelNames().size());

        SettingsMenu mediumSettings = settingsMenu.nextDifficulty();
        assertEquals(Difficulty.MEDIUM, mediumSettings.selectedDifficulty());
        assertEquals(6, mediumSettings.levelNames().size());
        assertEquals(Difficulty.HARD, mediumSettings.nextDifficulty().selectedDifficulty());
        assertEquals(Difficulty.EASY, mediumSettings.previousDifficulty().selectedDifficulty());

        SettingsMenu alternateControls = settingsMenu.toggleControls();
        assertEquals(Controls.IJKL, alternateControls.settings().controls());
        assertEquals(Difficulty.EASY, alternateControls.selectedDifficulty());

        SettingsMenu twoPlayer = settingsMenu.toggleGameMode();
        assertEquals(GameMode.TWO_PLAYER, twoPlayer.settings().gameMode());
        assertEquals(Difficulty.EASY, twoPlayer.selectedDifficulty());
        assertEquals(GameMode.SINGLE_PLAYER, twoPlayer.toggleGameMode().settings().gameMode());
    }

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
