package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import nye.progtech.model.mainmenu.settings.Difficulty;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelSolvabilityTest {
    @Test
    void everyConfiguredLevelHasAPathToItsExit() throws IOException {
        for (Level level : LevelLoader.loadLevels()) {
            boolean solvable = level.difficulty() == Difficulty.EASY
                    ? isSolvable(level) : isSolvableWithPlannedMoves(level);
            assertTrue(solvable, () -> "Level cannot be solved: " + level.name());
        }
    }

    @Test
    void everyConfiguredLevelHasExpectedExitBlockers() throws IOException {
        for (Level level : LevelLoader.loadLevels()) {
            Vehicle target = level.vehicles().stream()
                    .filter(Vehicle::target)
                    .findFirst()
                    .orElseThrow();
            int exitRow = level.exitCoordinate().rowIndex(level.boardSize());
            boolean targetAlreadyAtExit = level.exitSide() == ExitSide.RIGHT
                    ? target.column() + target.length() - 1 >= level.boardSize()
                    : target.column() == 0;
            assertFalse(targetAlreadyAtExit,
                    () -> "Target starts at the exit in " + level.name());
            Set<String> blockers = new HashSet<>();
            int firstColumn = level.exitSide() == ExitSide.RIGHT
                    ? target.column() + target.length() : target.column() - 1;
            int endColumn = level.exitSide() == ExitSide.RIGHT ? level.boardSize() : -1;
            int columnStep = level.exitSide() == ExitSide.RIGHT ? 1 : -1;
            for (int column = firstColumn; column != endColumn; column += columnStep) {
                for (Vehicle vehicle : level.vehicles()) {
                    if (!vehicle.target() && occupies(vehicle,
                            new Position(vehicle.row(), vehicle.column()), exitRow, column)) {
                        blockers.add(vehicle.id());
                    }
                }
            }
            boolean introductoryLevel = level.id() <= 2
                    || level.id() == 7 || level.id() == 8
                    || level.id() == 13 || level.id() == 14;
            int minimumBlockers = introductoryLevel ? 2 : 4;
            assertTrue(blockers.size() >= minimumBlockers,
                    () -> "Expected at least " + minimumBlockers
                            + " blocking vehicles: " + level.name());
        }
    }

    @Test
    void configuredVehiclesAndObstaclesDoNotOverlap() throws IOException {
        for (Level level : LevelLoader.loadLevels()) {
            Set<Obstacles.Cell> occupied = new HashSet<>();
            for (Vehicle vehicle : level.vehicles()) {
                for (int part = 0; part < vehicle.length(); part++) {
                    int row = vehicle.row()
                            + (vehicle.orientation() == Orientation.VERTICAL ? part : 0);
                    int column = vehicle.column()
                            + (vehicle.orientation() == Orientation.HORIZONTAL ? part : 0);
                    Obstacles.Cell cell = new Obstacles.Cell(row, column);
                    assertTrue(row >= 0 && column >= 0
                                    && row < level.boardSize() && column < level.boardSize(),
                            () -> "Vehicle outside the board in " + level.name());
                    assertTrue(!level.obstacles().cells().contains(cell),
                            () -> "Vehicle overlaps an obstacle in " + level.name());
                    assertTrue(occupied.add(cell),
                            () -> "Vehicles overlap in " + level.name());
                }
            }
        }
    }

    private boolean isSolvableWithPlannedMoves(Level level) {
        GameSession session = new GameSession(level);
        if (level.id() <= 2) {
            return session.move("BB", Direction.DOWN, 1).successful()
                    && session.move("GG", Direction.DOWN, 1).successful()
                    && moveTargetToExit(session);
        }
        if (level.id() == 7 || level.id() == 8) {
            return session.move("PP", Direction.RIGHT, 2).successful()
                    && session.move("BB", Direction.DOWN, 1).successful()
                    && session.move("GG", Direction.DOWN, 1).successful()
                    && moveTargetToExit(session);
        }
        if (level.id() == 13) {
            return session.move("BB", Direction.DOWN, 2).successful()
                    && session.move("GG", Direction.DOWN, 2).successful()
                    && moveTargetToExit(session);
        }
        if (level.id() == 14) {
            return session.move("PP", Direction.RIGHT, 2).successful()
                    && session.move("BB", Direction.DOWN, 2).successful()
                    && session.move("GG", Direction.DOWN, 2).successful()
                    && moveTargetToExit(session);
        }
        boolean leftExit = level.exitSide() == ExitSide.LEFT;
        Direction helperDirection = leftExit ? Direction.RIGHT : Direction.LEFT;
        int helperDistance = leftExit ? 4 : 2;
        if (!session.move("PP", helperDirection, helperDistance).successful()) {
            return false;
        }
        for (String blockerId : List.of("BB", "GG", "YY", "OO")) {
            if (!session.move(blockerId, Direction.DOWN, 1).successful()) {
                return false;
            }
        }
        return moveTargetToExit(session);
    }

    private boolean moveTargetToExit(GameSession session) {
        Level level = session.level();
        Vehicle target = session.vehicles().stream()
                .filter(Vehicle::target)
                .findFirst()
                .orElseThrow();
        boolean leftExit = level.exitSide() == ExitSide.LEFT;
        Direction exitDirection = leftExit ? Direction.LEFT : Direction.RIGHT;
        int exitDistance = leftExit ? target.column()
                : level.boardSize() - target.column() - target.length() + 1;
        return session.move(target.id(), exitDirection, exitDistance).successful()
                && session.isWon();
    }

    private boolean isSolvable(Level level) {
        List<Vehicle> vehicles = level.vehicles();
        int targetIndex = 0;
        for (int index = 0; index < vehicles.size(); index++) {
            if (vehicles.get(index).target()) {
                targetIndex = index;
                break;
            }
        }
        List<Position> initialState = vehicles.stream()
                .map(vehicle -> new Position(vehicle.row(), vehicle.column()))
                .toList();
        ArrayDeque<List<Position>> queue = new ArrayDeque<>();
        Set<List<Position>> visited = new HashSet<>();
        queue.add(initialState);
        visited.add(initialState);

        while (!queue.isEmpty()) {
            List<Position> state = queue.removeFirst();
            Vehicle target = vehicles.get(targetIndex);
            Position targetPosition = state.get(targetIndex);
            if (targetPosition.row() == level.exitCoordinate().rowIndex(level.boardSize())
                    && (level.exitSide() == ExitSide.RIGHT
                            && targetPosition.column() + target.length() - 1
                                    >= level.boardSize()
                        || level.exitSide() == ExitSide.LEFT && targetPosition.column() == 0)) {
                return true;
            }
            for (int index = 0; index < vehicles.size(); index++) {
                addNextStates(level, vehicles, state, index, queue, visited);
            }
        }
        return false;
    }

    private void addNextStates(
            Level level,
            List<Vehicle> vehicles,
            List<Position> state,
            int movingIndex,
            ArrayDeque<List<Position>> queue,
            Set<List<Position>> visited
    ) {
        Vehicle moving = vehicles.get(movingIndex);
        Position position = state.get(movingIndex);
        for (Direction direction : Direction.values()) {
            if (direction.orientation() != moving.orientation()) {
                continue;
            }
            Position next = new Position(position.row() + direction.rowDelta(),
                    position.column() + direction.columnDelta());
            if (!canOccupy(level, vehicles, state, movingIndex, moving, next, direction)) {
                continue;
            }
            List<Position> nextState = new ArrayList<>(state);
            nextState.set(movingIndex, next);
            List<Position> immutableState = List.copyOf(nextState);
            if (visited.add(immutableState)) {
                queue.addLast(immutableState);
            }
        }
    }

    private boolean canOccupy(
            Level level,
            List<Vehicle> vehicles,
            List<Position> state,
            int movingIndex,
            Vehicle moving,
            Position next,
            Direction direction
    ) {
        for (int part = 0; part < moving.length(); part++) {
            int row = next.row() + (moving.orientation() == Orientation.VERTICAL ? part : 0);
            int column = next.column()
                    + (moving.orientation() == Orientation.HORIZONTAL ? part : 0);
            boolean exitCell = moving.target()
                    && row == level.exitCoordinate().rowIndex(level.boardSize())
                    && direction == Direction.RIGHT && level.exitSide() == ExitSide.RIGHT
                    && column == level.boardSize();
            if (row < 0 || column < 0 || row >= level.boardSize() || column >= level.boardSize()) {
                if (!exitCell) {
                    return false;
                }
                continue;
            }
            if (level.obstacles().cells().contains(new Obstacles.Cell(row, column))) {
                return false;
            }
            for (int otherIndex = 0; otherIndex < vehicles.size(); otherIndex++) {
                if (otherIndex != movingIndex
                        && occupies(vehicles.get(otherIndex), state.get(otherIndex), row, column)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean occupies(Vehicle vehicle, Position position, int row, int column) {
        for (int part = 0; part < vehicle.length(); part++) {
            int occupiedRow = position.row()
                    + (vehicle.orientation() == Orientation.VERTICAL ? part : 0);
            int occupiedColumn = position.column()
                    + (vehicle.orientation() == Orientation.HORIZONTAL ? part : 0);
            if (occupiedRow == row && occupiedColumn == column) {
                return true;
            }
        }
        return false;
    }

    private record Position(int row, int column) {
    }
}
