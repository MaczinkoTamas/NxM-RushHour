package nye.progtech.service;

import nye.progtech.model.mainmenu.settings.Difficulty;
import nye.progtech.model.mainmenu.settings.GameMode;
import nye.progtech.model.mainmenu.settings.GameSettings;
import nye.progtech.model.map.GameSession;
import nye.progtech.model.map.LevelLoader;
import nye.progtech.model.map.RandomAiPlayer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTurnManagerTest {
    @Test
    void singlePlayerModeRunsTheAiAfterASuccessfulHumanMove() throws IOException {
        GameSession session = new GameSession(LevelLoader.loadLevel(1));
        GameTurnManager turns = new GameTurnManager(
                new GameSettings(Difficulty.EASY, GameMode.SINGLE_PLAYER),
                new RandomAiPlayer(new Random(1)));
        assertTrue(session.move("GG", "D4").successful());

        var aiMove = turns.completeHumanTurn(session, true);

        assertTrue(aiMove.isPresent());
        assertTrue(aiMove.orElseThrow().result().successful());
        assertEquals(2, session.steps());
    }

    @Test
    void twoHumanModeAlternatesOnlyAfterASuccessfulMove() throws IOException {
        GameSession session = new GameSession(LevelLoader.loadLevel(1));
        GameTurnManager turns = new GameTurnManager(
                new GameSettings(Difficulty.EASY, GameMode.TWO_PLAYER),
                new RandomAiPlayer(new Random(1)));

        assertEquals("Játékos 1", turns.currentPlayerLabel());
        assertFalse(turns.completeHumanTurn(session, false).isPresent());
        assertEquals("Játékos 1", turns.currentPlayerLabel());
        assertTrue(session.move("GG", "D4").successful());
        assertFalse(turns.completeHumanTurn(session, true).isPresent());
        assertEquals("Játékos 2", turns.currentPlayerLabel());
    }
}
