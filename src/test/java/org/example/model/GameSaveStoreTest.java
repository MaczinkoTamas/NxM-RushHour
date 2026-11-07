package org.example.model;

import org.example.model.map.GameSession;
import org.example.model.map.LevelLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSaveStoreTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void replacesFindsDeletesAndLoadsNamedSaves() throws IOException {
        GameSaveStore store = new GameSaveStore(temporaryDirectory.resolve("saves.xml"));

        assertTrue(store.loadAll().isEmpty());
        GameSession session = new GameSession(LevelLoader.loadLevel(1));
        store.saveSession(session, "Első állás");
        store.save(new GameSave("Első állás", 1, 4, session.save("temporary").vehiclePositions()));

        assertEquals(1, store.loadAll().size());
        assertEquals(4, store.find("első állás").orElseThrow().steps());
        assertEquals(4, store.loadSession("Első állás").steps());
        assertThrows(IOException.class, () -> store.loadSession("missing"));
        store.delete("ELSŐ ÁLLÁS");
        assertTrue(store.loadAll().isEmpty());
        assertThrows(IOException.class, () -> store.delete("missing"));
    }

    @Test
    void rejectsWrongRootElement() throws IOException {
        Path file = temporaryDirectory.resolve("invalid.xml");
        GameSaveStore store = new GameSaveStore(file);

        Files.writeString(file, "<wrong/>");
        assertThrows(IOException.class, store::loadAll);
    }

    @Test
    void rejectsInvalidIntegerAttributes() throws IOException {
        GameSaveStore store = storeWithXml(
                "<saves><save name=\"x\" levelId=\"bad\" steps=\"0\"/></saves>");
        assertThrows(IOException.class, store::loadAll);
    }

    @Test
    void rejectsUnexpectedElementsAndVehiclesOutsideSaves() throws IOException {
        GameSaveStore unexpected = storeWithXml("<saves><unexpected/></saves>");
        assertThrows(IOException.class, unexpected::loadAll);

        GameSaveStore misplacedVehicle = storeWithXml(
                "<saves><vehicle id=\"RR\" row=\"0\" column=\"0\"/></saves>");
        assertThrows(IOException.class, misplacedVehicle::loadAll);
    }

    @Test
    void rejectsNestedSavesDuplicateNamesAndIncompleteDocuments() throws IOException {
        GameSaveStore nested = storeWithXml("<saves><save name=\"x\" levelId=\"1\" steps=\"0\">"
                + "<save name=\"nested\" levelId=\"1\" steps=\"0\"/></save></saves>");
        assertThrows(IOException.class, nested::loadAll);

        GameSaveStore duplicateNames = storeWithXml(
                "<saves><save name=\"x\" levelId=\"1\" steps=\"0\"/>"
                        + "<save name=\"X\" levelId=\"1\" steps=\"0\"/></saves>");
        assertThrows(IOException.class, duplicateNames::loadAll);

        GameSaveStore incomplete = storeWithXml("<saves>");
        assertThrows(IOException.class, incomplete::loadAll);
    }

    private GameSaveStore storeWithXml(String xml) throws IOException {
        Path file = temporaryDirectory.resolve("invalid.xml");
        Files.writeString(file, xml);
        return new GameSaveStore(file);
    }
}
