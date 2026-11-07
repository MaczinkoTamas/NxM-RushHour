package org.example.model.map;

import java.util.List;

public record Obstacles(List<Cell> cells) {
    public Obstacles {
        cells = List.copyOf(cells);
    }

    public record Cell(int row, int column) {
        public GridCoordinate coordinate(int boardSize) {
            return GridCoordinate.fromGrid(row, column, boardSize);
        }
    }
}
