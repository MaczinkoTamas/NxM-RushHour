package nye.progtech.service;

import nye.progtech.model.mainmenu.settings.GameSettings;
import nye.progtech.model.map.GameSession;
import nye.progtech.model.map.RandomAiPlayer;

import java.util.Objects;
import java.util.Optional;

/** A játékosok körének sorrendjét és az automatikus ellenfél lépését kezeli. */
public final class GameTurnManager {
    private final GameSettings settings;
    private final RandomAiPlayer aiPlayer;
    private int humanPlayerNumber = 1;

    public GameTurnManager(GameSettings settings, RandomAiPlayer aiPlayer) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.aiPlayer = Objects.requireNonNull(aiPlayer, "aiPlayer");
    }

    public String currentPlayerLabel() {
        return settings.botEnabled() ? "Játékos" : "Játékos " + humanPlayerNumber;
    }

    /** Sikeres emberi lépés után továbblépteti a kört vagy végrehajtja a bot lépését. */
    public Optional<RandomAiPlayer.AiMove> completeHumanTurn(
            GameSession session,
            boolean humanMoveSucceeded
    ) {
        Objects.requireNonNull(session, "session");
        if (!humanMoveSucceeded || session.isWon() || !session.isActive()) {
            return Optional.empty();
        }
        if (settings.botEnabled()) {
            return aiPlayer.playTurn(session);
        }
        humanPlayerNumber = humanPlayerNumber == 1 ? 2 : 1;
        return Optional.empty();
    }
}
