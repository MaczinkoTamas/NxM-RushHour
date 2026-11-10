package nye.progtech.model.highscore;

import nye.progtech.model.mainmenu.settings.Difficulty;

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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class HighScoreStore {
    private final Path file;

    public HighScoreStore(Path file) {
        this.file = Objects.requireNonNull(file, "file").toAbsolutePath();
    }

    public List<HighScore> loadAll() throws IOException {
        if (!Files.exists(file)) {
            return List.of();
        }
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
        try (InputStream input = Files.newInputStream(file)) {
            XMLStreamReader reader = factory.createXMLStreamReader(input);
            try {
                return parse(reader);
            } finally {
                reader.close();
            }
        } catch (XMLStreamException exception) {
            throw new IOException("Could not read high scores from " + file, exception);
        }
    }

    public List<HighScore> ranking(Difficulty difficulty) throws IOException {
        return loadAll().stream()
                .filter(score -> score.difficulty() == difficulty)
                .sorted(Comparator.comparingInt(HighScore::score).reversed())
                .toList();
    }

    public boolean recordIfBest(HighScore candidate) throws IOException {
        Objects.requireNonNull(candidate, "candidate");
        List<HighScore> scores = new ArrayList<>(loadAll());
        int existingIndex = -1;
        for (int index = 0; index < scores.size(); index++) {
            if (scores.get(index).difficulty() == candidate.difficulty()) {
                existingIndex = index;
                break;
            }
        }
        if (existingIndex >= 0 && scores.get(existingIndex).score() >= candidate.score()) {
            return false;
        }
        if (existingIndex >= 0) {
            scores.set(existingIndex, candidate);
        } else {
            scores.add(candidate);
        }
        write(scores);
        return true;
    }

    private List<HighScore> parse(XMLStreamReader reader) throws XMLStreamException, IOException {
        List<HighScore> scores = new ArrayList<>();
        boolean rootSeen = false;
        while (reader.hasNext()) {
            int event = reader.next();
            if (event != XMLStreamConstants.START_ELEMENT) {
                continue;
            }
            if (!rootSeen) {
                if (!"highscores".equals(reader.getLocalName())) {
                    throw new IOException("Expected <highscores> root element in " + file);
                }
                rootSeen = true;
                continue;
            }
            if (!"highscore".equals(reader.getLocalName())) {
                throw new IOException("Unexpected element in high scores file: "
                        + reader.getLocalName());
            }
            try {
                scores.add(new HighScore(
                        Difficulty.valueOf(requiredAttribute(reader, "difficulty")),
                        requiredAttribute(reader, "player"),
                        Integer.parseInt(requiredAttribute(reader, "score"))));
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid high score entry in " + file, exception);
            }
        }
        if (!rootSeen) {
            throw new IOException("Missing <highscores> root element in " + file);
        }
        if (scores.stream().map(HighScore::difficulty).distinct().count() != scores.size()) {
            throw new IOException("Duplicate difficulty in high scores file: " + file);
        }
        return List.copyOf(scores);
    }

    private String requiredAttribute(XMLStreamReader reader, String name) throws IOException {
        String value = reader.getAttributeValue(null, name);
        if (value == null || value.isBlank()) {
            throw new IOException("Missing '" + name + "' attribute in " + file);
        }
        return value;
    }

    private void write(List<HighScore> scores) throws IOException {
        Path parent = file.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "rush-hour-highscores-", ".xml");
        try {
            try (OutputStream output = Files.newOutputStream(temporaryFile)) {
                XMLStreamWriter writer = XMLOutputFactory.newFactory()
                        .createXMLStreamWriter(output, "UTF-8");
                try {
                    writer.writeStartDocument("UTF-8", "1.0");
                    writer.writeStartElement("highscores");
                    for (HighScore score : scores) {
                        writer.writeCharacters("\n    ");
                        writer.writeEmptyElement("highscore");
                        writer.writeAttribute("difficulty", score.difficulty().name());
                        writer.writeAttribute("player", score.playerName());
                        writer.writeAttribute("score", Integer.toString(score.score()));
                    }
                    if (!scores.isEmpty()) {
                        writer.writeCharacters("\n");
                    }
                    writer.writeEndElement();
                    writer.writeEndDocument();
                    writer.flush();
                } finally {
                    writer.close();
                }
            } catch (XMLStreamException exception) {
                throw new IOException("Could not write high scores to " + file, exception);
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
}
