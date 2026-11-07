package org.example.model.map;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObstaclesTest {
    @Test
    void storesImmutableCellsAndConvertsCellCoordinates() {
        List<Obstacles.Cell> cells = new ArrayList<>(List.of(new Obstacles.Cell(2, 1)));
        Obstacles obstacles = new Obstacles(cells);
        cells.clear();

        assertEquals(1, obstacles.cells().size());
        assertEquals("B4", obstacles.cells().getFirst().coordinate(6).toString());
        assertThrows(UnsupportedOperationException.class,
                () -> obstacles.cells().add(new Obstacles.Cell(0, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new Obstacles.Cell(-1, 0).coordinate(6));
    }
}
