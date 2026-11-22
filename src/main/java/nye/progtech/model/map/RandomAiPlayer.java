package nye.progtech.model.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public final class RandomAiPlayer {
    private final Random random;

    public RandomAiPlayer() {
        this(new Random());
    }

    public RandomAiPlayer(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    public Optional<AiMove> playTurn(GameSession session) {
        Objects.requireNonNull(session, "session");
        if (!session.isActive() || session.isWon()) {
            return Optional.empty();
        }
        List<Vehicle> remainingVehicles = new ArrayList<>(session.vehicles());
        while (!remainingVehicles.isEmpty()) {
            Vehicle vehicle = remainingVehicles.remove(random.nextInt(remainingVehicles.size()));
            List<GameSession.LegalMove> legalMoves = session.legalMoves(vehicle.id());
            if (legalMoves.isEmpty()) {
                continue;
            }
            GameSession.LegalMove selected = legalMoves.get(random.nextInt(legalMoves.size()));
            return Optional.of(applyMove(session, selected));
        }
        return Optional.empty();
    }

    private AiMove applyMove(GameSession session, GameSession.LegalMove move) {
        MoveResult result = session.moveSteps(move.vehicleId(), move.direction(), move.distance());
        if (!result.successful()) {
            throw new IllegalStateException("AI selected an illegal move: " + result.message());
        }
        return new AiMove(move, result);
    }

    public record AiMove(GameSession.LegalMove move, MoveResult result) {
        public AiMove {
            Objects.requireNonNull(move, "move");
            Objects.requireNonNull(result, "result");
        }
    }
}
