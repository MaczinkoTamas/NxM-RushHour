package org.example.model.mainmenu;

import org.example.model.GameSave;
import org.example.model.mainmenu.play.PlayMenu;
import org.example.model.mainmenu.settings.GameSettings;
import org.example.model.mainmenu.settings.SettingsMenu;

import java.io.IOException;
import java.util.List;

public record MainMenu(
        String title,
        List<String> options,
        List<GameSave> savedGames,
        GameSettings settings
) {
    public static final int MAX_SAVED_GAMES = 6;

    public MainMenu() {
        this("Main Menu", List.of("Play", "Load game", "Settings", "Exit"),
                List.of(), new GameSettings());
    }

    public MainMenu(String title, List<String> options) {
        this(title, options, List.of(), new GameSettings());
    }

    public MainMenu(String title, List<String> options, List<GameSave> savedGames) {
        this(title, options, savedGames, new GameSettings());
    }

    public MainMenu {
        options = List.copyOf(options);
        savedGames = List.copyOf(savedGames);
        if (savedGames.size() > MAX_SAVED_GAMES) {
            throw new IllegalArgumentException(
                    "At most " + MAX_SAVED_GAMES + " game saves are allowed");
        }
        if (settings == null) {
            throw new IllegalArgumentException("Settings must not be null");
        }
    }

    public MainMenu withSavedGame(GameSave save) {
        List<GameSave> updatedSaves = new java.util.ArrayList<>(savedGames);
        int existingIndex = -1;
        for (int index = 0; index < updatedSaves.size(); index++) {
            if (updatedSaves.get(index).name().equalsIgnoreCase(save.name())) {
                existingIndex = index;
                break;
            }
        }
        if (existingIndex >= 0) {
            updatedSaves.set(existingIndex, save);
        } else if (updatedSaves.size() < MAX_SAVED_GAMES) {
            updatedSaves.add(save);
        } else {
            throw new IllegalStateException("A főmenü legfeljebb "
                    + MAX_SAVED_GAMES + " mentést tárolhat.");
        }
        return new MainMenu(title, options, updatedSaves, settings);
    }

    public PlayMenu openPlayMenu() throws IOException {
        return PlayMenu.load(settings.difficulty());
    }

    public SettingsMenu openSettingsMenu() throws IOException {
        return SettingsMenu.load(settings);
    }

    public MainMenu withSettings(SettingsMenu updatedSettingsMenu) {
        return new MainMenu(title, options, savedGames, updatedSettingsMenu.settings());
    }
}
