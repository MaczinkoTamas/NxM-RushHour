package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionUndoTest {
    // A visszavonás mezőlépésenként működik, és csak létező előzményt lehet visszavenni.
    @Test
    void undoesRequestedMovesAndRejectsInvalidCounts() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());
        session.moveSteps("RR", Direction.RIGHT, 2);

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

    // A győzelmet okozó lépések visszavonása a pályát újra aktívvá és megoldatlanná teszi.
    @Test
    void undoingWinningMoveRestoresActiveUnwonState() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());
        session.moveSteps("RR", Direction.RIGHT, 5);
        assertTrue(session.isWon());
        assertEquals(TargetOwner.HUMAN, session.winner());

        session.undo(5);

        assertFalse(session.isWon());
        assertTrue(session.isActive());
        assertEquals(0, session.steps());
    }
}
