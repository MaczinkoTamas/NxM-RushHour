package nye.progtech.model.ranking;

public record PlayerWins(String playerName, int wins) {
    public PlayerWins {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name must not be blank");
        }
        if (wins < 0) {
            throw new IllegalArgumentException("Wins must not be negative");
        }
    }
}
