package nye.progtech.model;

import nye.progtech.model.map.GameSession;
import nye.progtech.model.map.LevelLoader;
import nye.progtech.model.mainmenu.MainMenu;

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
import java.util.Optional;
import java.util.Objects;

public final class GameSaveStore {
    private static final int MAX_SAVES = MainMenu.MAX_SAVED_GAMES;

    private final Path file;

    public GameSaveStore(Path file) {
        this.file = file.toAbsolutePath();
    }

    public List<GameSave> loadAll() throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }
        try (InputStream input = Files.newInputStream(file)) {
            XMLInputFactory factory = XMLInputFactory.newFactory();
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

    public Optional<GameSave> find(String name) throws IOException {
        return loadAll().stream()
                .filter(save -> save.name().equalsIgnoreCase(name))
                .findFirst();
    }

    public GameSession loadSession(String name) throws IOException {
        GameSave save = find(name)
                .orElseThrow(() -> new IOException("No saved game named '" + name + "' exists."));
        return new GameSession(LevelLoader.loadLevel(save.levelId()), save);
    }

    public void saveSession(GameSession session, String name) throws IOException {
        Objects.requireNonNull(session, "session");
        save(session.save(name));
    }

    public void save(GameSave gameSave) throws IOException {
        List<GameSave> saves = new ArrayList<>(loadAll());
        int existingIndex = -1;
        for (int index = 0; index < saves.size(); index++) {
            if (saves.get(index).name().equalsIgnoreCase(gameSave.name())) {
                existingIndex = index;
                break;
            }
        }
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

    public void delete(String name) throws IOException {
        List<GameSave> saves = new ArrayList<>(loadAll());
        boolean removed = saves.removeIf(save -> save.name().equalsIgnoreCase(name));
        if (!removed) {
            throw new IOException("No saved game named '" + name + "' exists.");
        }
        writeSaves(saves);
    }

    private List<GameSave> parseSaves(XMLStreamReader reader)
            throws XMLStreamException, IOException {
        List<GameSave> saves = new ArrayList<>();
        String rootName = null;
        String saveName = null;
        int levelId = 0;
        int steps = 0;
        List<GameSave.VehiclePosition> positions = null;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String element = reader.getLocalName();
                if (rootName == null) {
                    if (!"saves".equals(element)) {
                        throw new IOException("Expected <saves> root element in " + file);
                    }
                    rootName = element;
                } else if ("save".equals(element)) {
                    if (positions != null) {
                        throw new IOException("Nested save elements are not allowed.");
                    }
                    saveName = requiredAttribute(reader, "name");
                    levelId = integerAttribute(reader, "levelId");
                    steps = integerAttribute(reader, "steps");
                    positions = new ArrayList<>();
                } else if ("vehicle".equals(element)) {
                    if (positions == null) {
                        throw new IOException("Vehicle position must be inside a save.");
                    }
                    positions.add(new GameSave.VehiclePosition(
                            requiredAttribute(reader, "id"),
                            integerAttribute(reader, "row"),
                            integerAttribute(reader, "column")));
                } else {
                    throw new IOException("Unexpected element in saves file: " + element);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT
                    && "save".equals(reader.getLocalName())) {
                if (positions == null) {
                    throw new IOException("Unexpected closing save element.");
                }
                saves.add(new GameSave(saveName, levelId, steps, positions));
                positions = null;
                if (saves.size() > MAX_SAVES) {
                    throw new IOException("Saves file contains more than " + MAX_SAVES + " saves.");
                }
            }
        }
        if (rootName == null || positions != null) {
            throw new IOException("Incomplete game saves XML: " + file);
        }
        for (int first = 0; first < saves.size(); first++) {
            for (int second = first + 1; second < saves.size(); second++) {
                if (saves.get(first).name().equalsIgnoreCase(saves.get(second).name())) {
                    throw new IOException("Duplicate saved game name: " + saves.get(first).name());
                }
            }
        }
        return List.copyOf(saves);
    }

    private void writeSaves(List<GameSave> saves) throws IOException {
        Path parent = file.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "rush-hour-saves-", ".xml");
        try {
            try (OutputStream output = Files.newOutputStream(temporaryFile)) {
                XMLStreamWriter writer = XMLOutputFactory.newFactory()
                        .createXMLStreamWriter(output, "UTF-8");
                try {
                    writer.writeStartDocument("UTF-8", "1.0");
                    writer.writeCharacters("\n");
                    writer.writeStartElement("saves");
                    for (GameSave save : saves) {
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
            try {
                Files.move(temporaryFile, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private String requiredAttribute(XMLStreamReader reader, String name) throws IOException {
        String value = reader.getAttributeValue(null, name);
        if (value == null || value.isBlank()) {
            throw new IOException("Missing required '" + name + "' attribute in " + file);
        }
        return value;
    }

    private int integerAttribute(XMLStreamReader reader, String name) throws IOException {
        String value = requiredAttribute(reader, name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IOException("Attribute '" + name + "' must be an integer.", exception);
        }
    }
}
