package nye.progtech;

import nye.progtech.model.GameSave;
import nye.progtech.persistence.GameSaveStore;
import nye.progtech.persistence.highscore.HighScoreStore;
import nye.progtech.ui.MainMenu;
import nye.progtech.ui.PlayMenu;
import nye.progtech.model.mainmenu.settings.Difficulty;
import nye.progtech.ui.SettingsMenu;
import nye.progtech.model.map.GameSession;
import nye.progtech.model.map.Level;
import nye.progtech.model.map.RandomAiPlayer;
import nye.progtech.model.map.TargetOwner;
import nye.progtech.persistence.ranking.JdbcPlayerWinRepository;
import nye.progtech.persistence.ranking.PlayerWinRepository;
import nye.progtech.service.GameTurnManager;

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
    private final RandomAiPlayer aiPlayer;
    private final PlayerRankingConsole playerRankingConsole;
    private MainMenu mainMenu;

    public ConsoleGame(Scanner input, PrintStream output, Path saveFile) {
        this(input, output, new GameSaveStore(saveFile),
                new HighScoreStore(saveFile.resolveSibling("highscores.xml")), new MainMenu(),
                new RandomAiPlayer(), new JdbcPlayerWinRepository(saveFile.resolveSibling("players")));
    }

    ConsoleGame(
            Scanner input,
            PrintStream output,
            GameSaveStore saveStore,
            HighScoreStore highScoreStore,
            MainMenu mainMenu,
            RandomAiPlayer aiPlayer,
            PlayerWinRepository playerWinRepository
    ) {
        this.input = Objects.requireNonNull(input, "input");
        this.output = Objects.requireNonNull(output, "output");
        this.saveStore = Objects.requireNonNull(saveStore, "saveStore");
        this.campaignHighScore = new CampaignHighScore(output,
                Objects.requireNonNull(highScoreStore, "highScoreStore"));
        this.mainMenu = Objects.requireNonNull(mainMenu, "mainMenu");
        this.aiPlayer = Objects.requireNonNull(aiPlayer, "aiPlayer");
        this.playerRankingConsole = new PlayerRankingConsole(
                output, Objects.requireNonNull(playerWinRepository, "playerWinRepository"));
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
                case "4" -> showRanking();
                case "5" -> running = false;
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

    /** Megmutatja a pályákat, bekéri a választást, majd elindítja a játékot. */
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
                String playerName = askPlayerName();
                GameSession session = playLevel(new GameSession(level), playerName);
                if (session.winner() == TargetOwner.HUMAN) {
                    campaignHighScore.registerCompletedLevel(session, playerName);
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
            output.println("b. Vissza");
            printPrompt("Választás (szám + Enter): ");

            String choice = readLine().trim();
            if (BACK_COMMAND.equalsIgnoreCase(choice)) {
                return;
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
            String playerName = askPlayerName();
            GameSession session = playLevel(
                    saveStore.loadSession(saves.get(index).name()), playerName);
            if (session.winner() == TargetOwner.HUMAN) {
                campaignHighScore.registerCompletedLevel(session, playerName);
            }
        } catch (NumberFormatException exception) {
            output.println("Érvénytelen mentéssorszám.");
        }
    }

    /** Nevet kér a játékhoz; üres válasz esetén alapértelmezett nevet használ. */
    private String askPlayerName() {
        while (input.hasNextLine()) {
            output.print("J\u00E1t\u00E9kos neve (legfeljebb 255 karakter): ");
            output.flush();
            String name = readLine().trim();
            if (name.isBlank()) {
                return "J\u00E1t\u00E9kos";
            }
            if (name.length() <= 255) {
                return name;
            }
            output.println("A név legfeljebb 255 karakter lehet.");
        }
        return "J\u00E1t\u00E9kos";
    }

    /** Megjeleníti az adatbázisban tárolt játékosokat győzelmeik száma szerint. */
    private void showRanking() {
        playerRankingConsole.showRanking();
    }

    /** Jelzi az adatbázishibát, de nem szakítja meg a már befejezett játékot. */
    private boolean recordPlayerWin(String playerName) {
        return playerRankingConsole.recordWin(playerName);
    }

    /** Lefuttatja a játékot, és az első győzelemkor elmenti az eredményt. */
    private GameSession playLevel(GameSession session, String playerName) throws IOException {
        boolean winAnnounced = false;
        boolean winRecorded = false;
        GameTurnManager turnManager = new GameTurnManager(
                mainMenu.settings(), aiPlayer);
        GameCommandHandler commandHandler = new GameCommandHandler(
                output, saveStore, mainMenu.settings());
        winRecorded = recordWinIfNeeded(session, playerName, winRecorded);
        while (session.isActive() && input.hasNextLine()) {
            output.println(turnManager.currentPlayerLabel() + " következik.");
            BoardRenderer.print(session, output);
            if (session.isWon()) {
                if (!winAnnounced) {
                    output.println(session.winner() == TargetOwner.HUMAN
                            ? "Gratulálok, megoldottad a pályát!"
                            : "Az AI nyerte a játékot.");
                    winAnnounced = true;
                }
                output.println("A pályaválasztóhoz: next; lépések visszavonása: undo; kilépés: b.");
            } else {
                output.println("Írd be: help — irányítás és parancsok");
            }
            printPrompt(gamePrompt(turnManager.currentPlayerLabel()));
            int stepsBeforeCommand = session.steps();
            commandHandler.handle(session, readLine().trim());
            winRecorded = recordWinIfNeeded(session, playerName, winRecorded);
            var aiMove = turnManager.completeHumanTurn(
                    session, session.steps() > stepsBeforeCommand);
            if (aiMove.isPresent()) {
                printAiMove(aiMove.orElseThrow());
                winRecorded = recordWinIfNeeded(session, playerName, winRecorded);
            }
            if (!session.isWon()) {
                winAnnounced = false;
            }
        }
        return session;
    }

    /** Egyszer rögzíti a győzelmet, és jelzi, hogy ez már megtörtént-e. */
    private boolean recordWinIfNeeded(
            GameSession session,
            String playerName,
            boolean winAlreadyRecorded
    ) {
        if (!session.isWon() || session.winner() != TargetOwner.HUMAN
                || winAlreadyRecorded) {
            return winAlreadyRecorded;
        }
        if (recordPlayerWin(playerName)) {
            output.println("Győzelem rögzítve: " + playerName);
        }
        return true;
    }

    private void printAiMove(RandomAiPlayer.AiMove aiMove) {
        output.println("AI-lépés (" + aiMove.move().vehicleId() + " "
                + aiMove.move().direction() + "): " + aiMove.result().message());
    }

    private String gamePrompt(String currentPlayer) {
        return "Parancs (" + currentPlayer + " / help / Enter): ";
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
