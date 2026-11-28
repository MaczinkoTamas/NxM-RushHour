package nye.progtech;

import nye.progtech.model.map.GameSession;
import nye.progtech.model.map.Level;
import nye.progtech.model.map.Obstacles;
import nye.progtech.model.map.ExitSide;
import nye.progtech.model.map.Orientation;
import nye.progtech.model.map.TargetGoal;
import nye.progtech.model.map.TargetOwner;
import nye.progtech.model.map.Vehicle;

import java.io.PrintStream;
import java.util.List;

final class BoardRenderer {
    private BoardRenderer() {
    }

    static void print(GameSession session, PrintStream output) {
        Level level = session.level();
        List<Vehicle> vehicles = session.vehicles();
        List<Obstacles.Cell> obstacles = level.obstacles().cells();
        TargetGoal humanGoal = level.targetGoal(TargetOwner.HUMAN);
        TargetGoal aiGoal = level.targetGoal(TargetOwner.AI);
        output.println();
        output.println(level.name() + " — " + level.difficulty().displayName()
                + " — lépések: " + session.steps());
        output.println("Céljárművek: emberi " + level.targetFor(TargetOwner.HUMAN).marker()
                + ", AI " + level.targetFor(TargetOwner.AI).marker());
        printColumnLabels(level, output);
        String border = "        +" + "---+".repeat(level.columnCount());
        output.println("         U (fel)");
        output.println(border);
        for (int row = 0; row < level.rowCount(); row++) {
            int rank = level.rowCount() - row;
            boolean leftExit = isExitOnRow(humanGoal, ExitSide.LEFT, row, level.rowCount())
                    || isExitOnRow(aiGoal, ExitSide.LEFT, row, level.rowCount());
            boolean rightExit = isExitOnRow(humanGoal, ExitSide.RIGHT, row, level.rowCount())
                    || isExitOnRow(aiGoal, ExitSide.RIGHT, row, level.rowCount());
            boolean directionRow = row == level.rowCount() / 2;
            output.print(directionRow ? "L " : "  ");
            output.printf("%2d ", rank);
            output.print(leftExit ? "<-  " : "   |");
            for (int column = 0; column < level.columnCount(); column++) {
                output.printf(" %s ", cellAt(vehicles, obstacles, row, column));
                boolean exitOpening = (rightExit && column == level.columnCount() - 1)
                        || (leftExit && column == 0);
                if (!exitOpening) {
                    output.print("|");
                }
            }
            if (leftExit) {
                output.print("  <- " + exitLabel(humanGoal, aiGoal, ExitSide.LEFT,
                        row, level.rowCount()));
            } else {
                output.print("   ");
            }
            if (directionRow) {
                output.print(" R");
            }
            if (rightExit) {
                output.print("  -> " + exitLabel(humanGoal, aiGoal, ExitSide.RIGHT,
                        row, level.rowCount()));
            }
            output.println();
            output.println(border);
        }
        output.println("         D (le)");
        printColumnLabels(level, output);
        output.println("Jelmagyarázat: színbetű = autó, # = akadály, nyilak = célkijáratok");
    }

    private static void printColumnLabels(Level level, PrintStream output) {
        output.print("          ");
        for (int column = 0; column < level.columnCount(); column++) {
            output.printf("%c   ", 'A' + column);
        }
        output.println();
    }

    private static boolean isExitOnRow(
            TargetGoal goal, ExitSide side, int row, int rowCount
    ) {
        return goal.exitSide() == side
                && goal.exitCoordinate().rowIndex(rowCount) == row;
    }

    private static String exitLabel(
            TargetGoal humanGoal,
            TargetGoal aiGoal,
            ExitSide side,
            int row,
            int rowCount
    ) {
        boolean humanExit = isExitOnRow(humanGoal, side, row, rowCount);
        boolean aiExit = isExitOnRow(aiGoal, side, row, rowCount);
        if (humanExit && aiExit) {
            return "kijárat (ember/AI)";
        }
        return aiExit ? "AI-kijárat" : "kijárat";
    }

    private static String cellAt(
            List<Vehicle> vehicles,
            List<Obstacles.Cell> obstacles,
            int row,
            int column
    ) {
        for (Vehicle vehicle : vehicles) {
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
        if (obstacles.contains(new Obstacles.Cell(row, column))) {
            return "#";
        }
        return " ";
    }
}
