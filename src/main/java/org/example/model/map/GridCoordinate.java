package org.example.model.map;

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

    public static GridCoordinate fromGrid(int row, int columnIndex, int boardSize) {
        if (row < 0 || row >= boardSize) {
            throw new IllegalArgumentException("Row is outside the board");
        }
        if (columnIndex < 0 || columnIndex >= boardSize) {
            throw new IllegalArgumentException("Column is outside the board");
        }
        return new GridCoordinate(columnIndex, boardSize - row);
    }

    public int rowIndex(int boardSize) {
        if (rank > boardSize) {
            throw new IllegalArgumentException("Rank is outside the board");
        }
        return boardSize - rank;
    }

    public int columnIndex(int boardSize) {
        if (columnIndex >= boardSize) {
            throw new IllegalArgumentException("Column is outside the board");
        }
        return columnIndex;
    }

    @Override
    public String toString() {
        return String.valueOf((char) ('A' + columnIndex)) + rank;
    }
}
