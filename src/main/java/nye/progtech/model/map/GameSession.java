package nye.progtech.model.map;

import nye.progtech.model.GameSave;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

public final class GameSession {
    private static final Logger LOGGER = Logger.getLogger(GameSession.class.getName());

    private final Level level;
    private final Deque<Move> history = new ArrayDeque<>();
    private List<Vehicle> vehicles;
    private int totalSteps;
    private boolean active = true;
    private TargetOwner winner;

    public GameSession(Level level) {
        this.level = Objects.requireNonNull(level, "level");
        vehicles = level.vehicles();
        winner = findWinner();
    }

    public GameSession(Level level, GameSave save) {
        this(level);
        Objects.requireNonNull(save, "save");
        if (save.levelId() != level.id()) {
            throw new IllegalArgumentException("The save belongs to a different level.");
        }
        if (save.vehiclePositions().size() != vehicles.size()) {
            throw new IllegalArgumentException("The save does not contain every vehicle.");
        }

        // A mentett pozíciókat az eredeti pálya járműveihez rendeli azonosító alapján.
        List<Vehicle> restoredVehicles = new ArrayList<>(vehicles);
        Set<String> restoredIds = new HashSet<>();
        for (GameSave.VehiclePosition position : save.vehiclePositions()) {
            if (!restoredIds.add(position.vehicleId())) {
                throw new IllegalArgumentException("The save contains a duplicate vehicle position.");
            }
            int index = findVehicleIndex(position.vehicleId());
            Vehicle original = restoredVehicles.get(index);
            restoredVehicles.set(index, new Vehicle(original.id(), original.color(),
                    position.row(), position.column(), original.length(),
                    original.orientation(), original.targetOwner()));
        }

        SavedVehicleValidator.validate(level, restoredVehicles);
        vehicles = List.copyOf(restoredVehicles);
        totalSteps = save.steps();
        winner = findWinner();
    }

    public Level level() {
        return level;
    }

    public List<Vehicle> vehicles() {
        return List.copyOf(vehicles);
    }

