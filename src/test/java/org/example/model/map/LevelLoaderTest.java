package org.example.model.map;

import org.example.model.mainmenu.settings.Difficulty;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelLoaderTest {
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
            assertEquals("G4", level.exitCoordinate().toString());
            assertEquals(level.difficulty().vehicleCount(), level.vehicles().size());
            assertTrue(level.vehicles().stream().allMatch(vehicle -> vehicle.length() >= 2));
        }
    }

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

    @Test
    void rejectsMalformedLevelDocuments() {
        assertThrows(IOException.class, () -> load("<maps/>"));
        assertThrows(IOException.class, () -> load("<levels boardSize=\"6\" exitSide=\"RIGHT\"/>"));
        assertThrows(IOException.class, () -> load("<levels>"));
        assertThrows(IOException.class,
                () -> load("<!DOCTYPE levels [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
                        + "<levels boardSize=\"6\" exitSide=\"RIGHT\">&x;</levels>"));
    }

    @Test
    void reportsUnknownLevelIds() {
        assertThrows(IOException.class, () -> LevelLoader.loadLevel(999));
    }

    private void load(String xml) throws IOException {
        LevelLoader.loadLevels(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private String layoutOf(Level level) {
        String vehicles = level.vehicles().stream()
                .sorted(java.util.Comparator.comparing(Vehicle::id))
                .map(vehicle -> vehicle.id() + ":" + vehicle.row() + ":" + vehicle.column()
                        + ":" + vehicle.length() + ":" + vehicle.orientation())
                .collect(Collectors.joining(";"));
        String obstacles = level.obstacles().cells().stream()
                .map(cell -> cell.row() + ":" + cell.column())
                .sorted()
                .collect(Collectors.joining(";"));
        return vehicles + "|" + obstacles;
    }
}
