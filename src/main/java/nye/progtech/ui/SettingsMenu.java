package nye.progtech.ui;

import nye.progtech.model.mainmenu.settings.Difficulty;
import nye.progtech.model.mainmenu.settings.GameMode;
import nye.progtech.model.mainmenu.settings.GameSettings;
import nye.progtech.model.map.Level;
import nye.progtech.model.map.LevelLoader;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public record SettingsMenu(GameSettings settings, List<Level> levels) {
    public SettingsMenu {
        Objects.requireNonNull(settings, "settings");
        levels = List.copyOf(levels);

        // A menü csak akkor érvényes, ha van pálya a beállított nehézséghez.
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

    // A nehézségek végén a választás visszaugrik az elsőre.
    public SettingsMenu nextDifficulty() {
        int nextIndex = (selectedDifficulty().ordinal() + 1) % difficulties().size();
        return selectDifficulty(difficulties().get(nextIndex));
    }

    // Az első nehézség előtt a választás az utolsóra ugrik.
    public SettingsMenu previousDifficulty() {
        int previousIndex = (selectedDifficulty().ordinal() + difficulties().size() - 1)
                % difficulties().size();
        return selectDifficulty(difficulties().get(previousIndex));
    }

    public SettingsMenu selectDifficulty(Difficulty difficulty) {
        return new SettingsMenu(settings.withDifficulty(difficulty), levels);
    }

    // Egyszemélyes és kétszemélyes mód között vált.
    public SettingsMenu toggleGameMode() {
        GameMode nextMode = settings.gameMode() == GameMode.SINGLE_PLAYER
                ? GameMode.TWO_PLAYER : GameMode.SINGLE_PLAYER;
        return new SettingsMenu(settings.withGameMode(nextMode), levels);
    }
}
