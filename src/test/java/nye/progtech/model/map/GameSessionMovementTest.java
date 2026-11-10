package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionMovementTest {
    @Test
    void keepsEveryValidStepWhenRequestedMoveReachesBoardEdge() throws Exception {
        GameSession session = new GameSession(LevelLoader.loadLevel(1));

        MoveResult result = session.move("BB", Direction.DOWN, 5);

        assertTrue(result.successful());
        assertTrue(result.message().contains("pálya szélén"));
        assertEquals(3, result.movedSteps());
        assertEquals(3, session.steps());
        assertEquals(4, session.vehicles().get(1).row());
    }

    @Test
    void rejectsInvalidMovementInputsAndCoordinates() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());

        assertFalse(session.move("XX", Direction.RIGHT, 1).successful());
        assertFalse(session.move("RR", (Direction) null, 1).successful());
        assertFalse(session.move("RR", Direction.RIGHT, 0).successful());
        assertFalse(session.move("BB", Direction.RIGHT, 1).successful());
        assertFalse(session.move("RR", "unknown", 1).successful());
        assertFalse(session.move("RR", (String) null, 1).successful());
        assertFalse(session.moveToCoordinate("RR", "invalid").successful());
        assertFalse(session.moveToCoordinate("RR", "A8").successful());
        assertFalse(session.moveToCoordinate("RR", "C3").successful());
        assertThrows(IllegalArgumentException.class, () -> session.move("RR", 3, 3));
        assertThrows(IllegalArgumentException.class, () -> session.move("XX", 0, 0));
    }

    @Test
    void movesVehiclesByTheirCoordinates() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());

        assertTrue(session.moveToCoordinate("RR", "B4").successful());
        assertEquals(1, session.vehicles().getFirst().column());
        assertTrue(session.moveToCoordinate("OO", "D2").successful());
        assertEquals(3, session.vehicles().get(4).column());
    }
}
