package nye.progtech.model.highscore;

import nye.progtech.model.mainmenu.settings.Difficulty;
import nye.progtech.model.map.Level;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DifficultyProgress {
    private static final int POINTS_PER_LEVEL = 1_000;
    private static final int POINTS_PER_MOVE = 10;

    // Nehézségenként tárolja, hogy az egyes pályákon mi volt a legjobb pontszám.
    private final Map<Difficulty, Map<Integer, Integer>> completedLevels =
            new EnumMap<>(Difficulty.class);

    public Optional<CompletedRun> complete(Level level, int steps) {
        Objects.requireNonNull(level, "level");
        if (steps < 0) {
            throw new IllegalArgumentException("A lépésszám nem lehet negatív.");
        }

        Map<Integer, Integer> levelScores = completedLevels.computeIfAbsent(
                level.difficulty(), ignored -> new HashMap<>());
        int score = scoreForSteps(steps);
        Integer previousScore = levelScores.get(level.id());

        // Ismételt teljesítésnél csak a jobb eredményt tartja meg.
        if (previousScore == null || score > previousScore) {
            levelScores.put(level.id(), score);
        }

        if (levelScores.size() < Difficulty.LEVELS_PER_DIFFICULTY) {
            return Optional.empty();
        }

        int totalScore = 0;
        for (int levelScore : levelScores.values()) {
            totalScore += levelScore;
        }
        completedLevels.remove(level.difficulty());
        return Optional.of(new CompletedRun(level.difficulty(), totalScore));
    }

    public int completedCount(Difficulty difficulty) {
        return completedLevels.getOrDefault(difficulty, Map.of()).size();
    }

    private int scoreForSteps(int steps) {
        if (steps >= POINTS_PER_LEVEL / POINTS_PER_MOVE) {
            return 0;
        }
        return Math.max(0, POINTS_PER_LEVEL - steps * POINTS_PER_MOVE);
    }

    public record CompletedRun(Difficulty difficulty, int score) {
    }
}
