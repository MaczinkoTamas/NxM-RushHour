package nye.progtech;

import nye.progtech.model.highscore.DifficultyProgress;
import nye.progtech.model.highscore.HighScore;
import nye.progtech.persistence.highscore.HighScoreStore;
import nye.progtech.model.mainmenu.settings.Difficulty;
import nye.progtech.model.map.GameSession;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;

final class CampaignHighScore {
    private final PrintStream output;
    private final HighScoreStore highScoreStore;
    private final DifficultyProgress progress = new DifficultyProgress();

    CampaignHighScore(PrintStream output, HighScoreStore highScoreStore) {
        this.output = Objects.requireNonNull(output, "output");
        this.highScoreStore = Objects.requireNonNull(highScoreStore, "highScoreStore");
    }

    void registerCompletedLevel(GameSession session, String playerName) throws IOException {
        var completedRun = progress.complete(session.level(), session.steps());
        if (completedRun.isPresent()) {
            saveHighScore(completedRun.get(), playerName);
            return;
        }
        Difficulty difficulty = session.level().difficulty();
        output.println(difficulty.displayName() + " kategória: "
                + progress.completedCount(difficulty) + "/"
                + Difficulty.LEVELS_PER_DIFFICULTY + " pálya teljesítve.");
    }

    private void saveHighScore(DifficultyProgress.CompletedRun completedRun, String playerName)
            throws IOException {
        output.println();
        output.println("HIGH SCORE — " + completedRun.difficulty().displayName()
                + " kategória");
        output.println(Difficulty.LEVELS_PER_DIFFICULTY
                + " pálya teljesítve! Pontszám: " + completedRun.score());
        HighScore candidate = new HighScore(completedRun.difficulty(), playerName,
                completedRun.score());
        printHighScoreResult(candidate);
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
