package nye.progtech.model.mainmenu;

import nye.progtech.model.GameSave;
import nye.progtech.model.mainmenu.settings.GameSettings;
import nye.progtech.ui.MainMenu;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainMenuTest {
    // Az új menü alapállapota az alapértelmezett opciókat és beállításokat tartalmazza.
    @Test
    void initializesDefaultOptionsAndSettings() {
        MainMenu menu = new MainMenu();

        assertEquals(List.of("Play", "Load game", "Settings", "Ranking", "Exit"),
                menu.options());
        assertEquals(new GameSettings(), menu.settings());
        assertTrue(menu.savedGames().isEmpty());
    }

    // Azonos nevű mentést cserélni lehet a limit elérése után is, új mentést már nem.
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

    // A konstruktor védi a mentési korlátot és nem enged hiányzó beállításokat.
    @Test
    void rejectsTooManySavesAndNullSettings() {
        List<GameSave> tooMany = new ArrayList<>();
        for (int index = 1; index <= MainMenu.MAX_SAVED_GAMES + 1; index++) {
            tooMany.add(save("Slot " + index));
        }
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
