package nye.progtech;

import nye.progtech.model.map.GameSession;
import nye.progtech.model.map.Level;
import nye.progtech.model.map.Obstacles;
import nye.progtech.model.map.Orientation;
import nye.progtech.model.map.Vehicle;
import nye.progtech.model.map.ExitSide;

import java.io.PrintStream;

final class BoardRenderer {
    private BoardRenderer() {
    }

    static void print(GameSession session, PrintStream output) {
        Level level = session.level();
        output.println();
        output.println(level.name() + " — " + level.difficulty().displayName()
                + " — lépések: " + session.steps());
        String border = "+---".repeat(level.boardSize()) + "+";
        output.println("       U (fel)");
        output.println(border);
        for (int row = 0; row < level.boardSize(); row++) {
            boolean exitRow = row == level.exitCoordinate().rowIndex(level.boardSize());
            boolean leftExit = exitRow && level.exitSide() == ExitSide.LEFT;
            output.print(leftExit ? "<-  " : "L  |");
            for (int column = 0; column < level.boardSize(); column++) {
                output.printf(" %s ", cellAt(session, row, column));
                boolean exitOpening = exitRow && ((column == level.boardSize() - 1
                        && level.exitSide() == ExitSide.RIGHT)
                        || (column == 0 && level.exitSide() == ExitSide.LEFT));
                if (!exitOpening || column > 0) {
                    output.print("|");
                }
            }
            output.print(leftExit ? "  <- kijárat" : "  R");
            if (exitRow && !leftExit) {
                output.print("  -> kijárat");
            }
            output.println();
            output.println(border);
        }
        output.println("       D (le)");
        output.println("Jelmagyarázat: színbetű = autó, # = akadály, -> = kijárat");
    }

    private static String cellAt(GameSession session, int row, int column) {
        for (Vehicle vehicle : session.vehicles()) {
            for (int part = 0; part < vehicle.length(); part++) {
                int vehicleRow = vehicle.row()
                        + (vehicle.orientation() == Orientation.VERTICAL ? part : 0);
                int vehicleColumn = vehicle.column()
                        + (vehicle.orientation() == Orientation.HORIZONTAL ? part : 0);
                if (vehicleRow == row && vehicleColumn == column) {
                    return vehicle.marker();
                }
            }
        }
        if (session.level().obstacles().cells().contains(new Obstacles.Cell(row, column))) {
            return "#";
        }
        return " ";
    }
}
