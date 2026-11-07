package org.example.model.map;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelSolvabilityTest {
    @Test
    void everyConfiguredLevelHasAPathToItsExit() throws IOException {
        for (Level level : LevelLoader.loadLevels()) {
            assertTrue(isSolvable(level), () -> "Level cannot be solved: " + level.name());
        }
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
                    && targetPosition.column() + target.length() - 1
                    >= level.exitCoordinate().columnIndex()) {
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
            boolean exitCell = moving.target() && direction == Direction.RIGHT
                    && row == level.exitCoordinate().rowIndex(level.boardSize())
                    && column == level.exitCoordinate().columnIndex();
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
