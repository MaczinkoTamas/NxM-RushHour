package nye.progtech.model.highscore;

import nye.progtech.model.mainmenu.settings.Difficulty;
import nye.progtech.model.map.LevelLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DifficultyProgressTest {
    // A kategória csak a hat különböző pálya után zárul le, az eredmény után a számláló újraindul.
    @Test
    void countsSixDistinctCompletedLevelsAndCalculatesTheirCombinedScore() throws IOException {
        DifficultyProgress progress = new DifficultyProgress();
        var easyLevels = LevelLoader.loadLevels().stream()
                .filter(level -> level.difficulty() == Difficulty.EASY)
                .toList();

        for (int index = 0; index < easyLevels.size() - 1; index++) {
            assertTrue(progress.complete(easyLevels.get(index), 1).isEmpty());
        }
        assertEquals(5, progress.completedCount(Difficulty.EASY));

        var completedRun = progress.complete(easyLevels.getLast(), 1);

        assertTrue(completedRun.isPresent());
        assertEquals(Difficulty.EASY, completedRun.get().difficulty());
        assertEquals(5_940, completedRun.get().score());
        assertEquals(0, progress.completedCount(Difficulty.EASY));
    }

    // Újrajátszáskor a jobb pontszám marad meg, a nehézségek haladása pedig külön fut.
    @Test
    void repeatedLevelDoesNotCountTwiceAndCategoriesProgressIndependently()
            throws IOException {
        DifficultyProgress progress = new DifficultyProgress();
        var levels = LevelLoader.loadLevels();
        var easy = levels.stream().filter(level -> level.difficulty() == Difficulty.EASY)
                .toList();
        var medium = levels.stream().filter(level -> level.difficulty() == Difficulty.MEDIUM)
                .toList();

        progress.complete(easy.getFirst(), 5);
        progress.complete(easy.getFirst(), 2);
        progress.complete(medium.getFirst(), 1);

        assertEquals(1, progress.completedCount(Difficulty.EASY));
        assertEquals(1, progress.completedCount(Difficulty.MEDIUM));

        // A második teljesítés 980 pontja váltja az első 950 pontját.
        Optional<DifficultyProgress.CompletedRun> run = Optional.empty();
        for (int index = 1; index < easy.size(); index++) {
            run = progress.complete(easy.get(index), 1);
        }
        assertTrue(run.isPresent());
        assertEquals(5_930, run.get().score());
        assertTrue(progress.complete(medium.get(1), 1).isEmpty());
    }
}
