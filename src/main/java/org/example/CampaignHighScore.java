package org.example;

import org.example.model.highscore.DifficultyProgress;
import org.example.model.highscore.HighScore;
import org.example.model.highscore.HighScoreStore;
import org.example.model.mainmenu.settings.Difficulty;
import org.example.model.map.GameSession;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

final class CampaignHighScore {
    private final Scanner input;
    private final PrintStream output;
    private final HighScoreStore highScoreStore;
    private final DifficultyProgress progress = new DifficultyProgress();

    CampaignHighScore(Scanner input, PrintStream output, HighScoreStore highScoreStore) {
        this.input = Objects.requireNonNull(input, "input");
        this.output = Objects.requireNonNull(output, "output");
        this.highScoreStore = Objects.requireNonNull(highScoreStore, "highScoreStore");
    }

    void registerCompletedLevel(GameSession session) throws IOException {
        var completedRun = progress.complete(session.level(), session.steps());
        if (completedRun.isPresent()) {
            saveHighScore(completedRun.get());
            return;
        }
        Difficulty difficulty = session.level().difficulty();
        output.println(difficulty.displayName() + " kategória: "
                + progress.completedCount(difficulty) + "/"
                + Difficulty.LEVELS_PER_DIFFICULTY + " pálya teljesítve.");
    }

    private void saveHighScore(DifficultyProgress.CompletedRun completedRun) throws IOException {
        output.println();
        output.println("HIGH SCORE — " + completedRun.difficulty().displayName()
                + " kategória");
        output.println("Hat pálya teljesítve! Pontszám: " + completedRun.score());
        printNamePrompt();
        HighScore candidate = new HighScore(
                completedRun.difficulty(), readPlayerName(), completedRun.score());
        printHighScoreResult(candidate);
    }

    private void printNamePrompt() {
        output.print("Játékos neve: ");
        output.flush();
    }

    private String readPlayerName() {
        if (!input.hasNextLine()) {
            return "Játékos";
        }
        String playerName = input.nextLine().trim();
        return playerName.isBlank() ? "Játékos" : playerName;
    }

    private void printHighScoreResult(HighScore candidate) throws IOException {
        if (highScoreStore.recordIfBest(candidate)) {
            output.println("Új kategóriarekord!");
        } else {
            output.println("A korábbi kategóriarekord megmaradt.");
        }
        output.println("Legjobb " + candidate.difficulty().displayName() + " eredmények:");
        for (HighScore highScore : highScoreStore.ranking(candidate.difficulty())) {
            output.printf("%s — %d pont%n", highScore.playerName(), highScore.score());
        }
    }
}
