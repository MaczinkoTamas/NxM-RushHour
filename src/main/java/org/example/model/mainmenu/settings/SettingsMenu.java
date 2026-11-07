package org.example.model.mainmenu.settings;

import org.example.model.map.Level;
import org.example.model.map.LevelLoader;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public record SettingsMenu(GameSettings settings, List<Level> levels) {
    public SettingsMenu {
        Objects.requireNonNull(settings, "settings");
        levels = List.copyOf(levels);
        if (levels.stream().noneMatch(level -> level.difficulty() == settings.difficulty())) {
            throw new IllegalArgumentException("No levels found for "
                    + settings.difficulty().displayName() + " difficulty");
        }
    }

    public static SettingsMenu load(GameSettings settings) throws IOException {
        return new SettingsMenu(settings, LevelLoader.loadLevels());
    }

    public Difficulty selectedDifficulty() {
        return settings.difficulty();
    }

    public List<Difficulty> difficulties() {
        return List.of(Difficulty.values());
    }

    public List<String> difficultyNames() {
        return difficulties().stream().map(Difficulty::displayName).toList();
    }

    public List<Level> difficultyLevels() {
        return levels.stream()
                .filter(level -> level.difficulty() == selectedDifficulty())
                .toList();
    }

    public List<String> levelNames() {
        return difficultyLevels().stream().map(Level::name).toList();
    }

    public SettingsMenu nextDifficulty() {
        int nextIndex = (selectedDifficulty().ordinal() + 1) % difficulties().size();
        return selectDifficulty(difficulties().get(nextIndex));
    }

    public SettingsMenu previousDifficulty() {
        int previousIndex = (selectedDifficulty().ordinal() + difficulties().size() - 1)
                % difficulties().size();
        return selectDifficulty(difficulties().get(previousIndex));
    }

    public SettingsMenu selectDifficulty(Difficulty difficulty) {
        return new SettingsMenu(settings.withDifficulty(difficulty), levels);
    }

    public SettingsMenu toggleControls() {
        return new SettingsMenu(settings.withControls(settings.controls().next()), levels);
    }

    public SettingsMenu toggleGameMode() {
        GameMode nextMode = settings.gameMode() == GameMode.SINGLE_PLAYER
                ? GameMode.TWO_PLAYER : GameMode.SINGLE_PLAYER;
        return new SettingsMenu(settings.withGameMode(nextMode), levels);
    }
}
