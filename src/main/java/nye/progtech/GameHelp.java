package nye.progtech;

import nye.progtech.model.mainmenu.settings.GameMode;

import java.io.PrintStream;

final class GameHelp {
    private GameHelp() {
    }

    static void print(GameMode gameMode, PrintStream output) {
        output.println("Mozgatás: move <autóazonosító> <célkoordináta>; például: move RR A5");
        output.println("Egyéb parancsok: undo [lépések], "
                + "save <név>, next (pályaválasztó), b (vissza).");
        output.println("A táblán a sorok száma és az oszlopok betűjele látható.");
        if (gameMode == GameMode.TWO_PLAYER) {
            output.println("Kétszemélyes módban sikeres lépésenként felváltva következnek a játékosok.");
        }
    }
}
