package org.example.model.mainmenu.settings;

import java.util.Objects;

public record GameSettings(Difficulty difficulty, GameMode gameMode, Controls controls) {
    public GameSettings {
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(gameMode, "gameMode");
        Objects.requireNonNull(controls, "controls");
    }

    public GameSettings(Difficulty difficulty, GameMode gameMode) {
        this(difficulty, gameMode, Controls.WASD);
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

    public GameSettings withDifficulty(Difficulty updatedDifficulty) {
        return new GameSettings(updatedDifficulty, gameMode, controls);
    }

    public GameSettings withControls(Controls updatedControls) {
        return new GameSettings(difficulty, gameMode, updatedControls);
    }

    public GameSettings withGameMode(GameMode updatedGameMode) {
        return new GameSettings(difficulty, updatedGameMode, controls);
    }
}
