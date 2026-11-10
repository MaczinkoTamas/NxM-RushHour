package nye.progtech.model.map;

import nye.progtech.model.mainmenu.settings.Difficulty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public final class LevelLoader {
    private static final String LEVELS_RESOURCE = "/levels.xml";
    private static final Logger LOGGER = Logger.getLogger(LevelLoader.class.getName());

    private LevelLoader() {
    }

    public static List<Level> loadLevels() throws IOException {
        Path externalFile = Path.of("levels.xml");
        if (Files.exists(externalFile)) {
            return loadLevelsOrDefault(externalFile);
        }
        try (InputStream input = LevelLoader.class.getResourceAsStream(LEVELS_RESOURCE)) {
            if (input == null) {
                LOGGER.warning("Level resource not found; loading built-in default levels.");
                return DefaultLevels.create();
            }
            try {
                List<Level> levels = loadLevels(input);
                LOGGER.info(() -> "Loaded " + levels.size() + " levels from " + LEVELS_RESOURCE);
                return levels;
            } catch (IOException exception) {
                return useDefaultLevels(exception);
            }
        } catch (IOException exception) {
            return useDefaultLevels(exception);
        }
    }

    public static List<Level> loadLevels(Path file) {
        return loadLevelsOrDefault(file);
    }

    private static List<Level> loadLevelsOrDefault(Path file) {
        try (InputStream input = Files.newInputStream(file)) {
            List<Level> levels = loadLevels(input);
            LOGGER.info(() -> "Loaded " + levels.size() + " levels from " + file);
            return levels;
        } catch (IOException exception) {
            return useDefaultLevels(exception);
        }
    }

    private static List<Level> useDefaultLevels(IOException cause) {
        LOGGER.log(java.util.logging.Level.WARNING,
                "Could not load level file; loading built-in default levels.", cause);
        return DefaultLevels.create();
    }

    public static Level loadLevel(int levelId) throws IOException {
        Level level = loadLevels().stream()
                .filter(candidate -> candidate.id() == levelId)
                .findFirst()
                .orElseThrow(() -> new IOException("Level not found: " + levelId));
        LOGGER.info(() -> "Selected level " + level.id() + ": " + level.name());
        return level;
    }

    public static List<Level> loadLevels(InputStream input) throws IOException {
        try {
            DocumentBuilderFactory factory = createSecureFactory();
            Document document = factory.newDocumentBuilder().parse(input);
            Element root = document.getDocumentElement();
            if (!"levels".equals(root.getTagName())) {
                throw new IOException("Expected <levels> as the document root");
            }

            int boardSize = positiveInteger(root, "boardSize");
            ExitSide defaultExitSide = ExitSide.valueOf(requiredAttribute(root, "exitSide"));
            List<Level> levels = new ArrayList<>();
            Set<Integer> levelIds = new HashSet<>();
            Set<String> levelNames = new HashSet<>();
            Set<String> obstacleLayouts = new HashSet<>();
            Set<String> mapLayouts = new HashSet<>();
            EnumMap<Difficulty, Integer> difficultyCounts = new EnumMap<>(Difficulty.class);
            for (Element levelElement : childElements(root, "level")) {
                Level level = parseLevel(levelElement, boardSize, defaultExitSide);
                if (!levelIds.add(level.id())) {
                    throw new IOException("Duplicate level ID: " + level.id());
                }
                if (!levelNames.add(level.name())) {
                    throw new IOException("Duplicate level name: " + level.name());
                }
                String obstacleLayout = level.obstacles().cells().stream()
                        .sorted(java.util.Comparator.comparingInt(Obstacles.Cell::row)
                                .thenComparingInt(Obstacles.Cell::column))
                        .map(cell -> cell.row() + "," + cell.column())
                        .collect(java.util.stream.Collectors.joining(";"));
                if (!obstacleLayouts.add(obstacleLayout)) {
                    throw new IOException("Duplicate obstacle layout in level: " + level.name());
                }
                String vehicleLayout = level.vehicles().stream()
                        .sorted(java.util.Comparator.comparing(Vehicle::id))
                        .map(vehicle -> vehicle.id() + ":" + vehicle.row() + ","
                                + vehicle.column() + "," + vehicle.length() + ","
                                + vehicle.orientation())
                        .collect(java.util.stream.Collectors.joining(";"));
                if (!mapLayouts.add(vehicleLayout + "|" + obstacleLayout)) {
                    throw new IOException("Duplicate map layout in level: " + level.name());
                }
                difficultyCounts.merge(level.difficulty(), 1, Integer::sum);
                levels.add(level);
            }
            if (levels.isEmpty()) {
                throw new IOException("The levels document contains no levels");
            }
            for (Difficulty difficulty : Difficulty.values()) {
                int count = difficultyCounts.getOrDefault(difficulty, 0);
                if (count != Difficulty.LEVELS_PER_DIFFICULTY) {
                    throw new IOException("Expected " + Difficulty.LEVELS_PER_DIFFICULTY
                            + " levels for " + difficulty.displayName() + ", found " + count);
                }
            }
            return List.copyOf(levels);
        } catch (IOException exception) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Could not read level XML", exception);
            throw exception;
        } catch (ParserConfigurationException | SAXException | IllegalArgumentException exception) {
            IOException ioException = new IOException(
                    "Could not read level XML: " + exception.getMessage(), exception);
            LOGGER.log(java.util.logging.Level.SEVERE, ioException.getMessage(), ioException);
            throw ioException;
        }
    }

    private static DocumentBuilderFactory createSecureFactory()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    private static Level parseLevel(
            Element element,
            int boardSize,
            ExitSide defaultExitSide
    ) throws IOException {
        int id = positiveInteger(element, "id");
        int levelBoardSize = element.hasAttribute("boardSize")
                ? positiveInteger(element, "boardSize") : boardSize;
        String name = requiredAttribute(element, "name");
        Difficulty difficulty = Difficulty.valueOf(requiredAttribute(element, "difficulty"));
        ExitSide exitSide = element.hasAttribute("exitSide")
                ? ExitSide.valueOf(requiredAttribute(element, "exitSide")) : defaultExitSide;
        GridCoordinate exitCoordinate = GridCoordinate.parse(
                requiredAttribute(requiredChild(element, "exit"), "coordinate"));

        List<Vehicle> vehicles = new ArrayList<>();
        Element vehiclesElement = requiredChild(element, "vehicles");
        for (Element vehicleElement : childElements(vehiclesElement, "vehicle")) {
            GridCoordinate coordinate = GridCoordinate.parse(
                    requiredAttribute(vehicleElement, "coordinate"));
            String vehicleId = requiredAttribute(vehicleElement, "id");
            vehicles.add(new Vehicle(
                    vehicleId,
                    VehicleColor.fromMarker(vehicleId),
                    coordinate.rowIndex(levelBoardSize),
                    coordinate.columnIndex(levelBoardSize),
                    positiveInteger(vehicleElement, "length"),
                    Orientation.valueOf(requiredAttribute(vehicleElement, "orientation")),
                    Boolean.parseBoolean(vehicleElement.getAttribute("target"))
            ));
        }

        List<Obstacles.Cell> obstacleCells = new ArrayList<>();
        Element obstaclesElement = requiredChild(element, "obstacles");
        for (Element obstacleElement : childElements(obstaclesElement, "obstacle")) {
            GridCoordinate coordinate = GridCoordinate.parse(
                    requiredAttribute(obstacleElement, "coordinate"));
            obstacleCells.add(new Obstacles.Cell(
                    coordinate.rowIndex(levelBoardSize),
                    coordinate.columnIndex(levelBoardSize)
            ));
        }

        return new Level(id, name, levelBoardSize, exitSide, exitCoordinate, difficulty,
                vehicles, new Obstacles(obstacleCells));
    }

    private static Element requiredChild(Element parent, String name) throws IOException {
        List<Element> children = childElements(parent, name);
        if (children.size() != 1) {
            throw new IOException("Expected exactly one <" + name + "> element");
        }
        return children.get(0);
    }

    private static List<Element> childElements(Element parent, String name) {
        List<Element> elements = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child instanceof Element element && name.equals(element.getTagName())) {
                elements.add(element);
            }
        }
        return elements;
    }

    private static int positiveInteger(Element element, String attribute) throws IOException {
        int value = integer(element, attribute);
        if (value < 1) {
            throw new IOException("Attribute " + attribute + " must be positive");
        }
        return value;
    }

    private static int nonNegativeInteger(Element element, String attribute) throws IOException {
        int value = integer(element, attribute);
        if (value < 0) {
            throw new IOException("Attribute " + attribute + " must not be negative");
        }
        return value;
    }

    private static int integer(Element element, String attribute) throws IOException {
        String value = requiredAttribute(element, attribute);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IOException("Attribute " + attribute + " must be an integer", exception);
        }
    }

    private static String requiredAttribute(Element element, String attribute) throws IOException {
        String value = element.getAttribute(attribute);
        if (value.isBlank()) {
            throw new IOException("Missing required attribute: " + attribute);
        }
        return value;
    }
}
