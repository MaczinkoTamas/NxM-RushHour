package org.example.model.map;

import java.util.Objects;

public record Vehicle(
        String id,
        VehicleColor color,
        int row,
        int column,
        int length,
        Orientation orientation,
        boolean target
) {
    public Vehicle {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Vehicle ID must not be blank");
        }
        Objects.requireNonNull(color, "color");
        if (row < 0 || column < 0) {
            throw new IllegalArgumentException("Vehicle coordinates must not be negative");
        }
        if (length < 2) {
            throw new IllegalArgumentException("Vehicle length must be at least 2");
        }
        Objects.requireNonNull(orientation, "orientation");
    }

    public Vehicle(String id, int row, int column, int length,
                   Orientation orientation, boolean target) {
        this(id, VehicleColor.fromMarker(id), row, column, length, orientation, target);
    }

    public String marker() {
        return color.marker();
    }

    public GridCoordinate coordinate(int boardSize) {
        return GridCoordinate.fromGrid(row, column, boardSize);
    }
}
