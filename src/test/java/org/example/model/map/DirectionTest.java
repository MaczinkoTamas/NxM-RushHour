package org.example.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {
    @Test
    void exposesDeltasAndRequiredOrientation() {
        assertEquals(-1, Direction.UP.rowDelta());
        assertEquals(0, Direction.UP.columnDelta());
        assertEquals(Orientation.VERTICAL, Direction.UP.orientation());
        assertEquals(1, Direction.DOWN.rowDelta());
        assertEquals(Orientation.VERTICAL, Direction.DOWN.orientation());
        assertEquals(0, Direction.LEFT.rowDelta());
        assertEquals(-1, Direction.LEFT.columnDelta());
        assertEquals(Orientation.HORIZONTAL, Direction.LEFT.orientation());
        assertEquals(1, Direction.RIGHT.columnDelta());
        assertEquals(Orientation.HORIZONTAL, Direction.RIGHT.orientation());
    }

    @Test
    void parsesMapDirectionLettersAndNames() {
        assertEquals(Direction.UP, Direction.fromNotation("u"));
        assertEquals(Direction.DOWN, Direction.fromNotation("D"));
        assertEquals(Direction.LEFT, Direction.fromNotation("left"));
        assertEquals(Direction.RIGHT, Direction.fromNotation("jobbra"));
    }
}
