package nye.progtech.model.map;

public enum Direction {
    UP(-1, 0, Orientation.VERTICAL),
    DOWN(1, 0, Orientation.VERTICAL),
    LEFT(0, -1, Orientation.HORIZONTAL),
    RIGHT(0, 1, Orientation.HORIZONTAL);

    private final int rowDelta;
    private final int columnDelta;
    private final Orientation orientation;

    Direction(int rowDelta, int columnDelta, Orientation orientation) {
        this.rowDelta = rowDelta;
        this.columnDelta = columnDelta;
        this.orientation = orientation;
    }

    public int rowDelta() {
        return rowDelta;
    }

    public int columnDelta() {
        return columnDelta;
    }

    public Orientation orientation() {
        return orientation;
    }

    public static Direction fromNotation(String notation) {
        if (notation == null) {
            return null;
        }
        return switch (notation.trim().toUpperCase()) {
            case "U", "UP", "FEL" -> UP;
            case "D", "DOWN", "LE" -> DOWN;
            case "L", "LEFT", "BALRA" -> LEFT;
            case "R", "RIGHT", "JOBBRA" -> RIGHT;
            default -> null;
        };
    }
}
