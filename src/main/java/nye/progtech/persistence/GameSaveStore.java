package nye.progtech.persistence;

import nye.progtech.model.GameSave;
import nye.progtech.model.map.GameSession;
import nye.progtech.model.map.LevelLoader;


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** A névvel ellátott játékállásokat tároló XML-fájlt olvassa és írja. */
public final class GameSaveStore {
    private static final int MAX_SAVES = 6;

    private final Path file;

    public GameSaveStore(Path file) {
        this.file = Objects.requireNonNull(file, "file").toAbsolutePath();
    }

    /** Visszaadja az összes mentést; hiányzó fájl esetén üres listát ad. */
    public List<GameSave> loadAll() throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }

        try (InputStream input = Files.newInputStream(file)) {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            // A mentés helyi adat: az XML ne tölthessen be külső fájlokat vagy címeket.
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

            XMLStreamReader reader = factory.createXMLStreamReader(input);
            try {
                return parseSaves(reader);
            } finally {
                reader.close();
            }
        } catch (XMLStreamException exception) {
            throw new IOException("Could not read game saves from " + file, exception);
        }
    }

    /** Név alapján keres; a kis- és nagybetűket nem különbözteti meg. */
    public Optional<GameSave> find(String name) throws IOException {
        return loadAll().stream()
                .filter(save -> save.name().equalsIgnoreCase(name))
                .findFirst();
    }

    /** Betölti a mentést, majd összekapcsolja az eredeti pályaleírással. */
    public GameSession loadSession(String name) throws IOException {
        GameSave save = find(name)
                .orElseThrow(() -> new IOException("No saved game named '" + name + "' exists."));
        return new GameSession(LevelLoader.loadLevel(save.levelId()), save);
    }

    /** Pillanatképet készít a játékállásról, majd elmenti a megadott névvel. */
    public void saveSession(GameSession session, String name) throws IOException {
        Objects.requireNonNull(session, "session");
        save(session.save(name));
    }

    /** Új mentést ad hozzá, vagy felülírja az azonos nevű mentést. */
    public void save(GameSave gameSave) throws IOException {
        Objects.requireNonNull(gameSave, "gameSave");
        List<GameSave> saves = new ArrayList<>(loadAll());
        int existingIndex = findSaveIndex(saves, gameSave.name());

        if (existingIndex >= 0) {
            saves.set(existingIndex, gameSave);
        } else {
            if (saves.size() >= MAX_SAVES) {
                throw new IOException("At most " + MAX_SAVES + " named game saves are allowed.");
            }
            saves.add(gameSave);
        }

        writeSaves(saves);
    }

    /** Név alapján töröl; hibát jelez, ha nincs ilyen mentés. */
    public void delete(String name) throws IOException {
        List<GameSave> saves = new ArrayList<>(loadAll());
        boolean removed = saves.removeIf(save -> save.name().equalsIgnoreCase(name));
        if (!removed) {
            throw new IOException("No saved game named '" + name + "' exists.");
        }
        writeSaves(saves);
    }

    /** Feldolgozza az XML-t, és elutasítja a mentési formátumtól eltérő elemeket. */
    private List<GameSave> parseSaves(XMLStreamReader reader)
            throws XMLStreamException, IOException {
        List<GameSave> saves = new ArrayList<>();
        SaveBuilder currentSave = null;
        // A mélység mutatja, hogy a gyökérben, a mentésben vagy egy járműben járunk-e.
        int depth = 0;
        boolean rootSeen = false;
        boolean rootClosed = false;

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                currentSave = handleStartElement(reader, depth, currentSave);
                depth++;
                if (depth == 1) {
                    rootSeen = true;
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                String element = reader.getLocalName();
                if ("save".equals(element)) {
                    if (depth != 2 || currentSave == null) {
                        throw new IOException("Unexpected closing save element.");
                    }
                    try {
                        saves.add(currentSave.build());
                    } catch (IllegalArgumentException exception) {
                        throw new IOException("Invalid save entry in " + file, exception);
                    }
                    currentSave = null;
                } else if ("vehicle".equals(element)) {
                    if (depth != 3 || currentSave == null) {
                        throw new IOException("Unexpected closing vehicle element.");
                    }
                } else if ("saves".equals(element)) {
                    if (depth != 1 || currentSave != null) {
                        throw new IOException("Unexpected closing saves element.");
                    }
                    rootClosed = true;
                }
                depth--;
            }

            if (saves.size() > MAX_SAVES) {
                throw new IOException("Saves file contains more than " + MAX_SAVES + " saves.");
            }
        }

        if (!rootSeen || !rootClosed || depth != 0 || currentSave != null) {
            throw new IOException("Incomplete game saves XML: " + file);
        }
        rejectDuplicateNames(saves);
        return List.copyOf(saves);
    }

    /** Ellenőrzi a nyitó címkét, majd mentést kezd vagy járműpozíciót olvas be. */
    private SaveBuilder handleStartElement(
            XMLStreamReader reader,
            int depth,
            SaveBuilder currentSave
    ) throws IOException {
        String element = reader.getLocalName();

        if (depth == 0 && "saves".equals(element)) {
            return null;
        }
        if (depth == 1 && "save".equals(element)) {
            if (currentSave != null) {
                throw new IOException("Nested save elements are not allowed.");
            }
            return new SaveBuilder(
                    requiredAttribute(reader, "name"),
                    integerAttribute(reader, "levelId"),
                    integerAttribute(reader, "steps"));
        }
        if (depth == 2 && "vehicle".equals(element) && currentSave != null) {
            try {
                currentSave.addVehicle(new GameSave.VehiclePosition(
                        requiredAttribute(reader, "id"),
                        integerAttribute(reader, "row"),
                        integerAttribute(reader, "column")));
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid vehicle position in " + file, exception);
            }
            return currentSave;
        }
        throw new IOException("Unexpected element in saves file: " + element);
    }

    /** A mentésnevek egyediek, mert a keresés egyetlen találatot ad vissza. */
    private void rejectDuplicateNames(List<GameSave> saves) throws IOException {
        for (int first = 0; first < saves.size(); first++) {
            for (int second = first + 1; second < saves.size(); second++) {
                if (saves.get(first).name().equalsIgnoreCase(saves.get(second).name())) {
                    throw new IOException("Duplicate saved game name: " + saves.get(first).name());
                }
            }
        }
    }

    /** Ideiglenes fájlba ír, és csak sikeres írás után cseréli le az eredetit. */
    private void writeSaves(List<GameSave> saves) throws IOException {
        Path parent = file.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "rush-hour-saves-", ".xml");

        try {
            writeXml(temporaryFile, saves);
            replaceFile(temporaryFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /** Kiírja a teljes XML-dokumentumot az ideiglenes fájlba. */
    private void writeXml(Path target, List<GameSave> saves) throws IOException {
        try (OutputStream output = Files.newOutputStream(target)) {
            XMLStreamWriter writer = XMLOutputFactory.newFactory()
                    .createXMLStreamWriter(output, "UTF-8");
            try {
                writer.writeStartDocument("UTF-8", "1.0");
                writer.writeCharacters("\n");
                writer.writeStartElement("saves");
                for (GameSave save : saves) {
                    writeSave(writer, save);
                }
                if (!saves.isEmpty()) {
                    writer.writeCharacters("\n");
                }
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeEndDocument();
                writer.flush();
            } finally {
                writer.close();
            }
        } catch (XMLStreamException exception) {
            throw new IOException("Could not write game saves to " + file, exception);
        }
    }

    /** Kiír egy mentést és annak összes járműpozícióját. */
    private void writeSave(XMLStreamWriter writer, GameSave save) throws XMLStreamException {
        writer.writeCharacters("\n    ");
        writer.writeStartElement("save");
        writer.writeAttribute("name", save.name());
        writer.writeAttribute("levelId", Integer.toString(save.levelId()));
        writer.writeAttribute("steps", Integer.toString(save.steps()));

        for (GameSave.VehiclePosition position : save.vehiclePositions()) {
            writer.writeCharacters("\n        ");
            writer.writeEmptyElement("vehicle");
            writer.writeAttribute("id", position.vehicleId());
            writer.writeAttribute("row", Integer.toString(position.row()));
            writer.writeAttribute("column", Integer.toString(position.column()));
        }

        if (!save.vehiclePositions().isEmpty()) {
            writer.writeCharacters("\n    ");
        }
        writer.writeEndElement();
    }

    /** Atomikusan cseréli a célfájlt, ha ezt a fájlrendszer támogatja. */
    private void replaceFile(Path temporaryFile) throws IOException {
        try {
            Files.move(temporaryFile, file, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Beolvas egy kötelező, nem üres XML-attribútumot. */
    private String requiredAttribute(XMLStreamReader reader, String name) throws IOException {
        String value = reader.getAttributeValue(null, name);
        if (value == null || value.isBlank()) {
            throw new IOException("Missing required '" + name + "' attribute in " + file);
        }
        return value;
    }

    /** Egész számot olvas; hibás értéknél egyértelmű formátumhibát jelez. */
    private int integerAttribute(XMLStreamReader reader, String name) throws IOException {
        String value = requiredAttribute(reader, name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IOException("Attribute '" + name + "' must be an integer.", exception);
        }
    }

    /** Visszaadja a névhez tartozó indexet, vagy -1-et, ha nincs találat. */
    private int findSaveIndex(List<GameSave> saves, String name) {
        for (int index = 0; index < saves.size(); index++) {
            if (saves.get(index).name().equalsIgnoreCase(name)) {
                return index;
            }
        }
        return -1;
    }

    /** A záró címkéig tárolja a készülő mentés adatait. */
    private static final class SaveBuilder {
        private final String name;
        private final int levelId;
        private final int steps;
        private final List<GameSave.VehiclePosition> vehiclePositions = new ArrayList<>();

        private SaveBuilder(String name, int levelId, int steps) {
            this.name = name;
            this.levelId = levelId;
            this.steps = steps;
        }

        private void addVehicle(GameSave.VehiclePosition position) {
            vehiclePositions.add(position);
        }

        private GameSave build() {
            return new GameSave(name, levelId, steps, vehiclePositions);
        }
    }
}
