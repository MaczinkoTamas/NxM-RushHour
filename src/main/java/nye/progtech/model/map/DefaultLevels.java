package nye.progtech.model.map;

import nye.progtech.model.mainmenu.settings.Difficulty;

import java.util.ArrayList;
import java.util.List;

final class DefaultLevels {
    private static final String[] VEHICLE_IDS = {
        "BB", "GG", "YY", "OO", "PP", "CC", "WW", "KK", "MM", "TT", "NN", "SS", "DD",
        "EE", "FF", "HH", "JJ"
    };

    private DefaultLevels() {
    }

    static List<Level> create() {
        List<Level> levels = new ArrayList<>();
        int id = 1;
        for (Difficulty difficulty : Difficulty.values()) {
            levels.add(createLevel(id++, difficulty));
        }
        return List.copyOf(levels);
    }

    private static Level createLevel(int id, Difficulty difficulty) {
        int boardSize = difficulty.boardSize();
        GridCoordinate exit = new GridCoordinate(boardSize, 4);
        int targetRow = exit.rowIndex(boardSize);
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("RR", targetRow, 0, 2, Orientation.HORIZONTAL, true));

        int nextVehicle = 0;
        for (int row = 0; row < boardSize
                && nextVehicle < difficulty.vehicleCount() - 1; row++) {
            if (row == targetRow) {
                continue;
            }
            for (int column = 0; column + 1 < boardSize
                    && nextVehicle < difficulty.vehicleCount() - 1; column += 2) {
                vehicles.add(new Vehicle(VEHICLE_IDS[nextVehicle++], row, column,
                        2, Orientation.HORIZONTAL, false));
            }
        }

        List<Obstacles.Cell> obstacleCells = new ArrayList<>();
        if (difficulty == Difficulty.HARD) {
            for (int row = 0; row < boardSize && obstacleCells.size() < 10; row++) {
                if (row == targetRow) {
                    continue;
                }
                for (int column = 0; column < boardSize && obstacleCells.size() < 10; column++) {
                    Obstacles.Cell cell = new Obstacles.Cell(row, column);
                    if (!isOccupied(vehicles, row, column)) {
                        obstacleCells.add(cell);
                    }
                }
            }
        }

        return new Level(id, "Alapértelmezett pálya — " + difficulty.displayName(),
                boardSize, ExitSide.RIGHT, exit, difficulty,
                vehicles, new Obstacles(obstacleCells));
    }

    private static boolean isOccupied(List<Vehicle> vehicles, int row, int column) {
        return vehicles.stream().anyMatch(vehicle -> occupies(vehicle, row, column));
    }

    private static boolean occupies(Vehicle vehicle, int row, int column) {
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
}
