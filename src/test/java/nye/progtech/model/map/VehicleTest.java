package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleTest {
    // A BB azonosító kék színt jelent; a rácspozícióból C5 koordináta lesz.
    @Test
    void derivesColorAndExposesMarkerAndCoordinate() {
        Vehicle vehicle = new Vehicle("BB", 1, 2, 3, Orientation.HORIZONTAL, false);

        assertEquals(VehicleColor.BLUE, vehicle.color());
        assertEquals("B", vehicle.marker());
        assertEquals("C5", vehicle.coordinate(6, 6).toString());
    }

    // Azonosító, pozíció, hossz, szín és tájolás nélkül nem hozható létre érvényes jármű.
    @Test
    void rejectsInvalidIdentifiersCoordinatesLengthAndNullValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("", VehicleColor.RED, 0, 0, 2, Orientation.HORIZONTAL, false));
        assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("RR", VehicleColor.RED, -1, 0, 2, Orientation.HORIZONTAL, true));
        assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("RR", VehicleColor.RED, 0, 0, 1, Orientation.HORIZONTAL, true));
        assertThrows(NullPointerException.class,
                () -> new Vehicle("RR", null, 0, 0, 2, Orientation.HORIZONTAL, true));
        assertThrows(NullPointerException.class,
                () -> new Vehicle("RR", VehicleColor.RED, 0, 0, 2, null, true));
        assertThrows(IllegalArgumentException.class,
                () -> new Vehicle("ZZ", 0, 0, 2, Orientation.HORIZONTAL, false));
    }
}
