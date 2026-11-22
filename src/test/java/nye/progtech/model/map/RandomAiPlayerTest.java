package nye.progtech.model.map;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RandomAiPlayerTest {
    // A befecskendezett véletlenforrás teszi megismételhetővé a kiválasztott AI-lépést.
    @Test
    void picksRandomVehicleAndThenOneOfItsLegalMoves() {
        Random random = mock(Random.class);
        when(random.nextInt(anyInt())).thenReturn(1, 0);
        GameSession session = new GameSession(LevelTestFixtures.openLevel());
        RandomAiPlayer aiPlayer = new RandomAiPlayer(random);

        Optional<RandomAiPlayer.AiMove> result = aiPlayer.playTurn(session);

        assertTrue(result.isPresent());
        assertEquals("BB", result.orElseThrow().move().vehicleId());
        assertEquals(Direction.LEFT, result.orElseThrow().move().direction());
        assertEquals(1, session.steps());
        assertEquals(3, session.vehicles().get(1).column());
        verify(random, times(2)).nextInt(anyInt());
    }

    // Lezárt pályán az AI nem választ újabb járművet vagy lépést.
    @Test
    void doesNotMoveAfterTheSessionIsClosed() {
        Random random = mock(Random.class);
        GameSession session = new GameSession(LevelTestFixtures.openLevel());
        session.leaveLevel();

        assertFalse(new RandomAiPlayer(random).playTurn(session).isPresent());
        verify(random, times(0)).nextInt(anyInt());
    }
}