    public int steps() {
        return totalSteps;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isWon() {
        return winner != null;
    }

    public TargetOwner winner() {
        return winner;
    }

    public boolean targetReached(TargetOwner owner) {
        return isAtExit(vehicles.get(findVehicleIndex(level.targetGoal(owner).vehicleId())));
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public int undoableSteps() {
        return history.size();
    }

    public List<LegalMove> legalMoves(String vehicleId) {
        int vehicleIndex = findVehicleIndex(vehicleId);
        Vehicle vehicle = vehicles.get(vehicleIndex);
        if (!active || isWon() || (vehicle.target() && isAtExit(vehicle))) {
            return List.of();
        }
        List<LegalMove> legalMoves = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (vehicle.orientation() != direction.orientation()) {
                continue;
            }
            int row = vehicle.row() + direction.rowDelta();
            int column = vehicle.column() + direction.columnDelta();
            if (movementProblem(vehicles, vehicleIndex, vehicle, row, column, direction) == null) {
                legalMoves.add(new LegalMove(vehicleId, direction, 1));
            }
        }
        return List.copyOf(legalMoves);
    }

    MoveResult moveSteps(String vehicleId, Direction direction, int distance) {
        if (!active) {
            return reject("A pálya már le van zárva.");
        }
        if (isWon()) {
            return reject("A pálya már teljesítve van.");
        }
        if (distance < 1) {
            return reject("A lépésszámnak legalább 1-nek kell lennie.");
        }

        int vehicleIndex;
        try {
            vehicleIndex = findVehicleIndex(vehicleId);
        } catch (IllegalArgumentException exception) {
            return reject(exception.getMessage());
        }
        Vehicle vehicle = vehicles.get(vehicleIndex);
        if (vehicle.target() && isAtExit(vehicle)) {
            return reject("Ez a céljármű már elérte a kijáratát.");
        }
        if (direction == null) {
            return reject("A mozgás iránya nincs megadva.");
        }
        if (vehicle.orientation() != direction.orientation()) {
            return reject("Ez az autó csak " + direction.orientation()
                    + " irányban, a saját tengelye mentén mozgatható.");
        }

        List<Vehicle> plannedVehicles = new ArrayList<>(vehicles);
        List<Move> plannedHistory = new ArrayList<>();
        for (int step = 0; step < distance; step++) {
            vehicle = plannedVehicles.get(vehicleIndex);
            int row = vehicle.row() + direction.rowDelta();
            int column = vehicle.column() + direction.columnDelta();
            String problem = movementProblem(
                    plannedVehicles, vehicleIndex, vehicle, row, column, direction);
            if (problem != null) {
                return reject(problem);
            }
            plannedHistory.add(new Move(vehicle.id(), vehicle.row(), vehicle.column()));
            Vehicle movedVehicle = movedTo(vehicle, row, column);
            plannedVehicles.set(vehicleIndex, movedVehicle);
            if (movedVehicle.target() && isAtExit(movedVehicle) && step < distance - 1) {
                return reject("A kért lépés túlnyúlna a cél kijáratán.");
            }
        }

        vehicles = List.copyOf(plannedVehicles);
        for (Move move : plannedHistory) {
            history.push(move);
        }
        totalSteps += distance;
        winner = findWinner();
        if (isWon()) {
            TargetOwner currentWinner = winner;
            LOGGER.info(() -> "A játék győztese: " + currentWinner + " (" + level.name() + ")");
            return new MoveResult(true, distance,
                    currentWinner == TargetOwner.HUMAN ? "Az emberi játékos nyert."
                            : "Az AI nyert.");
        }
        LOGGER.info(() -> "Járműmozgatás: " + vehicleId + ", " + direction
                + ", lépések: " + distance);
        return new MoveResult(true, distance, distance + " lépés sikeresen megtéve.");
    }

    public MoveResult move(String vehicleId, String coordinateNotation) {
        GridCoordinate coordinate;
        try {
            coordinate = GridCoordinate.parse(coordinateNotation);
        } catch (IllegalArgumentException exception) {
            return reject(exception.getMessage());
        }
        int vehicleIndex;
        try {
            vehicleIndex = findVehicleIndex(vehicleId);
        } catch (IllegalArgumentException exception) {
            return reject(exception.getMessage());
        }
        Vehicle vehicle = vehicles.get(vehicleIndex);
        int targetRow;
        int targetColumn;
        try {
            targetRow = coordinate.rowIndex(level.rowCount());
            targetColumn = coordinate.columnIndex(level.columnCount());
        } catch (IllegalArgumentException exception) {
            TargetGoal goal = vehicle.target()
                    ? level.targetGoal(vehicle.targetOwner()) : null;
            if (goal != null && goal.exitSide() == ExitSide.RIGHT
                    && coordinate.equals(goal.exitCoordinate())) {
                int distanceToExit = Math.max(1,
                        level.columnCount() - vehicle.column() - vehicle.length() + 1);
                return moveSteps(vehicleId, Direction.RIGHT, distanceToExit);
            }
            return reject(exception.getMessage());
        }
        if (targetRow == vehicle.row() && targetColumn != vehicle.column()
                && vehicle.orientation() == Orientation.HORIZONTAL) {
            int delta = targetColumn - vehicle.column();
            return moveSteps(vehicleId, delta > 0 ? Direction.RIGHT : Direction.LEFT, Math.abs(delta));
        }
        if (targetColumn == vehicle.column() && targetRow != vehicle.row()
                && vehicle.orientation() == Orientation.VERTICAL) {
            int delta = targetRow - vehicle.row();
            return moveSteps(vehicleId, delta > 0 ? Direction.DOWN : Direction.UP, Math.abs(delta));
        }
        return reject("A megadott koordináta nem mozgatható elérés az autó tengelye mentén.");
    }

    public void undo() {
        undo(1);
    }

    public void undo(int moveCount) {
        if (moveCount < 1) {
            throw new IllegalArgumentException("A visszavonandó lépések száma legalább 1 legyen.");
        }
        if (moveCount > history.size()) {
            throw new IllegalStateException("Ennyi lépés nem vonható vissza; eddig "
                    + history.size() + " mezőlépés történt.");
        }

        // Az előzmény verem: a legutóbbi mezőlépést kell először visszaállítani.
        for (int i = 0; i < moveCount; i++) {
            Move move = history.pop();
            int vehicleIndex = findVehicleIndex(move.vehicleId());
            updateVehicle(vehicleIndex, move.previousRow(), move.previousColumn());
            totalSteps--;
        }
        winner = findWinner();
        LOGGER.info(() -> moveCount + " lépés visszavonva.");
    }

    public GameSave save(String name) {
        List<GameSave.VehiclePosition> positions = vehicles.stream()
                .map(vehicle -> new GameSave.VehiclePosition(
                        vehicle.id(), vehicle.row(), vehicle.column()))
                .toList();
        LOGGER.info(() -> "Játékállás mentésre előkészítve: " + name);
        return new GameSave(name, level.id(), steps(), positions);
    }

    public void leaveLevel() {
        active = false;
        LOGGER.info(() -> "Pálya elhagyva: " + level.name());
    }

    private String movementProblem(
            List<Vehicle> currentVehicles,
            int vehicleIndex,
            Vehicle vehicle,
            int row,
            int column,
            Direction direction
    ) {
        TargetGoal goal = vehicle.target()
                ? level.targetGoal(vehicle.targetOwner()) : null;
        boolean targetLeavingThroughExit = goal != null
                && ((direction == Direction.RIGHT && goal.exitSide() == ExitSide.RIGHT)
                    || (direction == Direction.LEFT && goal.exitSide() == ExitSide.LEFT))
                && row == goal.exitCoordinate().rowIndex(level.rowCount());

        for (int part = 0; part < vehicle.length(); part++) {
            int occupiedRow = row
                    + (vehicle.orientation() == Orientation.VERTICAL ? part : 0);
            int occupiedColumn = column
                    + (vehicle.orientation() == Orientation.HORIZONTAL ? part : 0);
            if (occupiedRow < 0 || occupiedColumn < 0) {
                return "A mozgás kilépne a pálya szélén.";
            }
            if (occupiedRow >= level.rowCount() || occupiedColumn >= level.columnCount()) {
                if (targetLeavingThroughExit
                        && occupiedRow == goal.exitCoordinate().rowIndex(level.rowCount())
                        && occupiedColumn == goal.exitCoordinate().columnIndex()) {
                    continue;
                }
                return "A mozgás kilépne a pálya szélén, nem a kijáraton keresztül.";
            }
            if (level.obstacles().cells().contains(
                    new Obstacles.Cell(occupiedRow, occupiedColumn))) {
                return "A mozgás akadályba ütközne a "
                        + GridCoordinate.fromGrid(occupiedRow, occupiedColumn,
                                level.rowCount(), level.columnCount())
                        + " mezőn.";
            }
            for (int otherIndex = 0; otherIndex < currentVehicles.size(); otherIndex++) {
                if (otherIndex != vehicleIndex
                        && occupies(currentVehicles.get(otherIndex), occupiedRow, occupiedColumn)) {
                    return "A mozgás egy másik autóval ütközne a "
                            + GridCoordinate.fromGrid(occupiedRow, occupiedColumn,
                                    level.rowCount(), level.columnCount())
                            + " mezőn.";
                }
            }
        }
        return null;
    }

    private boolean isAtExit(Vehicle vehicle) {
        if (!vehicle.target()) {
            return false;
        }
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

    private TargetOwner findWinner() {
        for (TargetOwner owner : List.of(TargetOwner.HUMAN, TargetOwner.AI)) {
            Vehicle target = level.targetFor(owner);
            if (isAtExit(vehicles.get(findVehicleIndex(target.id())))) {
                return owner;
            }
        }
        return null;
    }

    private boolean occupies(Vehicle vehicle, int row, int column) {
        for (int offset = 0; offset < vehicle.length(); offset++) {
            int occupiedRow = vehicle.row()
                    + (vehicle.orientation() == Orientation.VERTICAL ? offset : 0);
            int occupiedColumn = vehicle.column()
                    + (vehicle.orientation() == Orientation.HORIZONTAL ? offset : 0);
            if (occupiedRow == row && occupiedColumn == column) {
                return true;
            }
        }
        return false;
    }

    private void updateVehicle(int vehicleIndex, int row, int column) {
        Vehicle current = vehicles.get(vehicleIndex);
        Vehicle moved = movedTo(current, row, column);
        List<Vehicle> updatedVehicles = new ArrayList<>(vehicles);
        updatedVehicles.set(vehicleIndex, moved);
        vehicles = List.copyOf(updatedVehicles);
    }

    private Vehicle movedTo(Vehicle vehicle, int row, int column) {
        return new Vehicle(vehicle.id(), vehicle.color(), row, column,
                vehicle.length(), vehicle.orientation(), vehicle.targetOwner());
    }

    private int findVehicleIndex(String vehicleId) {
        for (int index = 0; index < vehicles.size(); index++) {
            if (vehicles.get(index).id().equals(vehicleId)) {
                return index;
            }
        }
        throw new IllegalArgumentException("Ismeretlen autóazonosító: " + vehicleId);
    }

    private MoveResult reject(String message) {
        LOGGER.log(java.util.logging.Level.WARNING, message);
        return new MoveResult(false, 0, message);
    }

    private record Move(String vehicleId, int previousRow, int previousColumn) {
    }

    public record LegalMove(String vehicleId, Direction direction, int distance) {
    }
}
