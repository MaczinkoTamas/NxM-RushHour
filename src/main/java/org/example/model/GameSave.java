package org.example.model;

import java.util.List;
import java.util.Objects;

public record GameSave(
        String name,
        int levelId,
        int steps,
        List<VehiclePosition> vehiclePositions
) {
    public GameSave {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Save name must not be blank");
        }
        if (levelId < 1) {
            throw new IllegalArgumentException("Level ID must be positive");
        }
        if (steps < 0) {
            throw new IllegalArgumentException("Step count must not be negative");
        }
        vehiclePositions = List.copyOf(vehiclePositions);
    }

    public record VehiclePosition(String vehicleId, int row, int column) {
        public VehiclePosition {
            Objects.requireNonNull(vehicleId, "vehicleId");
            if (vehicleId.isBlank()) {
                throw new IllegalArgumentException("Vehicle ID must not be blank");
            }
            if (row < 0 || column < 0) {
                throw new IllegalArgumentException("Vehicle coordinates must not be negative");
            }
        }
    }
}
