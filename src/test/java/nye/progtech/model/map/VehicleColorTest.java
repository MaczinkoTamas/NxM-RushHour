package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleColorTest {
    @Test
    void mapsEveryColorMarkerAndDisplayName() {
        for (VehicleColor color : VehicleColor.values()) {
            assertEquals(color, VehicleColor.fromMarker(color.marker()));
            assertEquals(color, VehicleColor.fromMarker(color.marker().repeat(2)));
            assertEquals(1, color.marker().length());
            assertFalse(color.displayName().isBlank());
        }
    }

    @Test
    void rejectsUnknownMarker() {
        assertThrows(IllegalArgumentException.class, () -> VehicleColor.fromMarker("ZZ"));
    }
}
