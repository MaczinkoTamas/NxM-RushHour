package nye.progtech.model.map;

import java.util.Objects;

public record Vehicle(
        String id,
        VehicleColor color,
        int row,
        int column,
        int length,
        Orientation orientation,
        TargetOwner targetOwner
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
        this(id, VehicleColor.fromMarker(id), row, column, length, orientation,
                target ? TargetOwner.HUMAN : null);
    }

    public Vehicle(String id, int row, int column, int length,
                   Orientation orientation, TargetOwner targetOwner) {
        this(id, VehicleColor.fromMarker(id), row, column, length, orientation, targetOwner);
    }

    public Vehicle(String id, VehicleColor color, int row, int column, int length,
                   Orientation orientation, boolean target) {
        this(id, color, row, column, length, orientation,
                target ? TargetOwner.HUMAN : null);
    }

    public boolean target() {
        return targetOwner != null;
    }

    // A rövid jelölőből adja meg a jármű színét.
    public String marker() {
        return color.marker();
    }

    // A pálya méretével együtt a belső pozíciót felhasználói koordinátává alakítja.
    public GridCoordinate coordinate(int rowCount, int columnCount) {
        return GridCoordinate.fromGrid(row, column, rowCount, columnCount);
    }
}
