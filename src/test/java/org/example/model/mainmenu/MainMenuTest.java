package org.example.model.mainmenu;

import org.example.model.GameSave;
import org.example.model.mainmenu.settings.GameSettings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MainMenuTest {
    @Test
    void initializesDefaultOptionsAndSettings() {
        MainMenu menu = new MainMenu();

        assertEquals(List.of("Play", "Load game", "Settings", "Exit"), menu.options());
        assertEquals(new GameSettings(), menu.settings());
        assertEquals(0, menu.savedGames().size());
    }

    @Test
    void addsAndReplacesSavesUpToTheConfiguredLimit() {
        MainMenu menu = new MainMenu();
        for (int index = 1; index <= MainMenu.MAX_SAVED_GAMES; index++) {
            menu = menu.withSavedGame(save("Slot " + index));
        }

        MainMenu replaced = menu.withSavedGame(save("slot 1"));
        assertEquals(MainMenu.MAX_SAVED_GAMES, replaced.savedGames().size());
        assertEquals("slot 1", replaced.savedGames().getFirst().name());
        assertThrows(IllegalStateException.class,
                () -> replaced.withSavedGame(save("extra")));
    }

    @Test
    void rejectsTooManySavesAndNullSettings() {
        List<GameSave> tooMany = java.util.stream.IntStream.rangeClosed(
                        1, MainMenu.MAX_SAVED_GAMES + 1)
                .mapToObj(index -> save("Slot " + index))
                .toList();
        assertThrows(IllegalArgumentException.class, () -> new MainMenu(
                "Main Menu", List.of(), tooMany, new GameSettings()));
        assertThrows(IllegalArgumentException.class,
                () -> new MainMenu("Main Menu", List.of(), List.of(), null));
    }

    private GameSave save(String name) {
        return new GameSave(name, 1, 0,
                List.of(new GameSave.VehiclePosition("RR", 0, 0)));
    }
}
