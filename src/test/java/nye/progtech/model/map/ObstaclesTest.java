package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObstaclesTest {
    // A bemeneti lista későbbi módosítása nem változtathatja meg az akadályokat.
    @Test
    void copiesCellsFromInputList() {
        List<Obstacles.Cell> cells = new ArrayList<>(List.of(new Obstacles.Cell(2, 1)));

        Obstacles obstacles = new Obstacles(cells);
        cells.clear();

        assertEquals(List.of(new Obstacles.Cell(2, 1)), obstacles.cells());
    }

    // Az akadályok listája lekérés után sem módosítható.
    @Test
    void doesNotAllowChangingStoredCells() {
        Obstacles obstacles = new Obstacles(List.of(new Obstacles.Cell(2, 1)));

        assertThrows(UnsupportedOperationException.class,
                () -> obstacles.cells().add(new Obstacles.Cell(0, 0)));
    }

    // A (2. sor, 1. oszlop) cella a 6x6-os táblán B4 koordinátának felel meg.
    @Test
    void convertsCellToBoardCoordinate() {
        Obstacles.Cell cell = new Obstacles.Cell(2, 1);

        assertEquals("B4", cell.coordinate(6, 6).toString());
    }

    // Táblán kívüli rácscellából nem készíthető játékoskoordináta.
    @Test
    void rejectsCellOutsideBoardWhenConvertingCoordinate() {
        assertThrows(IllegalArgumentException.class,
                () -> new Obstacles.Cell(-1, 0).coordinate(6, 6));
    }
}
