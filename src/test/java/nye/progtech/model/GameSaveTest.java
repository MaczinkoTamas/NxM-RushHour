package nye.progtech.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameSaveTest {
    // A mentés a bemeneti pozíciólista későbbi módosítása után is megőrzi az adatait.
    @Test
    void copiesVehiclePositionsFromInputList() {
        List<GameSave.VehiclePosition> positions = new ArrayList<>(
                List.of(new GameSave.VehiclePosition("RR", 0, 0)));
        GameSave save = new GameSave("My save", 1, 2, positions);
        positions.clear();

        assertEquals(1, save.vehiclePositions().size());
    }

    // A mentésen keresztül visszakapott pozíciólista nem módosítható.
    @Test
    void doesNotAllowChangingSavedVehiclePositions() {
        GameSave save = new GameSave("My save", 1, 2,
                List.of(new GameSave.VehiclePosition("RR", 0, 0)));

        assertThrows(UnsupportedOperationException.class,
                () -> save.vehiclePositions().clear());
    }

    // A mentés neve kötelező, pályaazonosítója pozitív, lépésszáma nem negatív.
    @Test
    void rejectsInvalidSaveNameLevelIdAndStepCount() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameSave(" ", 1, 0, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new GameSave("x", 0, 0, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new GameSave("x", 1, -1, List.of()));
    }

    // A járműazonosító nem lehet null vagy üres.
    @Test
    void rejectsNullOrBlankVehicleId() {
        assertThrows(NullPointerException.class,
                () -> new GameSave.VehiclePosition(null, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new GameSave.VehiclePosition(" ", 0, 0));
    }

    // A jármű rácspozíciója egyik tengely mentén sem lehet negatív.
    @Test
    void rejectsNegativeVehiclePosition() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameSave.VehiclePosition("RR", -1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new GameSave.VehiclePosition("RR", 0, -1));
    }
}
