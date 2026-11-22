package nye.progtech.model.map;

import nye.progtech.model.mainmenu.settings.Difficulty;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record Level(
        int id,
        String name,
        int rowCount,
        int columnCount,
        ExitSide exitSide,
        GridCoordinate exitCoordinate,
        Difficulty difficulty,
        List<Vehicle> vehicles,
        Obstacles obstacles
) {
    public Level {
        if (id < 1 || rowCount < 1 || columnCount < 1 || columnCount > 26) {
            throw new IllegalArgumentException(
                    "Level ID and dimensions must be positive; columns cannot exceed 26");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Level name must not be blank");
        }
        Objects.requireNonNull(exitSide, "exitSide");
        Objects.requireNonNull(exitCoordinate, "exitCoordinate");
        Objects.requireNonNull(difficulty, "difficulty");
        vehicles = List.copyOf(vehicles);
        Objects.requireNonNull(obstacles, "obstacles");
        if (vehicles.size() != difficulty.vehicleCount()) {
            throw new IllegalArgumentException(
                    "Expected " + difficulty.vehicleCount() + " vehicles for "
                            + difficulty.displayName() + " difficulty, found " + vehicles.size());
        }

        Set<Obstacles.Cell> occupiedCells = validateVehicles(vehicles, rowCount, columnCount);
        validateHumanExit(vehicles, exitSide, exitCoordinate, rowCount, columnCount);
        validateObstacles(obstacles, rowCount, columnCount, occupiedCells);
    }

    private static Set<Obstacles.Cell> validateVehicles(
            List<Vehicle> vehicles,
            int rowCount,
            int columnCount
    ) {
        Set<String> vehicleIds = new HashSet<>();
        Set<Obstacles.Cell> occupiedCells = new HashSet<>();
        Set<TargetOwner> targetOwners = new HashSet<>();
        for (Vehicle vehicle : vehicles) {
            if (!vehicleIds.add(vehicle.id())) {
                throw new IllegalArgumentException("Duplicate vehicle ID: " + vehicle.id());
            }
            validateTarget(vehicle, targetOwners);
            occupyVehicleCells(vehicle, rowCount, columnCount, occupiedCells);
        }
        if (!targetOwners.containsAll(List.of(TargetOwner.HUMAN, TargetOwner.AI))) {
            throw new IllegalArgumentException(
                    "A level must have one target vehicle for each player");
        }
        return occupiedCells;
    }

    private static void validateTarget(Vehicle vehicle, Set<TargetOwner> targetOwners) {
        if (vehicle.targetOwner() == null) {
            return;
        }
        if (!targetOwners.add(vehicle.targetOwner())) {
            throw new IllegalArgumentException("A target owner may have only one target vehicle");
        }
        if (vehicle.orientation() != Orientation.HORIZONTAL) {
            throw new IllegalArgumentException("Target vehicle must be horizontal");
        }
        if (vehicle.targetOwner() == TargetOwner.HUMAN
                && (vehicle.color() != VehicleColor.RED || !"RR".equals(vehicle.id()))) {
            throw new IllegalArgumentException(
                    "The human target must use the red R color marker and RR ID");
        }
    }

    private static void occupyVehicleCells(
            Vehicle vehicle,
            int rowCount,
            int columnCount,
            Set<Obstacles.Cell> occupiedCells
    ) {
        for (int offset = 0; offset < vehicle.length(); offset++) {
            int row = vehicle.row()
                    + (vehicle.orientation() == Orientation.VERTICAL ? offset : 0);
            int column = vehicle.column()
                    + (vehicle.orientation() == Orientation.HORIZONTAL ? offset : 0);
            if (row >= rowCount || column >= columnCount) {
                throw new IllegalArgumentException("Vehicle extends beyond the board: " + vehicle.id());
            }
            if (!occupiedCells.add(new Obstacles.Cell(row, column))) {
                throw new IllegalArgumentException(
                        "Vehicles overlap at row " + row + ", column " + column);
            }
        }
    }

    private static void validateHumanExit(
            List<Vehicle> vehicles,
            ExitSide exitSide,
            GridCoordinate exitCoordinate,
            int rowCount,
            int columnCount
    ) {
        Vehicle humanTarget = vehicles.stream()
                .filter(vehicle -> vehicle.targetOwner() == TargetOwner.HUMAN)
                .findFirst()
                .orElseThrow();
        boolean validExitColumn = switch (exitSide) {
            case RIGHT -> exitCoordinate.columnIndex() == columnCount;
            case LEFT -> exitCoordinate.columnIndex() == 0;
            case TOP, BOTTOM -> false;
        };
        if (!validExitColumn || exitCoordinate.rowIndex(rowCount) != humanTarget.row()) {
            throw new IllegalArgumentException(
                    "Human exit must be on the target vehicle's row and a horizontal board edge");
        }
    }

    private static void validateObstacles(
            Obstacles obstacles,
            int rowCount,
            int columnCount,
            Set<Obstacles.Cell> occupiedCells
    ) {
        for (Obstacles.Cell cell : obstacles.cells()) {
            if (cell.row() < 0 || cell.row() >= rowCount
                    || cell.column() < 0 || cell.column() >= columnCount) {
                throw new IllegalArgumentException("Obstacle is outside the board");
            }
            if (!occupiedCells.add(cell)) {
                throw new IllegalArgumentException("Obstacle overlaps another map item");
            }
        }
    }

    public List<TargetGoal> targetGoals() {
        return List.of(targetGoal(TargetOwner.HUMAN), targetGoal(TargetOwner.AI));
    }

    public TargetGoal targetGoal(TargetOwner owner) {
        Vehicle target = targetFor(owner);
        ExitSide side = owner == TargetOwner.HUMAN
                ? exitSide : exitSide.oppositeHorizontalSide();
        GridCoordinate coordinate = owner == TargetOwner.HUMAN
                ? exitCoordinate
                : new GridCoordinate(side == ExitSide.LEFT ? 0 : columnCount,
                        target.coordinate(rowCount, columnCount).rank());
        return new TargetGoal(owner, target.id(), side, coordinate);
    }

    public Vehicle targetFor(TargetOwner owner) {
        return vehicles.stream()
                .filter(vehicle -> vehicle.targetOwner() == owner)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No target vehicle for " + owner));
    }
}
