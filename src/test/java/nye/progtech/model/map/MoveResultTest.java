package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MoveResultTest {
    @Test
    void storesMovementOutcomeAndMessage() {
        MoveResult result = new MoveResult(false, 2, "Ütközés");

        assertFalse(result.successful());
        assertEquals(2, result.movedSteps());
        assertEquals("Ütközés", result.message());
    }
}
