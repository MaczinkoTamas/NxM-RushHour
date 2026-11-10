package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    void completesLevelThroughLeftExit() {
        Level fixture = LevelTestFixtures.openLevel();
        List<Vehicle> vehicles = new ArrayList<>(fixture.vehicles());
        vehicles.set(0, new Vehicle("RR", 2, 2, 2, Orientation.HORIZONTAL, true));
        Level leftExitLevel = new Level(fixture.id(), fixture.name(), fixture.boardSize(),
                ExitSide.LEFT, GridCoordinate.parse("A4"), fixture.difficulty(),
                vehicles, fixture.obstacles());
        GameSession session = new GameSession(leftExitLevel);

        assertTrue(session.move("RR", Direction.LEFT, 2).successful());
        assertTrue(session.isWon());
    }
}
