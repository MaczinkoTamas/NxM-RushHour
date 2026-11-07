package org.example;

import org.example.model.map.GameSession;
import org.example.model.map.Level;
import org.example.model.map.Obstacles;
import org.example.model.map.Orientation;
import org.example.model.map.Vehicle;

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
            output.print("L  |");
            for (int column = 0; column < level.boardSize(); column++) {
                output.printf(" %s ", cellAt(session, row, column));
                boolean exitOpening = column == level.boardSize() - 1
                        && row == level.exitCoordinate().rowIndex(level.boardSize());
                if (!exitOpening) {
                    output.print("|");
                }
            }
            output.print("  R");
            if (row == level.exitCoordinate().rowIndex(level.boardSize())) {
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
