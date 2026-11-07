package org.example;

import org.example.model.mainmenu.settings.Controls;
import org.example.model.mainmenu.settings.GameMode;
import org.example.model.map.Direction;

import java.io.PrintStream;

final class GameHelp {
    private GameHelp() {
    }

    static void print(Controls controls, GameMode gameMode, PrintStream output) {
        output.println("Térképirányok: U=fel, D=le, L=balra, R=jobbra.");
        output.println("Használat: move <autó> <U|D|L|R> <lépések>; például: move R U 2");
        output.println("Billentyűk (" + controls.displayName() + "):");
        output.printf("  %c <autó> [lépések] — fel%n", controls.keyFor(Direction.UP));
        output.printf("  %c <autó> [lépések] — le%n", controls.keyFor(Direction.DOWN));
        output.printf("  %c <autó> [lépések] — balra%n", controls.keyFor(Direction.LEFT));
        output.printf("  %c <autó> [lépések] — jobbra%n", controls.keyFor(Direction.RIGHT));
        output.println("Egyéb parancsok: move <autó> <koordináta>, undo [lépések], "
                + "save <név>, next (pályaválasztó), b (vissza).");
        output.println("A jármű színbetűjét add meg; ha az nem egyedi, használd a teljes azonosítót.");
        if (gameMode == GameMode.TWO_PLAYER) {
            output.println("Kétszemélyes módban sikeres lépésenként felváltva következnek a játékosok.");
        }
    }
}
