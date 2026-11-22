package nye.progtech.model.map;

import java.util.List;

// A pályán lévő akadálycellákat változtathatatlan listaként tárolja.
public record Obstacles(List<Cell> cells) {
    public Obstacles {
        cells = List.copyOf(cells);
    }

    public record Cell(int row, int column) {
        // A belső rácskoordinátát a játékos által használt koordinátává alakítja.
        public GridCoordinate coordinate(int rowCount, int columnCount) {
            return GridCoordinate.fromGrid(row, column, rowCount, columnCount);
        }
    }
}
