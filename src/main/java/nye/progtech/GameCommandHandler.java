package nye.progtech;

import nye.progtech.persistence.GameSaveStore;
import nye.progtech.model.mainmenu.settings.GameSettings;
import nye.progtech.model.map.GameSession;
import nye.progtech.model.map.MoveResult;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Objects;

final class GameCommandHandler {
    private final PrintStream output;
    private final GameSaveStore saveStore;
    private final GameSettings settings;

    GameCommandHandler(PrintStream output, GameSaveStore saveStore, GameSettings settings) {
        this.output = Objects.requireNonNull(output, "output");
        this.saveStore = Objects.requireNonNull(saveStore, "saveStore");
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    void handle(GameSession session, String command) throws IOException {
        if (command.isBlank()) {
            return;
        }
        String[] parts = command.split("\\s+", 3);
        switch (parts[0].toLowerCase(Locale.ROOT)) {
            case "b", "back", "exit", "next" -> session.leaveLevel();
            case "help", "h", "?" -> GameHelp.print(settings.gameMode(), output);
            case "move" -> moveVehicle(session, parts);
            case "undo" -> undoMoves(session, parts);
            case "save" -> saveGame(session, command);
            default -> output.println("Ismeretlen parancs.");
        }
    }

    private void moveVehicle(GameSession session, String[] parts) {
        if (parts.length != 3) {
            output.println("Használat: move <autóazonosító> <célkoordináta>");
            return;
        }
        printMoveResult(session.move(parts[1], parts[2]));
    }

    private void printMoveResult(MoveResult result) {
        output.println(result.message());
    }

    private void undoMoves(GameSession session, String[] parts) {
        try {
            int count = parts.length == 1 ? 1 : Integer.parseInt(parts[1]);
            session.undo(count);
            output.println(count + " lépés visszavonva.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            output.println(exception.getMessage());
        }
    }

    private void saveGame(GameSession session, String command) throws IOException {
        String name = command.length() > "save".length()
                ? command.substring("save".length()).trim() : "";
        if (name.isBlank()) {
            output.println("Használat: save <név>");
            return;
        }
        saveStore.saveSession(session, name);
        output.println("Játékállás elmentve: " + name);
    }
}
