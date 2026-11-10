package nye.progtech.model.map;

import nye.progtech.model.mainmenu.settings.Difficulty;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LevelTest {
    @Test
    void exposesImmutableLevelData() {
        Level level = LevelTestFixtures.openLevel();

        assertEquals("Nyitott pálya", level.name());
        assertEquals(6, level.boardSize());
        assertEquals(6, level.vehicles().size());
        assertThrows(UnsupportedOperationException.class,
                () -> level.vehicles().clear());
    }

    @Test
    void rejectsInvalidLevelDimensionsAndVehicleCounts() {
        Level valid = LevelTestFixtures.openLevel();

        assertThrows(IllegalArgumentException.class, () -> new Level(
                0, valid.name(), 6, valid.exitSide(), valid.exitCoordinate(),
                valid.difficulty(), valid.vehicles(), valid.obstacles()));
        assertThrows(IllegalArgumentException.class, () -> new Level(
                1, " ", 6, valid.exitSide(), valid.exitCoordinate(),
                valid.difficulty(), valid.vehicles(), valid.obstacles()));
        assertThrows(IllegalArgumentException.class, () -> new Level(
                1, valid.name(), 5, valid.exitSide(), valid.exitCoordinate(),
                valid.difficulty(), valid.vehicles(), valid.obstacles()));
        assertThrows(IllegalArgumentException.class, () -> new Level(
                1, valid.name(), 6, valid.exitSide(), valid.exitCoordinate(),
                valid.difficulty(), valid.vehicles().subList(0, 5), valid.obstacles()));
    }

    @Test
    void rejectsMissingOrMisconfiguredTargetAndOverlappingItems() {
        Level valid = LevelTestFixtures.openLevel();
        List<Vehicle> noTarget = valid.vehicles().stream()
                .map(vehicle -> new Vehicle(vehicle.id(), vehicle.color(), vehicle.row(),
                        vehicle.column(), vehicle.length(), vehicle.orientation(), false))
                .toList();
        assertThrows(IllegalArgumentException.class, () -> levelWith(noTarget, valid.obstacles()));

        List<Vehicle> verticalTarget = new ArrayList<>(valid.vehicles());
        verticalTarget.set(0, new Vehicle("RR", VehicleColor.RED, 2, 0,
                2, Orientation.VERTICAL, true));
        assertThrows(IllegalArgumentException.class,
                () -> levelWith(verticalTarget, valid.obstacles()));

        List<Vehicle> wrongTargetColor = new ArrayList<>(valid.vehicles());
        wrongTargetColor.set(0, new Vehicle("RR", VehicleColor.BLUE, 2, 0,
                2, Orientation.HORIZONTAL, true));
        assertThrows(IllegalArgumentException.class,
                () -> levelWith(wrongTargetColor, valid.obstacles()));

        List<Vehicle> duplicate = new ArrayList<>(valid.vehicles());
        duplicate.set(1, new Vehicle("RR", 0, 0, 2, Orientation.VERTICAL, false));
        assertThrows(IllegalArgumentException.class,
                () -> levelWith(duplicate, valid.obstacles()));

        List<Vehicle> outOfBounds = new ArrayList<>(valid.vehicles());
        outOfBounds.set(1, new Vehicle("BB", 5, 0, 2, Orientation.VERTICAL, false));
        assertThrows(IllegalArgumentException.class,
                () -> levelWith(outOfBounds, valid.obstacles()));
    }

    @Test
    void rejectsMisalignedExitAndObstacleOverlap() {
        Level valid = LevelTestFixtures.openLevel();
        assertThrows(IllegalArgumentException.class, () -> new Level(
                1, valid.name(), 6, ExitSide.LEFT, valid.exitCoordinate(), Difficulty.EASY,
                valid.vehicles(), valid.obstacles()));
        assertThrows(IllegalArgumentException.class, () -> new Level(
                1, valid.name(), 6, ExitSide.RIGHT, GridCoordinate.parse("G3"),
                Difficulty.EASY, valid.vehicles(), valid.obstacles()));
        assertThrows(IllegalArgumentException.class, () -> levelWith(
                valid.vehicles(), new Obstacles(List.of(new Obstacles.Cell(2, 0)))));
        assertThrows(IllegalArgumentException.class, () -> levelWith(
                valid.vehicles(), new Obstacles(List.of(new Obstacles.Cell(6, 0)))));
    }

    private Level levelWith(List<Vehicle> vehicles, Obstacles obstacles) {
        return new Level(1, "Tesztpálya", 6, ExitSide.RIGHT,
                GridCoordinate.parse("G4"), Difficulty.EASY, vehicles, obstacles);
    }
}
