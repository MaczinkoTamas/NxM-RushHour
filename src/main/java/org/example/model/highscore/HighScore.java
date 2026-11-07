package org.example.model.highscore;

import org.example.model.mainmenu.settings.Difficulty;

import java.util.Objects;

public record HighScore(Difficulty difficulty, String playerName, int score) {
    public HighScore {
        Objects.requireNonNull(difficulty, "difficulty");
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("A játékos neve nem lehet üres.");
        }
        if (score < 0) {
            throw new IllegalArgumentException("A pontszám nem lehet negatív.");
        }
    }
}
