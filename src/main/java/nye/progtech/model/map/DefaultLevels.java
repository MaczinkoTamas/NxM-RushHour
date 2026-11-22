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
        int rowCount = switch (difficulty) {
            case EASY -> 6;
            case MEDIUM -> 8;
            case HARD -> 10;
        };
        int columnCount = rowCount;
        GridCoordinate exit = new GridCoordinate(columnCount, 4);
        int targetRow = exit.rowIndex(rowCount);
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("RR", VehicleColor.RED, targetRow, 0, 2,
                Orientation.HORIZONTAL, TargetOwner.HUMAN));
        int aiTargetRow = 0;
        vehicles.add(new Vehicle("BB", aiTargetRow, columnCount - 2, 2,
                Orientation.HORIZONTAL, TargetOwner.AI));

        // A két céljármű mellett a nehézség által előírt számú másik autót helyezi el.
        int nextVehicle = 1;
        for (int row = 0; row < rowCount
                && nextVehicle < difficulty.vehicleCount() - 1; row++) {
            if (row == targetRow || row == aiTargetRow) {
                continue;
            }
            for (int column = 0; column + 1 < columnCount
                    && nextVehicle < difficulty.vehicleCount() - 1; column += 2) {
                String vehicleId = VEHICLE_IDS[nextVehicle];
                TargetOwner owner = nextVehicle == 0 ? TargetOwner.AI : null;
                vehicles.add(new Vehicle(vehicleId, row, column, 2,
                        Orientation.HORIZONTAL, owner));
                nextVehicle++;
            }
        }

        List<Obstacles.Cell> obstacleCells = new ArrayList<>();
        // Akadályok csak a nehéz alapértelmezett pályára kerülnek.
        if (difficulty == Difficulty.HARD) {
            for (int row = 0; row < rowCount && obstacleCells.size() < 10; row++) {
                if (row == targetRow || row == aiTargetRow) {
                    continue;
                }
                for (int column = 0; column < columnCount && obstacleCells.size() < 10; column++) {
                    Obstacles.Cell cell = new Obstacles.Cell(row, column);
                    if (!isOccupied(vehicles, row, column)) {
                        obstacleCells.add(cell);
                    }
                }
            }
        }

        return new Level(id, "Alapértelmezett pálya — " + difficulty.displayName(),
                rowCount, columnCount, ExitSide.RIGHT, exit, difficulty,
                vehicles, new Obstacles(obstacleCells));
    }

    // Megnézi, hogy a jármű valamelyik mezője elfoglalja-e a megadott cellát.
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
