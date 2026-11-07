package org.example.model.map;

import org.example.model.GameSave;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameSessionRestoreTest {
    @Test
    void restoresSavedVehiclePositionsAndStepCount() {
        Level level = LevelTestFixtures.openLevel();
        List<GameSave.VehiclePosition> positions = positions(level);
        GameSession restored = new GameSession(level,
                new GameSave("Saved", level.id(), 3, positions));

        assertEquals(3, restored.steps());
        assertEquals(level.vehicles(), restored.vehicles());
        assertFalse(restored.canUndo());
    }

    @Test
    void rejectsSavesForAnotherLevelOrMissingVehicles() {
        Level level = LevelTestFixtures.openLevel();
        List<GameSave.VehiclePosition> positions = positions(level);

        assertThrows(IllegalArgumentException.class, () -> new GameSession(
                level, new GameSave("Other level", 2, 0, positions)));
        assertThrows(IllegalArgumentException.class, () -> new GameSession(
                level, new GameSave("Missing vehicle", 1, 0, positions.subList(0, 5))));
    }

    @Test
    void rejectsDuplicateOverlappingAndOutOfBoundsSavedVehicles() {
        Level level = LevelTestFixtures.openLevel();
        List<GameSave.VehiclePosition> duplicate = new ArrayList<>(positions(level));
        duplicate.set(1, new GameSave.VehiclePosition("RR", 2, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new GameSession(level, new GameSave("Duplicate", 1, 0, duplicate)));

        List<GameSave.VehiclePosition> overlap = new ArrayList<>(positions(level));
        overlap.set(1, new GameSave.VehiclePosition("BB", 2, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new GameSession(level, new GameSave("Overlap", 1, 0, overlap)));

        List<GameSave.VehiclePosition> outside = new ArrayList<>(positions(level));
        outside.set(1, new GameSave.VehiclePosition("BB", 5, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new GameSession(level, new GameSave("Outside", 1, 0, outside)));
    }

    private List<GameSave.VehiclePosition> positions(Level level) {
        return level.vehicles().stream()
                .map(vehicle -> new GameSave.VehiclePosition(
                        vehicle.id(), vehicle.row(), vehicle.column()))
                .toList();
    }
}
