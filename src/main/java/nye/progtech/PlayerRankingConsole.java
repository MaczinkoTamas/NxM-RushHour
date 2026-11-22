package nye.progtech;

import nye.progtech.persistence.ranking.PlayerWinRepository;
import nye.progtech.model.ranking.PlayerWins;

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** A játékosranglista konzolos megjelenítését és a győzelmek mentését kezeli. */
final class PlayerRankingConsole {
    private final PrintStream output;
    private final PlayerWinRepository repository;

    PlayerRankingConsole(PrintStream output, PlayerWinRepository repository) {
        this.output = Objects.requireNonNull(output, "output");
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    /** Elment egy győzelmet; adatbázishibánál hibaüzenetet ír ki. */
    boolean recordWin(String playerName) {
        try {
            repository.recordWin(playerName);
            return true;
        } catch (SQLException exception) {
            output.println("A győzelem nem menthető az adatbázisba: " + exception.getMessage());
            return false;
        }
    }

    /** Kiírja a rangsort a repository által meghatározott sorrendben. */
    void showRanking() {
        output.println("Játékos ranglista:");
        try {
            List<PlayerWins> ranking = repository.ranking();
            if (ranking.isEmpty()) {
                output.println("Még nincs rögzített győzelem.");
                return;
            }
            for (int index = 0; index < ranking.size(); index++) {
                PlayerWins player = ranking.get(index);
                output.printf("%d. %s — %d győzelem%n", index + 1,
                        player.playerName(), player.wins());
            }
        } catch (SQLException exception) {
            output.println("A ranglista nem olvasható: " + exception.getMessage());
        }
    }
}
