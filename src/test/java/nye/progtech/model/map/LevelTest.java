package nye.progtech.model.map;

import nye.progtech.model.mainmenu.settings.Difficulty;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LevelTest {
    // A pálya külső kód által visszakapott járműlistája nem módosítható.
    @Test
    void exposesImmutableLevelData() {
        Level level = LevelTestFixtures.openLevel();

        assertEquals("Nyitott pálya", level.name());
        assertEquals(6, level.rowCount());
        assertEquals(6, level.columnCount());
        assertEquals(6, level.vehicles().size());
        assertThrows(UnsupportedOperationException.class,
                () -> level.vehicles().clear());
    }

    @Test
    void allowsRectangleDimensionsThatAreIndependentOfDifficulty() {
        List<Vehicle> vehicles = List.of(
                new Vehicle("RR", 2, 0, 2, Orientation.HORIZONTAL, true),
                new Vehicle("BB", 3, 10, 2, Orientation.HORIZONTAL, TargetOwner.AI),
                new Vehicle("GG", 0, 4, 2, Orientation.VERTICAL, false),
                new Vehicle("YY", 0, 6, 2, Orientation.VERTICAL, false),
                new Vehicle("OO", 4, 2, 2, Orientation.HORIZONTAL, false),
                new Vehicle("PP", 5, 8, 2, Orientation.HORIZONTAL, false));

        Level level = new Level(2, "Téglalap", 6, 12, ExitSide.RIGHT,
                GridCoordinate.parse("M4"), Difficulty.EASY, vehicles,
                new Obstacles(List.of()));

        assertEquals(6, level.rowCount());
        assertEquals(12, level.columnCount());
    }

    // Az azonosítónak, névnek, méretnek és járműszámnak illeszkednie kell a nehézséghez.
    @Test
    void rejectsInvalidLevelDimensionsAndVehicleCounts() {
        Level valid = LevelTestFixtures.openLevel();

        assertThrows(IllegalArgumentException.class, () -> new Level(
                0, valid.name(), 6, 6, valid.exitSide(), valid.exitCoordinate(),
                valid.difficulty(), valid.vehicles(), valid.obstacles()));
        assertThrows(IllegalArgumentException.class, () -> new Level(
                1, " ", 6, 6, valid.exitSide(), valid.exitCoordinate(),
                valid.difficulty(), valid.vehicles(), valid.obstacles()));
        assertThrows(IllegalArgumentException.class, () -> new Level(
                1, valid.name(), 0, 6, valid.exitSide(), valid.exitCoordinate(),
                valid.difficulty(), valid.vehicles(), valid.obstacles()));
        assertThrows(IllegalArgumentException.class, () -> new Level(
                1, valid.name(), 6, 6, valid.exitSide(), valid.exitCoordinate(),
                valid.difficulty(), valid.vehicles().subList(0, 5), valid.obstacles()));
    }

    // A piros RR az emberé, a másik vízszintes cél az AI-é; a járművek nem fedhetik egymást.
    @Test
    void rejectsMissingOrMisconfiguredTargetAndOverlappingItems() {
        Level valid = LevelTestFixtures.openLevel();
        List<Vehicle> noTarget = valid.vehicles().stream()
                .map(vehicle -> new Vehicle(vehicle.id(), vehicle.color(), vehicle.row(),
                        vehicle.column(), vehicle.length(), vehicle.orientation(), false))
                .toList();
        assertThrows(IllegalArgumentException.class, () -> levelWith(noTarget, valid.obstacles()));

        // A céljármű csak vízszintes lehet.
        List<Vehicle> verticalTarget = new ArrayList<>(valid.vehicles());
        verticalTarget.set(0, new Vehicle("RR", VehicleColor.RED, 2, 0,
                2, Orientation.VERTICAL, true));
        assertThrows(IllegalArgumentException.class,
                () -> levelWith(verticalTarget, valid.obstacles()));

        // A céljármű azonosítója és színe a játék szabályai szerint rögzített.
        List<Vehicle> wrongTargetColor = new ArrayList<>(valid.vehicles());
        wrongTargetColor.set(0, new Vehicle("RR", VehicleColor.BLUE, 2, 0,
                2, Orientation.HORIZONTAL, true));
        assertThrows(IllegalArgumentException.class,
                () -> levelWith(wrongTargetColor, valid.obstacles()));

        // Járműazonosító nem ismétlődhet, céljárműtől függetlenül.
        List<Vehicle> duplicate = new ArrayList<>(valid.vehicles());
        duplicate.set(1, new Vehicle("RR", 0, 0, 2, Orientation.VERTICAL, false));
        assertThrows(IllegalArgumentException.class,
                () -> levelWith(duplicate, valid.obstacles()));

        // A jármű teljes hossza a táblán belül kell maradjon.
        List<Vehicle> outOfBounds = new ArrayList<>(valid.vehicles());
        outOfBounds.set(1, new Vehicle("BB", 5, 0, 2, Orientation.VERTICAL, false));
        assertThrows(IllegalArgumentException.class,
                () -> levelWith(outOfBounds, valid.obstacles()));
    }

    // A kijárat a céljármű sorában, a megfelelő szélen legyen; akadály nem fedhet más elemet.
    @Test
    void rejectsMisalignedExitAndObstacleOverlap() {
        Level valid = LevelTestFixtures.openLevel();
        assertThrows(IllegalArgumentException.class, () -> new Level(
                1, valid.name(), 6, 6, ExitSide.LEFT, valid.exitCoordinate(), Difficulty.EASY,
                valid.vehicles(), valid.obstacles()));
        assertThrows(IllegalArgumentException.class, () -> new Level(
                1, valid.name(), 6, 6, ExitSide.RIGHT, GridCoordinate.parse("G3"),
                Difficulty.EASY, valid.vehicles(), valid.obstacles()));
        assertThrows(IllegalArgumentException.class, () -> levelWith(
                valid.vehicles(), new Obstacles(List.of(new Obstacles.Cell(2, 0)))));
        assertThrows(IllegalArgumentException.class, () -> levelWith(
                valid.vehicles(), new Obstacles(List.of(new Obstacles.Cell(6, 0)))));
    }

    private Level levelWith(List<Vehicle> vehicles, Obstacles obstacles) {
        return new Level(1, "Tesztpálya", 6, 6, ExitSide.RIGHT,
                GridCoordinate.parse("G4"), Difficulty.EASY, vehicles, obstacles);
    }
}
