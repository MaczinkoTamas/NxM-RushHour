package nye.progtech.model.mainmenu.play;

import nye.progtech.ui.MainMenu;
import nye.progtech.ui.PlayMenu;
import nye.progtech.model.mainmenu.settings.Difficulty;
import nye.progtech.model.map.Level;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlayMenuTest {
    // Alapból a könnyű kategória aktív; a pályalista azon belül körbefordul.
    @Test
    void showsLevelsForSelectedDifficultyAndNavigatesWithinIt() throws IOException {
        PlayMenu menu = new MainMenu().openPlayMenu();

        assertEquals(3, menu.difficultyNames().size());
        assertEquals(Difficulty.EASY, menu.selectedDifficulty());
        assertEquals(6, menu.levelNames().size());
        assertEquals("Nyitott garázs", menu.selectedLevelName());
        assertEquals("Kettős torlasz", menu.nextLevel().selectedLevelName());
        assertEquals(menu.levelNames().get(5), menu.previousLevel().selectedLevelName());
    }

    // Nehézségváltáskor másik kategória pályái jelennek meg, idegen pályaazonosító nem választható.
    @Test
    void switchesDifficultyAndShowsOnlyItsLevels() throws IOException {
        PlayMenu menu = PlayMenu.load();

        PlayMenu mediumMenu = menu.nextDifficulty();
        assertEquals(Difficulty.MEDIUM, mediumMenu.selectedDifficulty());
        assertEquals(Difficulty.LEVELS_PER_DIFFICULTY, mediumMenu.levelNames().size());
        assertEquals(Difficulty.HARD, mediumMenu.nextDifficulty().selectedDifficulty());
        assertEquals(Difficulty.EASY, mediumMenu.previousDifficulty().selectedDifficulty());

        int mediumLevelId = mediumMenu.difficultyLevels().getFirst().id();
        Level selected = mediumMenu.selectLevel(mediumLevelId).selectedLevel();
        assertEquals(mediumLevelId, selected.id());
        assertThrows(IllegalArgumentException.class, () -> mediumMenu.selectLevel(1));
    }
}
