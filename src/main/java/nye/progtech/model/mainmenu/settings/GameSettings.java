package nye.progtech.model.mainmenu.settings;

import java.util.Objects;

public record GameSettings(Difficulty difficulty, GameMode gameMode) {
    public GameSettings {
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(gameMode, "gameMode");
    }

    public GameSettings(Difficulty difficulty) {
        this(difficulty, GameMode.SINGLE_PLAYER);
    }

    public GameSettings() {
        this(Difficulty.EASY, GameMode.SINGLE_PLAYER);
    }

    public boolean botEnabled() {
        return gameMode.includesBot();
    }

    // Új beállításokat ad vissza; az eredeti rekord változatlan marad.
    public GameSettings withDifficulty(Difficulty updatedDifficulty) {
        return new GameSettings(updatedDifficulty, gameMode);
    }

    public GameSettings withGameMode(GameMode updatedGameMode) {
        return new GameSettings(difficulty, updatedGameMode);
    }
}
