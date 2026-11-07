package org.example.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionExitTest {
    @Test
    void completesLevelAtExitAndRejectsFurtherMoves() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());

        assertTrue(session.move("RR", Direction.RIGHT, 5).successful());
        assertTrue(session.isWon());
        assertFalse(session.move("RR", Direction.RIGHT, 1).successful());
    }

    @Test
    void leavingLevelClosesSession() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());

        session.leaveLevel();

        assertFalse(session.isActive());
        assertFalse(session.move("RR", Direction.RIGHT, 1).successful());
    }
}
