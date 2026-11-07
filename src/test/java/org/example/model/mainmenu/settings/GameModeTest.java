package org.example.model.mainmenu.settings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameModeTest {
    @Test
    void displaysModeNamesAndIdentifiesBotMode() {
        assertEquals("Egyszemélyes", GameMode.SINGLE_PLAYER.displayName());
        assertTrue(GameMode.SINGLE_PLAYER.includesBot());
        assertEquals("Kétszemélyes", GameMode.TWO_PLAYER.displayName());
        assertFalse(GameMode.TWO_PLAYER.includesBot());
    }
}
