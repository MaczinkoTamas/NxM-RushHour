package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionMovementTest {
    // Ha a teljes kérés nem fér el, egyetlen részlépés sem maradhat érvényben.
    @Test
    void rejectsWholeMoveWhenRequestedDistanceReachesBoardEdge() throws Exception {
        GameSession session = new GameSession(LevelLoader.loadLevel(1));
        int initialRow = session.vehicles().stream()
                .filter(vehicle -> vehicle.id().equals("BB"))
                .findFirst()
                .orElseThrow()
                .row();

        MoveResult result = session.moveSteps("BB", Direction.DOWN, 5);

        assertFalse(result.successful());
        assertTrue(result.message().contains("pálya szélén"));
        assertEquals(0, result.movedSteps());
        assertEquals(0, session.steps());
        assertEquals(initialRow, session.vehicles().stream()
                .filter(vehicle -> vehicle.id().equals("BB"))
                .findFirst()
                .orElseThrow()
                .row());
    }

    // Hibás azonosító, irány, távolság vagy koordináta nem eredményezhet sikeres lépést.
    @Test
    void rejectsInvalidMovementInputsAndCoordinates() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());

        assertFalse(session.moveSteps("XX", Direction.RIGHT, 1).successful());
        assertFalse(session.move("RR", (String) null).successful());
        assertFalse(session.move("RR", "invalid").successful());
        assertFalse(session.move("RR", "A8").successful());
        assertFalse(session.move("RR", "C3").successful());
    }

    // A megadott koordináta a jármű elejének célpozícióját jelöli.
    @Test
    void movesVehiclesByTheirCoordinates() {
        GameSession session = new GameSession(LevelTestFixtures.openLevel());

        assertTrue(session.move("RR", "B4").successful());
        assertEquals(1, session.vehicles().getFirst().column());
        assertTrue(session.move("OO", "D2").successful());
        assertEquals(3, session.vehicles().get(4).column());
    }

    // Given egy 6x12-es pálya, When a játékos a megadott koordinátára mozgat, Then a szélesség
    // alapján számított kijáratig jut, majd mindkét cél kijuttatásával teljesíti a pályát.
    @Test
    void playsACompleteGameOnARectangularBoard() {
        Level level = new Level(99, "Téglalap", 6, 12, ExitSide.RIGHT,
                GridCoordinate.parse("M4"), nye.progtech.model.mainmenu.settings.Difficulty.EASY,
                java.util.List.of(
                        new Vehicle("RR", 2, 0, 2, Orientation.HORIZONTAL, true),
                        new Vehicle("BB", 3, 10, 2, Orientation.HORIZONTAL, TargetOwner.AI),
                        new Vehicle("GG", 0, 4, 2, Orientation.VERTICAL, false),
                        new Vehicle("YY", 0, 6, 2, Orientation.VERTICAL, false),
                        new Vehicle("OO", 4, 2, 2, Orientation.HORIZONTAL, false),
                        new Vehicle("PP", 5, 8, 2, Orientation.HORIZONTAL, false)),
                new Obstacles(java.util.List.of()));
        GameSession session = new GameSession(level);

        assertTrue(session.move("RR", "M4").successful());
        assertTrue(session.targetReached(TargetOwner.HUMAN));
        assertEquals(11, session.steps());
        assertTrue(session.isWon());
        assertEquals(TargetOwner.HUMAN, session.winner());
    }
}
