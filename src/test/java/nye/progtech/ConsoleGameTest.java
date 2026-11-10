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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleGameTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void opensPlaySelectsDifficultyAndDisplaysLevelBoard() throws IOException {
        String output = run("1\n1\n1\nmove B down 1\nb\n4\n",
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
        assertFalse(output.contains("A  B  C  D  E  F"));
        assertTrue(output.contains("lépések: 1"));
    }

    @Test
    void twoPlayerSettingAlternatesTurnsAfterSuccessfulMoves() throws IOException {
        String output = run("3\nm\nb\n1\n1\n1\nhelp\nmove B U 1\nmove B D 1\nb\nb\n4\n",
                temporaryDirectory.resolve("two-player-saves.xml"));

        assertTrue(output.contains("Játékmód beállítva: Kétszemélyes"));
        assertTrue(output.contains("Játékos 1 következik."));
        assertTrue(output.contains("Játékos 2 következik."));
        assertTrue(output.contains("Kétszemélyes módban sikeres lépésenként"));
    }

    @Test
    void settingsDifficultyIsUsedByPlayMenu() throws IOException {
        String output = run("3\n3\nb\n1\n3\nb\n4\n",
                temporaryDirectory.resolve("settings-saves.xml"));

        assertTrue(output.contains("Beállítás mentve."));
        assertTrue(output.contains("Nehéz pályái:"));
        assertTrue(output.contains("Forgalmi labirintus"));
    }

    @Test
    void settingsShowCurrentDifficultyLevelsImmediatelyAfterChangingDifficulty()
            throws IOException {
        String output = run("3\n2\nb\n4\n",
                temporaryDirectory.resolve("settings-level-list.xml"));

        assertTrue(output.contains("Közepes (aktuális)"));
        assertTrue(output.contains("Közepes pályái:"));
        assertTrue(output.contains("1. Rakparti torlódás"));
        assertTrue(output.contains("2. Konténerudvar"));
    }

    @Test
    void savesGameAndLoadsItFromMainMenu() throws IOException {
        Path saveFile = temporaryDirectory.resolve("saves.xml");
        String output = run("1\n1\n1\nsave Tesztállás\nb\nb\n2\n1\nb\n4\n", saveFile);

        assertTrue(output.contains("Játékállás elmentve: Tesztállás"));
        assertTrue(output.contains("Mentések:"));
        assertTrue(output.contains("Tesztállás"));
        assertTrue(output.contains("Nyitott garázs"));
    }

    @Test
    void helpExplainsTheCurrentControlsAndKeyboardMovesVehicles() throws IOException {
        String output = run("1\n1\n1\nhelp\nw B 1\nb\n4\n",
                temporaryDirectory.resolve("controls-saves.xml"));

        assertTrue(output.contains("Billentyűk (WASD):"));
        assertTrue(output.contains("U=fel, D=le, L=balra, R=jobbra"));
        assertTrue(output.contains("d <autó> [lépések] — jobbra"));
        assertTrue(output.contains("lépések: 1"));
    }

    @Test
    void settingsCanSwitchControlsAndTheNewKeysWorkInGameplay() throws IOException {
        String output = run("3\nc\nb\n1\n1\n1\nhelp\ni B 1\nb\n4\n",
                temporaryDirectory.resolve("alternate-controls-saves.xml"));

        assertTrue(output.contains("Irányítás beállítva: IJKL"));
        assertTrue(output.contains("Billentyűk (IJKL):"));
        assertTrue(output.contains("l <autó> [lépések] — jobbra"));
        assertTrue(output.contains("lépések: 1"));
    }

    @Test
    void returningFromAGameKeepsThePlayerInTheLevelSelection() throws IOException {
        String output = run("1\n1\n1\nb\n2\nb\nb\n4\n",
                temporaryDirectory.resolve("continuous-play-saves.xml"));

        assertTrue(output.contains("Kettős torlasz"));
        assertTrue(output.contains("Könnyű pályái:"));
    }

    @Test
    void vehicleCanMoveWithShortCommandAndKeepsPartialMovementBeforeCollision()
            throws IOException {
        String output = run("1\n1\n1\nB down 5\nb\nb\n4\n",
                temporaryDirectory.resolve("flexible-movement-saves.xml"));

        assertTrue(output.contains("3 lépés megtéve; tovább nem mozdítható."));
        assertTrue(output.contains("lépések: 3"));
    }

    @Test
    void mapDirectionLettersMoveVehiclesInTheMarkedDirections() throws IOException {
        String output = run("1\n1\n1\nmove B U 1\nmove B D 1\nb\nb\n4\n",
                temporaryDirectory.resolve("map-directions-saves.xml"));

        assertTrue(output.split("1 lépés sikeresen megtéve.", -1).length - 1 >= 2);
        assertTrue(output.contains("lépések: 2"));
    }

    @Test
    void reportsProgressUnderTheDifficultyOfTheCompletedLevel() throws IOException {
        String output = run("1\n1\n1\nB D 1\nG D 1\nY D 1\nO D 1\nR R 5\nnext\nb\nb\n4\n",
                temporaryDirectory.resolve("difficulty-progress-saves.xml"));

        assertTrue(output.contains("Könnyű kategória:"));
        assertTrue(output.contains("1/6 pálya teljesítve."));
        assertFalse(output.contains("Közepes kategória:"));
    }

    private String run(String commands, Path saveFile) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Scanner input = new Scanner(new StringReader(commands));
             PrintStream printStream = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            new ConsoleGame(input, printStream, saveFile).run();
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
