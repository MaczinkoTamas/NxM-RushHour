package nye.progtech;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Random;

import nye.progtech.persistence.highscore.HighScoreStore;
import nye.progtech.ui.MainMenu;
import nye.progtech.model.map.RandomAiPlayer;
import nye.progtech.persistence.ranking.JdbcPlayerWinRepository;
import nye.progtech.persistence.GameSaveStore;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleGameTest {
    @TempDir
    Path temporaryDirectory;

    // Elindítja a könnyű pályát, mozgat egy autót, majd ellenőrzi a tábla és az irányjelölések kiírását.
    @Test
    void opensPlaySelectsDifficultyAndDisplaysLevelBoard() throws IOException {
        String output = run("1\n1\n1\nAnna\nmove BB C4\nb\n5\n",
                temporaryDirectory.resolve("play-saves.xml"));

        assertTrue(output.contains("Nehézségi szint:"));
        assertTrue(output.contains("Könnyű pályái:"));
        assertTrue(output.contains("Nyitott garázs"));
        assertTrue(output.contains("| R |"));
        assertTrue(output.contains("+---+---+---+---+---+---+"));
        assertTrue(output.contains("-> kijárat"));
        assertTrue(output.contains("U (fel)"));
        assertTrue(output.contains("L "));
        assertTrue(output.contains(" R"));
        assertFalse(output.contains("L / R"));
        assertTrue(output.contains("D (le)"));
        assertTrue(output.contains("# = akadály"));
        assertFalse(output.contains("Játékos 1 következik."));
        assertTrue(output.contains("A  B  C  D  E  F"));
        assertTrue(output.contains(" 6 "));
        assertTrue(output.contains("AI-lépés"));
        assertTrue(output.contains("lépések: 2"));
    }

    // A súgó nem vált játékost; csak a két sikeres mozgatás után kerül sorra a másik játékos.
    @Test
    void twoPlayerSettingAlternatesTurnsAfterSuccessfulMoves() throws IOException {
        String output = run("3\nm\nb\n1\n1\n1\nAnna\nhelp\nmove BB C4\nmove BB C5\nb\nb\n5\n",
                temporaryDirectory.resolve("two-player-saves.xml"));

        assertTrue(output.contains("Játékmód beállítva: Kétszemélyes"));
        assertTrue(output.contains("Játékos 1 következik."));
        assertTrue(output.contains("Játékos 2 következik."));
        assertTrue(output.contains("Kétszemélyes módban sikeres lépésenként"));
    }

    // A beállításokban kiválasztott nehézség határozza meg a később megjelenő pályákat.
    @Test
    void settingsDifficultyIsUsedByPlayMenu() throws IOException {
        String output = run("3\n3\nb\n1\n3\nb\n5\n",
                temporaryDirectory.resolve("settings-saves.xml"));

        assertTrue(output.contains("Beállítás mentve."));
        assertTrue(output.contains("Nehéz pályái:"));
        assertTrue(output.contains("Forgalmi labirintus"));
    }

    // A nehézség kiválasztása után a menü azonnal az új kategória pályáit mutatja.
    @Test
    void settingsShowCurrentDifficultyLevelsImmediatelyAfterChangingDifficulty()
            throws IOException {
        String output = run("3\n2\nb\n5\n",
                temporaryDirectory.resolve("settings-level-list.xml"));

        assertTrue(output.contains("Közepes (aktuális)"));
        assertTrue(output.contains("Közepes pályái:"));
        assertTrue(output.contains("1. Rakparti torlódás"));
        assertTrue(output.contains("2. Konténerudvar"));
    }

    // A játékállás névvel elmenthető, majd a főmenüből kiválasztva visszatölthető.
    @Test
    void savesGameAndLoadsItFromMainMenu() throws IOException {
        Path saveFile = temporaryDirectory.resolve("saves.xml");
        String output = run("1\n1\n1\nAnna\nsave Tesztállás\nb\nb\n2\n1\nAnna\nb\n5\n", saveFile);

        assertTrue(output.contains("Játékállás elmentve: Tesztállás"));
        assertTrue(output.contains("Mentések:"));
        assertTrue(output.contains("Tesztállás"));
        assertTrue(output.contains("Nyitott garázs"));
    }

    // A súgó az aktuális billentyűkiosztást írja ki, és az egyik billentyűvel mozgás végezhető.
    @Test
    void helpDocumentsTheSingleCoordinateMoveSyntax() throws IOException {
        String output = run("1\n1\n1\nAnna\nhelp\nmove GG D4\nb\n5\n",
                temporaryDirectory.resolve("controls-saves.xml"));

        assertTrue(output.contains("move <autóazonosító> <célkoordináta>"));
        assertTrue(output.contains("move RR A5"));
        assertTrue(output.contains("AI-lépés"));
    }

    // Az IJKL kiosztás menüből bekapcsolható, és a játékban az új billentyű működik.
    @Test
    void settingsUseTheSameMoveCommandSyntaxAsGameplay() throws IOException {
        String output = run("3\nb\n1\n1\n1\nAnna\nhelp\nmove GG D4\nb\n5\n",
                temporaryDirectory.resolve("alternate-controls-saves.xml"));
        assertTrue(output.contains("move <autóazonosító> <célkoordináta>"));
        assertTrue(output.contains("AI-lépés"));
    }

    // A pálya elhagyása után a játékos a pályaválasztó menüben marad.
    @Test
    void returningFromAGameKeepsThePlayerInTheLevelSelection() throws IOException {
        String output = run("1\n1\n1\nAnna\nb\n2\nb\nb\n5\n",
                temporaryDirectory.resolve("continuous-play-saves.xml"));

        assertTrue(output.contains("Kettős torlasz"));
        assertTrue(output.contains("Könnyű pályái:"));
    }

    // Ha a kért táv akadályba ütközne, a program elutasítja a teljes mozgást.
    @Test
    void rejectsAnEntireCoordinateMoveWhenItsPathIsBlocked() throws IOException {
        String output = run("1\n1\n1\nAnna\nmove BB C1\nb\nb\n5\n",
                temporaryDirectory.resolve("blocked-movement-saves.xml"));

        assertTrue(output.contains("A mozg"));
        assertFalse(output.contains("4 lépés sikeresen megtéve."));
    }

    // A térképen jelölt U és D irányparancsokkal ugyanaz a jármű fel- és lefelé mozgatható.
    @Test
    void rejectsTheRemovedDirectionCommandSyntax() throws IOException {
        String output = run("1\n1\n1\nAnna\nX U 1\nmove RR U 2\nb\nb\n5\n",
                temporaryDirectory.resolve("map-directions-saves.xml"));

        assertTrue(output.contains("Ismeretlen parancs."));
    }

    // A teljesített pálya a saját nehézségi kategóriájának haladását növeli.
    @Test
    void doesNotCountAnUnfinishedLevelAsCampaignProgress() throws IOException {
        String output = run("3\nm\nb\n1\n1\n1\nAnna\nmove RR C3\nb\nb\n5\n",
                temporaryDirectory.resolve("difficulty-progress-saves.xml"));

        assertFalse(output.contains("1/6 pálya teljesítve."));
        assertFalse(output.contains("Gratulálok, megoldottad a pályát!"));
    }

    // A játékot szimulált billentyűzettel futtatja, a kimenetet pedig memóriában rögzíti.
    private String run(String commands, Path saveFile) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Scanner input = new Scanner(new StringReader(commands));
             PrintStream printStream = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            new ConsoleGame(input, printStream,
                    new GameSaveStore(saveFile),
                    new HighScoreStore(saveFile.resolveSibling("highscores.xml")),
                    new MainMenu(),
                    new RandomAiPlayer(new Random(0)),
                    new JdbcPlayerWinRepository(saveFile.resolveSibling("players"))).run();
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
