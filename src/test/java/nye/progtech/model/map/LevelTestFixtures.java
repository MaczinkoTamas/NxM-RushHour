package nye.progtech.model.map;

import nye.progtech.model.mainmenu.settings.Difficulty;

import java.util.List;

final class LevelTestFixtures {
    private LevelTestFixtures() {
    }

    static Level openLevel() {
        return new Level(1, "Nyitott pálya", 6, ExitSide.RIGHT,
                GridCoordinate.parse("G4"), Difficulty.EASY,
                List.of(
                        new Vehicle("RR", 2, 0, 2, Orientation.HORIZONTAL, true),
                        new Vehicle("BB", 0, 0, 2, Orientation.VERTICAL, false),
                        new Vehicle("GG", 0, 2, 2, Orientation.VERTICAL, false),
                        new Vehicle("YY", 0, 4, 2, Orientation.VERTICAL, false),
                        new Vehicle("OO", 4, 0, 2, Orientation.HORIZONTAL, false),
                        new Vehicle("PP", 5, 3, 2, Orientation.HORIZONTAL, false)
                ),
                new Obstacles(List.of()));
    }
}
