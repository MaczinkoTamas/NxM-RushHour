package nye.progtech.model.map;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record GridCoordinate(int columnIndex, int rank) {
    private static final Pattern NOTATION = Pattern.compile("([A-Z])([1-9][0-9]*)");

    public GridCoordinate {
        if (columnIndex < 0 || columnIndex >= 26) {
            throw new IllegalArgumentException("Column must be between A and Z");
        }
        if (rank < 1) {
            throw new IllegalArgumentException("Rank must be positive");
        }
    }

    public static GridCoordinate parse(String notation) {
        if (notation == null) {
            throw new IllegalArgumentException("Coordinate must not be null");
        }
        Matcher matcher = NOTATION.matcher(notation.toUpperCase(Locale.ROOT));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid grid coordinate: " + notation);
        }
        int columnIndex = matcher.group(1).charAt(0) - 'A';
        int rank = Integer.parseInt(matcher.group(2));
        return new GridCoordinate(columnIndex, rank);
    }

    public static GridCoordinate fromGrid(
            int row,
            int columnIndex,
            int rowCount,
            int columnCount
    ) {
        if (row < 0 || row >= rowCount) {
            throw new IllegalArgumentException("Row is outside the board");
        }
        if (columnIndex < 0 || columnIndex >= columnCount) {
            throw new IllegalArgumentException("Column is outside the board");
        }
        return new GridCoordinate(columnIndex, rowCount - row);
    }

    public int rowIndex(int rowCount) {
        if (rank > rowCount) {
            throw new IllegalArgumentException("Rank is outside the board");
        }
        // A felhasználói koordináták alulról számoznak, a tömbsorok felülről.
        return rowCount - rank;
    }

    public int columnIndex(int columnCount) {
        if (columnIndex >= columnCount) {
            throw new IllegalArgumentException("Column is outside the board");
        }
        return columnIndex;
    }

    @Override
    public String toString() {
        return String.valueOf((char) ('A' + columnIndex)) + rank;
    }
}
