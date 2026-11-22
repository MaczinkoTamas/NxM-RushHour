package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionExitTest {
    // Az emberi cél kijutása azonnal emberi győzelmet jelent.
    @Test
    void humanWinsWhenItsTargetReachesItsExit() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());

        assertTrue(session.moveSteps("RR", Direction.RIGHT, 5).successful());
        assertTrue(session.targetReached(TargetOwner.HUMAN));
        assertTrue(session.isWon());
        assertEquals(TargetOwner.HUMAN, session.winner());
        assertFalse(session.moveSteps("RR", Direction.RIGHT, 1).successful());
        assertFalse(session.moveSteps("BB", Direction.LEFT, 4).successful());
    }

    // Az AI saját céljának kijuttatása AI-győzelmet eredményez.
    @Test
    void aiWinsWhenItsTargetReachesItsOwnExit() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());

        assertTrue(session.moveSteps("BB", Direction.LEFT, 4).successful());
        assertTrue(session.targetReached(TargetOwner.AI));
        assertTrue(session.isWon());
        assertEquals(TargetOwner.AI, session.winner());
        assertFalse(session.moveSteps("RR", Direction.RIGHT, 5).successful());
    }

    // A pálya elhagyása lezárja a munkamenetet, és elutasítja a további lépéseket.
    @Test
    void leavingLevelClosesSession() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());

        session.leaveLevel();

        assertFalse(session.isActive());
        assertFalse(session.moveSteps("RR", Direction.RIGHT, 1).successful());
    }

    // A bal oldali emberi kijárat mellett az AI a szemközti, jobb oldali kijáratot használja.
    @Test
    void assignsOppositeExitsWhenHumanExitIsOnTheLeft() {
        Level fixture = LevelTestFixtures.openLevel();
        List<Vehicle> vehicles = new ArrayList<>(fixture.vehicles());
        vehicles.set(0, new Vehicle("RR", 2, 2, 2, Orientation.HORIZONTAL, true));
        // A kijárat az A4 koordinátán van; a jármű innen két mező balra jut ki.
        Level leftExitLevel = new Level(fixture.id(), fixture.name(), fixture.rowCount(), fixture.columnCount(),
                ExitSide.LEFT, GridCoordinate.parse("A4"), fixture.difficulty(),
                vehicles, fixture.obstacles());
        GameSession session = new GameSession(leftExitLevel);

        assertTrue(session.moveSteps("RR", Direction.LEFT, 2).successful());
        assertTrue(session.targetReached(TargetOwner.HUMAN));
        assertTrue(session.isWon());
        assertEquals(TargetOwner.HUMAN, session.winner());
    }
}
