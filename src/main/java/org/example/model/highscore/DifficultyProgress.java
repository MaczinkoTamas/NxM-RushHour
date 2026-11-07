package org.example.model.highscore;

import org.example.model.mainmenu.settings.Difficulty;
import org.example.model.map.Level;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DifficultyProgress {
    private static final int POINTS_PER_LEVEL = 1_000;
    private static final int POINTS_PER_MOVE = 10;

    private final Map<Difficulty, Map<Integer, Integer>> completedLevels =
            new EnumMap<>(Difficulty.class);

    public Optional<CompletedRun> complete(Level level, int steps) {
        Objects.requireNonNull(level, "level");
        if (steps < 0) {
            throw new IllegalArgumentException("A lépésszám nem lehet negatív.");
        }
        Map<Integer, Integer> levelScores = completedLevels.computeIfAbsent(
                level.difficulty(), ignored -> new HashMap<>());
        levelScores.merge(level.id(), scoreForSteps(steps), Math::max);
        if (levelScores.size() < Difficulty.LEVELS_PER_DIFFICULTY) {
            return Optional.empty();
        }
        int totalScore = levelScores.values().stream().mapToInt(Integer::intValue).sum();
        completedLevels.remove(level.difficulty());
        return Optional.of(new CompletedRun(level.difficulty(), totalScore));
    }

    public int completedCount(Difficulty difficulty) {
        return completedLevels.getOrDefault(difficulty, Map.of()).size();
    }

    private int scoreForSteps(int steps) {
        return Math.max(0, POINTS_PER_LEVEL - steps * POINTS_PER_MOVE);
    }

    public record CompletedRun(Difficulty difficulty, int score) {
    }
}
