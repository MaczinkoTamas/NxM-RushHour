package org.example;

import org.example.model.GameSave;
import org.example.model.GameSaveStore;
import org.example.model.highscore.HighScoreStore;
import org.example.model.mainmenu.MainMenu;
import org.example.model.mainmenu.play.PlayMenu;
import org.example.model.mainmenu.settings.Difficulty;
import org.example.model.mainmenu.settings.GameMode;
import org.example.model.mainmenu.settings.SettingsMenu;
import org.example.model.map.GameSession;
import org.example.model.map.Level;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public final class ConsoleGame {
    private static final String BACK_COMMAND = "b";

    private final Scanner input;
    private final PrintStream output;
    private final GameSaveStore saveStore;
    private final CampaignHighScore campaignHighScore;
    private MainMenu mainMenu;

    public ConsoleGame(Scanner input, PrintStream output, Path saveFile) {
        this(input, output, new GameSaveStore(saveFile),
                new HighScoreStore(saveFile.resolveSibling("highscores.xml")), new MainMenu());
    }

    ConsoleGame(
            Scanner input,
            PrintStream output,
            GameSaveStore saveStore,
            HighScoreStore highScoreStore,
            MainMenu mainMenu
    ) {
        this.input = Objects.requireNonNull(input, "input");
        this.output = Objects.requireNonNull(output, "output");
        this.saveStore = Objects.requireNonNull(saveStore, "saveStore");
        this.campaignHighScore = new CampaignHighScore(input, output,
                Objects.requireNonNull(highScoreStore, "highScoreStore"));
        this.mainMenu = Objects.requireNonNull(mainMenu, "mainMenu");
    }

    public void run() throws IOException {
        boolean running = true;
        while (running && input.hasNextLine()) {
            printMainMenu();
            String choice = readLine().trim();
            switch (choice) {
                case "1" -> openPlayMenu();
                case "2" -> loadSavedGame();
                case "3" -> openSettingsMenu();
                case "4" -> running = false;
                default -> output.println("Érvénytelen menüpont.");
            }
        }
        output.println("Viszlát!");
    }

    private void printMainMenu() {
        output.println();
        output.println(mainMenu.title());
        for (int index = 0; index < mainMenu.options().size(); index++) {
            output.printf("%d. %s%n", index + 1, mainMenu.options().get(index));
        }
        printPrompt("Választás (szám + Enter): ");
    }

    private void openPlayMenu() throws IOException {
        PlayMenu playMenu = mainMenu.openPlayMenu();
        while (input.hasNextLine()) {
            printDifficulties(playMenu);
            String choice = readLine().trim();
            if (BACK_COMMAND.equalsIgnoreCase(choice)) {
                return;
            }
            Difficulty difficulty = parseDifficulty(choice);
            if (difficulty == null) {
                output.println("Érvénytelen nehézségi szint.");
                continue;
            }
            playMenu = playMenu.selectDifficulty(difficulty);
            chooseLevel(playMenu);
            return;
        }
    }

    private void printDifficulties(PlayMenu playMenu) {
        output.println("Nehézségi szint:");
        for (int index = 0; index < playMenu.difficulties().size(); index++) {
            Difficulty difficulty = playMenu.difficulties().get(index);
            output.printf("%d. %s%n", index + 1, difficulty.displayName());
        }
        output.println("b. Vissza");
        printPrompt("Választás (szám + Enter): ");
    }

    private Difficulty parseDifficulty(String choice) {
        try {
            int index = Integer.parseInt(choice) - 1;
            return Difficulty.values()[index];
        } catch (NumberFormatException | IndexOutOfBoundsException exception) {
            return null;
        }
    }

    private void chooseLevel(PlayMenu playMenu) throws IOException {
        while (input.hasNextLine()) {
            printLevels(playMenu);
            String choice = readLine().trim();
            if (BACK_COMMAND.equalsIgnoreCase(choice)) {
                return;
            }
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index < 0 || index >= playMenu.difficultyLevels().size()) {
                    output.println("Nincs ilyen sorszámú pálya.");
                    continue;
                }
                Level level = playMenu.difficultyLevels().get(index);
                GameSession session = playLevel(new GameSession(level));
                if (session.isWon()) {
                    campaignHighScore.registerCompletedLevel(session);
                }
            } catch (NumberFormatException exception) {
                output.println("Adj meg egy pályasorszámot vagy b-t a visszalépéshez.");
            }
        }
    }

    private void printLevels(PlayMenu playMenu) {
        output.println();
        output.println(playMenu.selectedDifficulty().displayName() + " pályái:");
        List<Level> levels = playMenu.difficultyLevels();
        for (int index = 0; index < levels.size(); index++) {
            Level level = levels.get(index);
            output.printf("%d. %s%n", index + 1, level.name());
        }
        output.println("b. Vissza");
        printPrompt("Pálya kiválasztása (szám + Enter): ");
    }

    private void openSettingsMenu() throws IOException {
        SettingsMenu settingsMenu = mainMenu.openSettingsMenu();
        while (input.hasNextLine()) {
            output.println("Nehézségi szint beállítása:");
            for (int index = 0; index < settingsMenu.difficulties().size(); index++) {
                Difficulty difficulty = settingsMenu.difficulties().get(index);
                String selected = difficulty == settingsMenu.selectedDifficulty() ? " (aktuális)" : "";
                output.printf("%d. %s%s%n", index + 1, difficulty.displayName(), selected);
            }
            printSettingsLevels(settingsMenu);
            output.println("Játékmód: " + settingsMenu.settings().gameMode().displayName());
            output.println("m. Egyszemélyes / kétszemélyes váltása");
            output.println("Controls: " + settingsMenu.settings().controls().displayName());
            output.println("c. Billentyűk váltása (WASD / IJKL)");
            output.println("b. Vissza");
            printPrompt("Választás (szám + Enter): ");

            String choice = readLine().trim();
            if (BACK_COMMAND.equalsIgnoreCase(choice)) {
                return;
            }
            if ("c".equalsIgnoreCase(choice)) {
                settingsMenu = settingsMenu.toggleControls();
                mainMenu = mainMenu.withSettings(settingsMenu);
                output.println("Irányítás beállítva: "
                        + settingsMenu.settings().controls().displayName());
                continue;
            }
            if ("m".equalsIgnoreCase(choice)) {
                settingsMenu = settingsMenu.toggleGameMode();
                mainMenu = mainMenu.withSettings(settingsMenu);
                output.println("Játékmód beállítva: "
                        + settingsMenu.settings().gameMode().displayName());
                continue;
            }
            Difficulty difficulty = parseDifficulty(choice);
            if (difficulty == null) {
                output.println("Érvénytelen nehézségi szint.");
                continue;
            }
            settingsMenu = settingsMenu.selectDifficulty(difficulty);
            mainMenu = mainMenu.withSettings(settingsMenu);
            output.println("Beállítás mentve.");
        }
    }

    private void printSettingsLevels(SettingsMenu settingsMenu) {
        output.println(settingsMenu.selectedDifficulty().displayName() + " pályái:");
        for (int index = 0; index < settingsMenu.levelNames().size(); index++) {
            output.printf("%d. %s%n", index + 1, settingsMenu.levelNames().get(index));
        }
    }

    private void loadSavedGame() throws IOException {
        List<GameSave> saves = saveStore.loadAll();
        if (saves.isEmpty()) {
            output.println("Nincs betölthető mentés.");
            return;
        }
        output.println("Mentések:");
        for (int index = 0; index < saves.size(); index++) {
            output.printf("%d. %s%n", index + 1, saves.get(index).name());
        }
        output.println("b. Vissza");
        printPrompt("Betöltendő mentés (szám + Enter): ");
        if (!input.hasNextLine()) {
            return;
        }
        String choice = readLine().trim();
        if (BACK_COMMAND.equalsIgnoreCase(choice)) {
            return;
        }
        try {
            int index = Integer.parseInt(choice) - 1;
            if (index < 0 || index >= saves.size()) {
                output.println("Nincs ilyen sorszámú mentés.");
                return;
            }
            playLevel(saveStore.loadSession(saves.get(index).name()));
        } catch (NumberFormatException exception) {
            output.println("Érvénytelen mentéssorszám.");
        }
    }

    private GameSession playLevel(GameSession session) throws IOException {
        boolean winAnnounced = false;
        int currentPlayer = 1;
        GameCommandHandler commandHandler = new GameCommandHandler(
                output, saveStore, mainMenu.settings());
        while (session.isActive() && input.hasNextLine()) {
            if (mainMenu.settings().gameMode() == GameMode.TWO_PLAYER) {
                output.println("Játékos " + currentPlayer + " következik.");
            }
            BoardRenderer.print(session, output);
            if (session.isWon()) {
                if (!winAnnounced) {
                    output.println("Gratulálok, megoldottad a pályát!");
                    winAnnounced = true;
                }
                output.println("A pályaválasztóhoz: next; lépések visszavonása: undo; kilépés: b.");
            } else {
                output.println("Írd be: help — irányítás és parancsok");
            }
            printPrompt(gamePrompt(currentPlayer));
            int stepsBeforeCommand = session.steps();
            commandHandler.handle(session, readLine().trim());
            if (mainMenu.settings().gameMode() == GameMode.TWO_PLAYER
                    && session.steps() > stepsBeforeCommand && !session.isWon()) {
                currentPlayer = currentPlayer == 1 ? 2 : 1;
            }
            if (!session.isWon()) {
                winAnnounced = false;
            }
        }
        return session;
    }

    private String gamePrompt(int currentPlayer) {
        if (mainMenu.settings().gameMode() == GameMode.TWO_PLAYER) {
            return "Parancs (Játékos " + currentPlayer + " / help / Enter): ";
        }
        return "Parancs (help / Enter): ";
    }

    private void printPrompt(String prompt) {
        output.print(prompt);
        output.flush();
    }

    private String readLine() {
        String line = input.nextLine();
        output.println();
        return line;
    }

}
