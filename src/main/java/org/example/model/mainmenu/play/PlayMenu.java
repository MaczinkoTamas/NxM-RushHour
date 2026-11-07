package org.example.model.mainmenu.play;

import org.example.model.mainmenu.settings.Difficulty;
import org.example.model.map.Level;
import org.example.model.map.LevelLoader;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public record PlayMenu(
        List<Level> levels,
        Difficulty selectedDifficulty,
        int selectedIndex
) {
    public PlayMenu {
        levels = List.copyOf(levels);
        Objects.requireNonNull(selectedDifficulty, "selectedDifficulty");
        if (levelsFor(levels, selectedDifficulty).isEmpty()) {
            throw new IllegalArgumentException(
                    "The play menu must contain levels for " + selectedDifficulty.displayName());
        }
        if (selectedIndex < 0 || selectedIndex >= levelsFor(levels, selectedDifficulty).size()) {
            throw new IllegalArgumentException(
                    "Selected level index is outside the selected difficulty");
        }
    }

    public static PlayMenu load() throws IOException {
        return load(Difficulty.EASY);
    }

    public static PlayMenu load(Difficulty difficulty) throws IOException {
        return new PlayMenu(LevelLoader.loadLevels(), difficulty, 0);
    }

    public List<Difficulty> difficulties() {
        return List.of(Difficulty.values());
    }

    public List<String> difficultyNames() {
        return difficulties().stream().map(Difficulty::displayName).toList();
    }

    public List<Level> difficultyLevels() {
        return levelsFor(levels, selectedDifficulty);
    }

    public List<String> levelNames() {
        return difficultyLevels().stream().map(Level::name).toList();
    }

    public Level selectedLevel() {
        return difficultyLevels().get(selectedIndex);
    }

    public String selectedLevelName() {
        return selectedLevel().name();
    }

    public PlayMenu nextDifficulty() {
        int nextIndex = (selectedDifficulty.ordinal() + 1) % difficulties().size();
        return selectDifficulty(difficulties().get(nextIndex));
    }

    public PlayMenu previousDifficulty() {
        int previousIndex = (selectedDifficulty.ordinal() + difficulties().size() - 1)
                % difficulties().size();
        return selectDifficulty(difficulties().get(previousIndex));
    }

    public PlayMenu selectDifficulty(Difficulty difficulty) {
        return new PlayMenu(levels, difficulty, 0);
    }

    public PlayMenu nextLevel() {
        return new PlayMenu(levels, selectedDifficulty,
                (selectedIndex + 1) % difficultyLevels().size());
    }

    public PlayMenu previousLevel() {
        return new PlayMenu(levels, selectedDifficulty,
                (selectedIndex + difficultyLevels().size() - 1) % difficultyLevels().size());
    }

    public PlayMenu selectLevel(int levelId) {
        List<Level> difficultyLevels = difficultyLevels();
        for (int index = 0; index < difficultyLevels.size(); index++) {
            if (difficultyLevels.get(index).id() == levelId) {
                return new PlayMenu(levels, selectedDifficulty, index);
            }
        }
        throw new IllegalArgumentException("Level " + levelId + " is not available for "
                + selectedDifficulty.displayName() + " difficulty");
    }

    private static List<Level> levelsFor(List<Level> levels, Difficulty difficulty) {
        return levels.stream()
                .filter(level -> level.difficulty() == difficulty)
                .toList();
    }
}
