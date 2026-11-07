package org.example.model.map;

import org.example.model.GameSave;

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
    private boolean won;

    public GameSession(Level level) {
        this.level = Objects.requireNonNull(level, "level");
        vehicles = level.vehicles();
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
                    original.orientation(), original.target()));
        }
        validateRestoredVehicles(restoredVehicles);
        vehicles = List.copyOf(restoredVehicles);
        totalSteps = save.steps();
        Vehicle target = vehicles.stream().filter(Vehicle::target).findFirst().orElseThrow();
        won = isAtExit(target);
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
        return won;
    }

    public boolean canUndo() {
        return !history.isEmpty();
    }

    public int undoableSteps() {
        return history.size();
    }

    public MoveResult move(String vehicleId, Direction direction, int distance) {
        if (!active) {
            return reject("A pálya már le van zárva.");
        }
        if (won) {
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
        if (direction == null) {
            return reject("A mozgás iránya nincs megadva.");
        }
        if (vehicle.orientation() != direction.orientation()) {
            return reject("Ez az autó csak " + direction.orientation()
                    + " irányban, a saját tengelye mentén mozgatható.");
        }

        int moved = 0;
        for (int step = 0; step < distance; step++) {
            vehicleIndex = findVehicleIndex(vehicleId);
            vehicle = vehicles.get(vehicleIndex);
            int row = vehicle.row() + direction.rowDelta();
            int column = vehicle.column() + direction.columnDelta();
            String problem = movementProblem(vehicleIndex, vehicle, row, column, direction);
            if (problem != null) {
                if (moved == 0) {
                    LOGGER.warning(problem);
                    return new MoveResult(false, 0, problem);
                }
                String message = moved + " lépés megtéve; tovább nem mozdítható. " + problem;
                LOGGER.info(message);
                return new MoveResult(true, moved, message);
            }
            updateVehicle(vehicleIndex, row, column);
            history.push(new Move(vehicle.id(), vehicle.row(), vehicle.column()));
            totalSteps++;
            moved++;
            if (vehicle.target() && isAtExit(vehicles.get(vehicleIndex))) {
                won = true;
                LOGGER.info(() -> "Pálya teljesítve: " + level.name());
                return new MoveResult(true, moved, "Siker! Az autó elérte a kijáratot.");
            }
        }
        int movedSteps = moved;
        LOGGER.info(() -> "Járműmozgatás: " + vehicleId + ", " + direction
                + ", lépések: " + movedSteps);
        return new MoveResult(true, moved, moved + " lépés sikeresen megtéve.");
    }

    public MoveResult move(String vehicleId, String direction, int distance) {
        if (direction == null) {
            return reject("A mozgás iránya nincs megadva.");
        }
        try {
            return move(vehicleId, Direction.valueOf(direction.trim().toUpperCase()), distance);
        } catch (IllegalArgumentException exception) {
            return reject("Érvénytelen irány: " + direction
                    + ". Lehetséges értékek: UP, DOWN, LEFT, RIGHT.");
        }
    }

    public MoveResult moveToCoordinate(String vehicleId, String coordinateNotation) {
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
            targetRow = coordinate.rowIndex(level.boardSize());
            targetColumn = coordinate.columnIndex(level.boardSize());
        } catch (IllegalArgumentException exception) {
            if (vehicle.target() && level.exitSide() == ExitSide.RIGHT
                    && coordinate.equals(level.exitCoordinate())) {
                int distanceToExit = Math.max(1,
                        level.boardSize() - vehicle.column() - vehicle.length() + 1);
                return move(vehicleId, Direction.RIGHT, distanceToExit);
            }
            return reject(exception.getMessage());
        }
        if (targetRow == vehicle.row() && targetColumn != vehicle.column()
                && vehicle.orientation() == Orientation.HORIZONTAL) {
            int delta = targetColumn - vehicle.column();
            return move(vehicleId, delta > 0 ? Direction.RIGHT : Direction.LEFT, Math.abs(delta));
        }
        if (targetColumn == vehicle.column() && targetRow != vehicle.row()
                && vehicle.orientation() == Orientation.VERTICAL) {
            int delta = targetRow - vehicle.row();
            return move(vehicleId, delta > 0 ? Direction.DOWN : Direction.UP, Math.abs(delta));
        }
        return reject("A megadott koordináta nem mozgatható elérés az autó tengelye mentén.");
    }

    public void move(String vehicleId, int row, int column) {
        int vehicleIndex = findVehicleIndex(vehicleId);
        Vehicle vehicle = vehicles.get(vehicleIndex);
        int rowDelta = row - vehicle.row();
        int columnDelta = column - vehicle.column();
        if (rowDelta != 0 && columnDelta != 0) {
            throw new IllegalArgumentException("Az autó nem mozoghat átlósan.");
        }
        Direction direction = rowDelta < 0 ? Direction.UP
                : rowDelta > 0 ? Direction.DOWN
                : columnDelta < 0 ? Direction.LEFT : Direction.RIGHT;
        MoveResult result = move(vehicleId, direction, Math.max(Math.abs(rowDelta),
                Math.abs(columnDelta)));
        if (!result.successful()) {
            throw new IllegalArgumentException(result.message());
        }
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
        for (int i = 0; i < moveCount; i++) {
            Move move = history.pop();
            int vehicleIndex = findVehicleIndex(move.vehicleId());
            updateVehicle(vehicleIndex, move.previousRow(), move.previousColumn());
            totalSteps--;
        }
        won = false;
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

    private void validateRestoredVehicles(List<Vehicle> restoredVehicles) {
        List<Obstacles.Cell> occupied = new ArrayList<>();
        for (Vehicle vehicle : restoredVehicles) {
            if (vehicle.target()
                    && vehicle.row() != level.exitCoordinate().rowIndex(level.boardSize())) {
                throw new IllegalArgumentException(
                        "The saved target vehicle is not aligned with the exit.");
            }
            for (int part = 0; part < vehicle.length(); part++) {
                int row = vehicle.row()
                        + (vehicle.orientation() == Orientation.VERTICAL ? part : 0);
                int column = vehicle.column()
                        + (vehicle.orientation() == Orientation.HORIZONTAL ? part : 0);
                boolean targetAtExit = vehicle.target() && isAtExit(vehicle);
                if (row >= level.boardSize() || column >= level.boardSize()) {
                    if (targetAtExit && row == level.exitCoordinate().rowIndex(level.boardSize())
                            && column == level.exitCoordinate().columnIndex()) {
                        continue;
                    }
                    throw new IllegalArgumentException("Saved vehicle is outside the board.");
                }
                Obstacles.Cell cell = new Obstacles.Cell(row, column);
                if (level.obstacles().cells().contains(cell) || occupied.contains(cell)) {
                    throw new IllegalArgumentException(
                            "Saved vehicles overlap each other or an obstacle.");
                }
                occupied.add(cell);
            }
        }
    }

    private String movementProblem(
            int vehicleIndex,
            Vehicle vehicle,
            int row,
            int column,
            Direction direction
    ) {
        boolean targetLeavingThroughExit = vehicle.target()
                && direction == Direction.RIGHT
                && level.exitSide() == ExitSide.RIGHT
                && row == level.exitCoordinate().rowIndex(level.boardSize());
        for (int part = 0; part < vehicle.length(); part++) {
            int occupiedRow = row
                    + (vehicle.orientation() == Orientation.VERTICAL ? part : 0);
            int occupiedColumn = column
                    + (vehicle.orientation() == Orientation.HORIZONTAL ? part : 0);
            if (occupiedRow < 0 || occupiedColumn < 0) {
                return "A mozgás kilépne a pálya szélén.";
            }
            if (occupiedRow >= level.boardSize() || occupiedColumn >= level.boardSize()) {
                if (targetLeavingThroughExit
                        && occupiedRow == level.exitCoordinate().rowIndex(level.boardSize())
                        && occupiedColumn == level.exitCoordinate().columnIndex()) {
                    continue;
                }
                return "A mozgás kilépne a pálya szélén, nem a kijáraton keresztül.";
            }
            if (level.obstacles().cells().contains(
                    new Obstacles.Cell(occupiedRow, occupiedColumn))) {
                return "A mozgás akadályba ütközne a "
                        + GridCoordinate.fromGrid(occupiedRow, occupiedColumn, level.boardSize())
                        + " mezőn.";
            }
            for (int otherIndex = 0; otherIndex < vehicles.size(); otherIndex++) {
                if (otherIndex != vehicleIndex
                        && occupies(vehicles.get(otherIndex), occupiedRow, occupiedColumn)) {
                    return "A mozgás egy másik autóval ütközne a "
                            + GridCoordinate.fromGrid(occupiedRow, occupiedColumn, level.boardSize())
                            + " mezőn.";
                }
            }
        }
        return null;
    }

    private boolean isAtExit(Vehicle vehicle) {
        return vehicle.row() == level.exitCoordinate().rowIndex(level.boardSize())
                && vehicle.column() + vehicle.length() - 1
                >= level.exitCoordinate().columnIndex();
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
        Vehicle moved = new Vehicle(current.id(), current.color(), row, column,
                current.length(), current.orientation(), current.target());
        List<Vehicle> updatedVehicles = new ArrayList<>(vehicles);
        updatedVehicles.set(vehicleIndex, moved);
        vehicles = List.copyOf(updatedVehicles);
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
}
