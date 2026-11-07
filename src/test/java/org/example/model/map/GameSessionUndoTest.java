package org.example.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionUndoTest {
    @Test
    void undoesRequestedMovesAndRejectsInvalidCounts() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());
        session.move("RR", Direction.RIGHT, 2);

        assertTrue(session.canUndo());
        assertEquals(2, session.undoableSteps());
        session.undo();
        assertEquals(1, session.steps());
        session.undo(1);
        assertFalse(session.canUndo());
        assertEquals(0, session.steps());
        assertThrows(IllegalArgumentException.class, () -> session.undo(0));
        assertThrows(IllegalStateException.class, () -> session.undo(1));
    }

    @Test
    void undoingWinningMoveRestoresActiveUnwonState() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());
        session.move("RR", Direction.RIGHT, 5);
        assertTrue(session.isWon());

        session.undo(5);

        assertFalse(session.isWon());
        assertTrue(session.isActive());
        assertEquals(0, session.steps());
    }
}
