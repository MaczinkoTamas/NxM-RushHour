package nye.progtech.model.map;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** A betöltött mentés járműpozícióinak érvényességét ellenőrzi. */
final class SavedVehicleValidator {
    private SavedVehicleValidator() {
    }

    /** Kizárja a pályán kívüli, egymást fedő vagy akadályra kerülő járműveket. */
    static void validate(Level level, List<Vehicle> vehicles) {
        Set<Obstacles.Cell> occupiedCells = new HashSet<>();
        for (Vehicle vehicle : vehicles) {
            validateTargetRow(level, vehicle);
            addVehicleCells(level, vehicle, occupiedCells);
        }
    }

    /** A céljárműnek a saját kijáratának sorában kell maradnia. */
    private static void validateTargetRow(Level level, Vehicle vehicle) {
        if (vehicle.target()
                && vehicle.row() != level.targetGoal(vehicle.targetOwner())
                        .exitCoordinate().rowIndex(level.rowCount())) {
            throw new IllegalArgumentException(
                    "The saved target vehicle is not aligned with the exit.");
        }
    }

    /** Ellenőrzi a jármű minden mezőjét, és felveszi a foglaltsági halmazba. */
    private static void addVehicleCells(
            Level level,
            Vehicle vehicle,
            Set<Obstacles.Cell> occupiedCells
    ) {
        boolean targetAtExit = vehicle.target() && isAtExit(level, vehicle);
        for (int part = 0; part < vehicle.length(); part++) {
            int row = vehicle.row()
                    + (vehicle.orientation() == Orientation.VERTICAL ? part : 0);
            int column = vehicle.column()
                    + (vehicle.orientation() == Orientation.HORIZONTAL ? part : 0);
            if (isOutside(level, row, column)) {
                if (isAllowedExitCell(level, vehicle, targetAtExit, row, column)) {
                    continue;
                }
                throw new IllegalArgumentException("Saved vehicle is outside the board.");
            }

            Obstacles.Cell cell = new Obstacles.Cell(row, column);
            if (level.obstacles().cells().contains(cell) || !occupiedCells.add(cell)) {
                throw new IllegalArgumentException(
                        "Saved vehicles overlap each other or an obstacle.");
            }
        }
    }

    private static boolean isOutside(Level level, int row, int column) {
        return row < 0 || column < 0
                || row >= level.rowCount() || column >= level.columnCount();
    }

    /** A jobb oldali kijáraton csak a céljármű legkülső mezője lóghat túl. */
    private static boolean isAllowedExitCell(
            Level level,
            Vehicle vehicle,
            boolean targetAtExit,
            int row,
            int column
    ) {
        if (!targetAtExit || !vehicle.target()) {
            return false;
        }
        TargetGoal goal = level.targetGoal(vehicle.targetOwner());
        return goal.exitSide() == ExitSide.RIGHT
                && row == goal.exitCoordinate().rowIndex(level.rowCount())
                && column == level.columnCount();
    }

    private static boolean isAtExit(Level level, Vehicle vehicle) {
        TargetGoal goal = level.targetGoal(vehicle.targetOwner());
        if (vehicle.row() != goal.exitCoordinate().rowIndex(level.rowCount())) {
            return false;
        }
        return switch (goal.exitSide()) {
            case RIGHT -> vehicle.column() + vehicle.length() - 1 >= level.columnCount();
            case LEFT -> vehicle.column() == 0;
            case TOP, BOTTOM -> false;
        };
    }
}
