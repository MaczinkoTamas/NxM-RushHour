package org.example;

import org.example.model.GameSaveStore;
import org.example.model.mainmenu.settings.GameSettings;
import org.example.model.map.Direction;
import org.example.model.map.GameSession;
import org.example.model.map.MoveResult;
import org.example.model.map.Vehicle;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
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
        String[] parts = command.split("\\s+", 4);
        if (isControlKey(parts[0]) && parts.length > 1) {
            moveWithControl(session, parts);
            return;
        }
        if (isVehicleDirectionCommand(session, parts)) {
            moveVehicleInDirection(session, parts);
            return;
        }
        switch (parts[0].toLowerCase()) {
            case "b", "back", "exit", "next" -> session.leaveLevel();
            case "help", "h", "?" -> GameHelp.print(settings.controls(), settings.gameMode(), output);
            case "move" -> moveVehicle(session, parts);
            case "undo" -> undoMoves(session, parts);
            case "save" -> saveGame(session, command);
            default -> output.println("Ismeretlen parancs.");
        }
    }

    private boolean isControlKey(String key) {
        return settings.controls().directionFor(key) != null;
    }

    private void moveWithControl(GameSession session, String[] parts) {
        if (parts.length < 2 || parts.length > 3) {
            output.println("Használat: <iránybillentyű> <színbetű/azonosító> [lépések]");
            return;
        }
        try {
            Direction direction = settings.controls().directionFor(parts[0]);
            int distance = parts.length == 3 ? Integer.parseInt(parts[2]) : 1;
            String vehicleId = resolveVehicleId(session, parts[1]);
            if (vehicleId != null) {
                printMoveResult(session.move(vehicleId, direction, distance));
            }
        } catch (NumberFormatException exception) {
            output.println("A lépésszám egész szám legyen.");
        }
    }

    private boolean isVehicleDirectionCommand(GameSession session, String[] parts) {
        return (parts.length == 2 || parts.length == 3)
                && findVehicle(session, parts[0]) != null
                && parseMoveDirection(parts[1]) != null;
    }

    private void moveVehicleInDirection(GameSession session, String[] parts) {
        try {
            String vehicleId = resolveVehicleId(session, parts[0]);
            int distance = parts.length == 3 ? Integer.parseInt(parts[2]) : 1;
            if (vehicleId != null) {
                Direction direction = parseMoveDirection(parts[1]);
                printMoveResult(session.move(vehicleId, direction, distance));
            }
        } catch (NumberFormatException exception) {
            output.println("A lépésszám egész szám legyen.");
        }
    }

    private Direction parseMoveDirection(String input) {
        Direction direction = Direction.fromNotation(input);
        return direction != null ? direction : settings.controls().directionFor(input);
    }

    private Vehicle findVehicle(GameSession session, String inputId) {
        return session.vehicles().stream()
                .filter(vehicle -> vehicle.id().equalsIgnoreCase(inputId)
                        || vehicle.marker().equalsIgnoreCase(inputId))
                .findFirst()
                .orElse(null);
    }

    private void moveVehicle(GameSession session, String[] parts) {
        if (parts.length == 3) {
            String vehicleId = resolveVehicleId(session, parts[1]);
            if (vehicleId != null) {
                Direction direction = parseMoveDirection(parts[2]);
                MoveResult result = direction == null
                        ? session.moveToCoordinate(vehicleId, parts[2])
                        : session.move(vehicleId, direction, 1);
                printMoveResult(result);
            }
            return;
        }
        if (parts.length == 4) {
            moveVehicleByDistance(session, parts);
            return;
        }
        output.println("Használat: move <színbetű/azonosító> <irány> <lépés> "
                + "vagy move <színbetű/azonosító> <koordináta>");
    }

    private void moveVehicleByDistance(GameSession session, String[] parts) {
        Direction direction = parseMoveDirection(parts[2]);
        if (direction == null) {
            output.println("Érvénytelen irány; használható: U, D, L, R vagy a súgó szerinti billentyűk.");
            return;
        }
        try {
            int distance = Integer.parseInt(parts[3]);
            String vehicleId = resolveVehicleId(session, parts[1]);
            if (vehicleId != null) {
                printMoveResult(session.move(vehicleId, direction, distance));
            }
        } catch (NumberFormatException exception) {
            output.println("A lépésszám egész szám legyen.");
        }
    }

    private String resolveVehicleId(GameSession session, String inputId) {
        List<Vehicle> matches = session.vehicles().stream()
                .filter(vehicle -> vehicle.id().equalsIgnoreCase(inputId)
                        || vehicle.marker().equalsIgnoreCase(inputId))
                .toList();
        if (matches.size() > 1) {
            output.println("A színbetű nem egyedi; add meg a teljes autóazonosítót.");
            return null;
        }
        return matches.isEmpty() ? inputId : matches.getFirst().id();
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
