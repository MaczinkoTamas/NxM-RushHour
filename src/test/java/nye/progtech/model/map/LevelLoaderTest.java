package nye.progtech.model.map;

import nye.progtech.model.mainmenu.settings.Difficulty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelLoaderTest {
    @TempDir
    Path temporaryDirectory;

    // A betöltött pályák kategóriánként teljesek, a céljármű és a kijárat adatai érvényesek.
    @Test
    void loadsAllLevelsGroupedByDifficultyWithValidTargetsAndExits() throws IOException {
        List<Level> levels = LevelLoader.loadLevels();

        assertEquals(18, levels.size());
        Map<Difficulty, Long> levelsPerDifficulty = levels.stream()
                .collect(Collectors.groupingBy(Level::difficulty, Collectors.counting()));
        for (Difficulty difficulty : Difficulty.values()) {
            assertEquals(6, levelsPerDifficulty.get(difficulty));
        }
        for (Level level : levels) {
            Vehicle target = level.vehicles().stream()
                    .filter(Vehicle::target)
                    .findFirst()
                    .orElseThrow();
            assertEquals("RR", target.id());
            assertEquals(VehicleColor.RED, target.color());
            assertEquals(TargetOwner.HUMAN, target.targetOwner());
            assertEquals(TargetOwner.AI,
                    level.targetFor(TargetOwner.AI).targetOwner());
            assertEquals(level.exitSide().oppositeHorizontalSide(),
                    level.targetGoal(TargetOwner.AI).exitSide());
            assertEquals(level.targetFor(TargetOwner.AI).row(),
                    level.targetGoal(TargetOwner.AI).exitCoordinate()
                            .rowIndex(level.rowCount()));
            assertEquals(2, level.targetGoals().size());
            assertTrue(level.rowCount() > 0);
            assertTrue(level.columnCount() > 0);
            int exitColumn = level.exitSide() == ExitSide.LEFT ? 0 : level.columnCount();
            assertEquals(exitColumn, level.exitCoordinate().columnIndex());
            assertEquals(new GridCoordinate(exitColumn, level.exitCoordinate().rank()).toString(),
                    level.exitCoordinate().toString());
            assertEquals(level.difficulty().vehicleCount(), level.vehicles().size());
            assertTrue(level.vehicles().stream().allMatch(vehicle -> vehicle.length() >= 2));
        }
    }

    // A nehéz pályák nagyobbak és zsúfoltabbak, kijáratuk váltakozva jobb vagy bal oldali.
    @Test
    void hardLevelsAreLargerDenserAndAlternateTheirExitSide() throws IOException {
        List<Level> hardLevels = LevelLoader.loadLevels().stream()
                .filter(level -> level.difficulty() == Difficulty.HARD)
                .toList();

        assertEquals(6, hardLevels.size());
        for (int index = 0; index < hardLevels.size(); index++) {
            Level level = hardLevels.get(index);
            assertEquals(10, level.rowCount());
            assertEquals(18, level.vehicles().size());
            assertEquals(10, level.obstacles().cells().size());
            assertEquals(index % 2 == 0 ? ExitSide.RIGHT : ExitSide.LEFT, level.exitSide());
            assertTrue(level.vehicles().stream()
                    .filter(Vehicle::target)
                    .anyMatch(vehicle -> vehicle.column() != 0
                            && level.vehicles().stream()
                                    .filter(candidate -> !candidate.target())
                                    .anyMatch(candidate -> blocksTargetLane(level, candidate))));
        }
        // Minden nehéz pályán máshonnan indul a céljármű.
        assertEquals(6, hardLevels.stream()
                .map(level -> level.vehicles().stream()
                        .filter(Vehicle::target)
                        .findFirst()
                        .orElseThrow()
                        .coordinate(level.rowCount(), level.columnCount()).toString())
                .distinct()
                .count());
    }

    // A különböző nevek mellett a járművek és akadályok együttese is legyen egyedi.
    @Test
    void everyLevelHasAUniqueNameAndVehicleAndObstacleLayout() throws IOException {
        List<Level> levels = LevelLoader.loadLevels();
        Set<String> names = new HashSet<>();
        Set<String> layouts = new HashSet<>();

        for (Level level : levels) {
            names.add(level.name());
            layouts.add(layoutOf(level));
        }

        assertEquals(18, names.size());
        assertEquals(18, layouts.size());
    }

    // A közepes pályák eltérő akadályokat, induló pozíciókat és kijáratokat használnak.
    @Test
    void mediumLevelsHaveDistinctObstaclePatternsAndBlockTheTargetLane() throws IOException {
        List<Level> mediumLevels = LevelLoader.loadLevels().stream()
                .filter(level -> level.difficulty() == Difficulty.MEDIUM)
                .toList();
        Set<String> names = mediumLevels.stream().map(Level::name).collect(Collectors.toSet());
        Set<String> vehicleLayouts = mediumLevels.stream()
                .map(this::vehicleLayout)
                .collect(Collectors.toSet());
        Set<String> obstacleLayouts = mediumLevels.stream()
                .map(level -> level.obstacles().cells().toString())
                .collect(Collectors.toSet());
        Set<String> targetStarts = mediumLevels.stream()
                .map(level -> level.vehicles().stream()
                        .filter(Vehicle::target)
                        .findFirst()
                        .orElseThrow()
                        .coordinate(level.rowCount(), level.columnCount()).toString())
                .collect(Collectors.toSet());
        Set<String> exits = mediumLevels.stream()
                .map(level -> level.exitCoordinate().toString())
                .collect(Collectors.toSet());

        assertEquals(6, mediumLevels.size());
        assertEquals(6, names.size());
        assertEquals(6, vehicleLayouts.size());
        assertEquals(6, obstacleLayouts.size());
        assertEquals(6, targetStarts.size());
        assertEquals(6, exits.size());
        for (Level level : mediumLevels) {
            assertEquals(8, level.rowCount());
            assertEquals(14, level.vehicles().size());
            assertTrue(level.obstacles().cells().size() >= 6);
            assertTrue(level.vehicles().stream()
                    .filter(vehicle -> !vehicle.target())
                    // Legalább egy másik jármű útját kell szabaddá tenni a kijárathoz.
                    .anyMatch(vehicle -> blocksTargetLane(level, vehicle)),
                    () -> level.name() + " must make the exit path a puzzle");
            assertTrue(level.vehicles().stream().anyMatch(vehicle -> vehicle.length() == 3));
        }
        assertTrue(mediumLevels.stream()
                .map(level -> level.vehicles().stream()
                        .filter(Vehicle::target)
                        .findFirst()
                        .orElseThrow()
                        .coordinate(level.rowCount(), level.columnCount()).toString())
                .noneMatch("A4"::equals));
    }

    // Hibás gyökérelem, üres dokumentum, befejezetlen XML és külső entitás is elutasítandó.
    @Test
    void rejectsMalformedLevelDocuments() {
        assertThrows(IOException.class, () -> load("<maps/>"));
        assertThrows(IOException.class, () -> load("<levels rows=\"6\" columns=\"6\" exitSide=\"RIGHT\"/>"));
        assertThrows(IOException.class, () -> load("<levels>"));
        assertThrows(IOException.class,
                () -> load("<!DOCTYPE levels [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
                        + "<levels rows=\"6\" columns=\"6\" exitSide=\"RIGHT\">&x;</levels>"));
    }

    // Nem létező azonosítóhoz nem adhat vissza másik pályát.
    @Test
    void reportsUnknownLevelIds() {
        assertThrows(IOException.class, () -> LevelLoader.loadLevel(999));
    }

    // Hiányzó külső fájlnál az alapértelmezett pályák is betölthetők és játszhatók.
    @Test
    void missingExternalLevelFileUsesPlayableDefaultsForEveryDifficulty() {
        List<Level> levels = LevelLoader.loadLevels(temporaryDirectory.resolve("missing.xml"));

        assertDefaultLevels(levels);
        Level easyLevel = levels.getFirst();
        GameSession humanSession = new GameSession(easyLevel);
        assertTrue(humanSession.moveSteps("RR", Direction.RIGHT,
                easyLevel.columnCount() - 1).successful());
        assertEquals(TargetOwner.HUMAN, humanSession.winner());

        GameSession aiSession = new GameSession(easyLevel);
        assertTrue(aiSession.moveSteps("BB", Direction.LEFT,
                easyLevel.columnCount() - 2).successful());
        assertEquals(TargetOwner.AI, aiSession.winner());
    }

    // Hibás külső XML esetén szintén használható alapértelmezett pályákra vált.
    @Test
    void invalidExternalLevelFileUsesPlayableDefaults() throws IOException {
        Path invalidFile = temporaryDirectory.resolve("invalid.xml");
        Files.writeString(invalidFile, "<levels>");

        assertDefaultLevels(LevelLoader.loadLevels(invalidFile));
    }

    private void assertDefaultLevels(List<Level> levels) {
        // Minden nehézséghez pontosan egy alapértelmezett pálya tartozik.
        assertEquals(Difficulty.values().length, levels.size());
        for (Difficulty difficulty : Difficulty.values()) {
            Level level = levels.stream()
                    .filter(candidate -> candidate.difficulty() == difficulty)
                    .findFirst()
                    .orElseThrow();
            assertEquals(difficulty.vehicleCount(), level.vehicles().size());
            int exitColumn = level.exitSide() == ExitSide.LEFT ? 0 : level.columnCount();
            assertEquals(exitColumn, level.exitCoordinate().columnIndex());
            if (difficulty == Difficulty.HARD) {
                assertEquals(10, level.obstacles().cells().size());
            }
            assertTrue(level.vehicles().stream().anyMatch(Vehicle::target));
        }
    }

    private void load(String xml) throws IOException {
        LevelLoader.loadLevels(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private String layoutOf(Level level) {
        return vehicleLayout(level) + "|" + obstacleLayout(level);
    }

    private String vehicleLayout(Level level) {
        // ID szerint rendezi, hogy az XML-sorrend ne befolyásolja az elrendezés összehasonlítását.
        return level.vehicles().stream()
                .sorted(java.util.Comparator.comparing(Vehicle::id))
                .map(vehicle -> vehicle.id() + ":" + vehicle.row() + ":" + vehicle.column()
                        + ":" + vehicle.length() + ":" + vehicle.orientation())
                .collect(Collectors.joining(";"));
    }

    private String obstacleLayout(Level level) {
        // Rendezi az akadálycellákat, így azonos cellakészlet ugyanazt a leírást adja.
        return level.obstacles().cells().stream()
                .map(cell -> cell.row() + ":" + cell.column())
                .sorted()
                .collect(Collectors.joining(";"));
    }

    private boolean blocksTargetLane(Level level, Vehicle vehicle) {
        Vehicle target = level.vehicles().stream()
                .filter(Vehicle::target)
                .findFirst()
                .orElseThrow();
        int targetRow = target.row();
        int firstExitColumn = target.column() + target.length();
        // A kijáratsáv attól függ, melyik oldalon hagyhatja el a céljármű a táblát.
        for (int part = 0; part < vehicle.length(); part++) {
            int row = vehicle.row() + (vehicle.orientation() == Orientation.VERTICAL ? part : 0);
            int column = vehicle.column()
                    + (vehicle.orientation() == Orientation.HORIZONTAL ? part : 0);
            boolean inExitLane = level.exitSide() == ExitSide.RIGHT
                    ? column >= firstExitColumn && column < level.columnCount()
                    : column < target.column();
            if (row == targetRow && inExitLane) {
                return true;
            }
        }
        return false;
    }
}
