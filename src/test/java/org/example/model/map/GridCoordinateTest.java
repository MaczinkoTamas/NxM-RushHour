package org.example.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GridCoordinateTest {
    @Test
    void parsesFormatsAndConvertsBoardCoordinates() {
        GridCoordinate coordinate = GridCoordinate.parse("f7");

        assertEquals(5, coordinate.columnIndex());
        assertEquals(7, coordinate.rank());
        assertEquals("F7", coordinate.toString());
        assertEquals(0, coordinate.rowIndex(7));
        assertEquals(5, coordinate.columnIndex(7));
        assertEquals(new GridCoordinate(1, 6), GridCoordinate.fromGrid(0, 1, 6));
    }

    @Test
    void rejectsMalformedAndOutOfBoardCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> GridCoordinate.parse(null));
        assertThrows(IllegalArgumentException.class, () -> GridCoordinate.parse("A0"));
        assertThrows(IllegalArgumentException.class, () -> GridCoordinate.parse("AA1"));
        assertThrows(IllegalArgumentException.class, () -> GridCoordinate.parse("A10000000000"));
        assertThrows(IllegalArgumentException.class, () -> new GridCoordinate(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> new GridCoordinate(0, 0));
        assertThrows(IllegalArgumentException.class, () -> GridCoordinate.fromGrid(-1, 0, 6));
        assertThrows(IllegalArgumentException.class, () -> GridCoordinate.fromGrid(0, 6, 6));
        assertThrows(IllegalArgumentException.class,
                () -> new GridCoordinate(0, 7).rowIndex(6));
        assertThrows(IllegalArgumentException.class,
                () -> new GridCoordinate(6, 1).columnIndex(6));
    }
}
