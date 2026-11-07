package org.example.model.highscore;

import org.example.model.mainmenu.settings.Difficulty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighScoreStoreTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void storesTheBestScoreSeparatelyForEachDifficulty() throws IOException {
        HighScoreStore store = new HighScoreStore(temporaryDirectory.resolve("highscores.xml"));

        assertTrue(store.recordIfBest(new HighScore(Difficulty.EASY, "Anna", 4_500)));
        assertFalse(store.recordIfBest(new HighScore(Difficulty.EASY, "Béla", 4_000)));
        assertTrue(store.recordIfBest(new HighScore(Difficulty.EASY, "Csilla", 5_000)));
        assertTrue(store.recordIfBest(new HighScore(Difficulty.HARD, "Dénes", 2_500)));

        assertEquals(new HighScore(Difficulty.EASY, "Csilla", 5_000),
                store.ranking(Difficulty.EASY).getFirst());
        assertEquals(new HighScore(Difficulty.HARD, "Dénes", 2_500),
                store.ranking(Difficulty.HARD).getFirst());
        assertEquals(2, store.loadAll().size());
    }

    @Test
    void reportsMalformedHighScoreFile() throws IOException {
        Path file = temporaryDirectory.resolve("highscores.xml");
        Files.writeString(file, "<wrong/>");
        HighScoreStore store = new HighScoreStore(file);

        assertThrows(IOException.class, store::loadAll);
    }
}
