package org.example.model.map;

import org.example.model.mainmenu.settings.Difficulty;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record Level(
        int id,
        String name,
        int boardSize,
        ExitSide exitSide,
        GridCoordinate exitCoordinate,
        Difficulty difficulty,
        List<Vehicle> vehicles,
        Obstacles obstacles
) {
    public Level {
        if (id < 1 || boardSize < 1) {
            throw new IllegalArgumentException("Level ID and board size must be positive");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Level name must not be blank");
        }
        Objects.requireNonNull(exitSide, "exitSide");
        Objects.requireNonNull(exitCoordinate, "exitCoordinate");
        Objects.requireNonNull(difficulty, "difficulty");
        vehicles = List.copyOf(vehicles);
        Objects.requireNonNull(obstacles, "obstacles");
        if (boardSize != difficulty.boardSize()) {
            throw new IllegalArgumentException("Board size does not match the level difficulty");
        }
        if (vehicles.size() != difficulty.vehicleCount()) {
            throw new IllegalArgumentException(
                    "Expected " + difficulty.vehicleCount() + " vehicles for "
                            + difficulty.displayName() + " difficulty, found " + vehicles.size());
        }

        Set<String> vehicleIds = new HashSet<>();
        Set<Obstacles.Cell> occupiedCells = new HashSet<>();
        int targetCount = 0;
        for (Vehicle vehicle : vehicles) {
            if (!vehicleIds.add(vehicle.id())) {
                throw new IllegalArgumentException("Duplicate vehicle ID: " + vehicle.id());
            }
            if (vehicle.target()) {
                targetCount++;
                if (vehicle.orientation() != Orientation.HORIZONTAL) {
                    throw new IllegalArgumentException("Target vehicle must be horizontal");
                }
                if (vehicle.color() != VehicleColor.RED || !"RR".equals(vehicle.id())) {
                    throw new IllegalArgumentException(
                            "The target vehicle must use the red R color marker and RR ID");
                }
            }
            for (int offset = 0; offset < vehicle.length(); offset++) {
                int row = vehicle.row()
                        + (vehicle.orientation() == Orientation.VERTICAL ? offset : 0);
                int column = vehicle.column()
                        + (vehicle.orientation() == Orientation.HORIZONTAL ? offset : 0);
                if (row >= boardSize || column >= boardSize) {
                    throw new IllegalArgumentException(
                            "Vehicle extends beyond the board: " + vehicle.id());
                }
                if (!occupiedCells.add(new Obstacles.Cell(row, column))) {
                    throw new IllegalArgumentException(
                            "Vehicles overlap at row " + row + ", column " + column);
                }
            }
        }
        if (targetCount != 1) {
            throw new IllegalArgumentException("A level must have exactly one target vehicle");
        }
        Vehicle targetVehicle = vehicles.stream().filter(Vehicle::target).findFirst().orElseThrow();
        if (exitSide != ExitSide.RIGHT || exitCoordinate.columnIndex() != boardSize
                || exitCoordinate.rowIndex(boardSize) != targetVehicle.row()) {
            throw new IllegalArgumentException(
                    "Exit must be immediately to the right of the target vehicle's row");
        }
        for (Obstacles.Cell cell : obstacles.cells()) {
            if (cell.row() < 0 || cell.row() >= boardSize
                    || cell.column() < 0 || cell.column() >= boardSize) {
                throw new IllegalArgumentException("Obstacle is outside the board");
            }
            if (!occupiedCells.add(cell)) {
                throw new IllegalArgumentException("Obstacle overlaps another map item");
            }
        }
    }
}
