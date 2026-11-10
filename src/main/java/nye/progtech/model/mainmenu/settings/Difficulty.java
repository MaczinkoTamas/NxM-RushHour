package nye.progtech.model.mainmenu.settings;

public enum Difficulty {
    EASY(6, 6, "Könnyű"),
    MEDIUM(8, 14, "Közepes"),
    HARD(10, 18, "Nehéz");

    public static final int LEVELS_PER_DIFFICULTY = 6;

    private final int boardSize;
    private final int vehicleCount;
    private final String displayName;

    Difficulty(int boardSize, int vehicleCount, String displayName) {
        this.boardSize = boardSize;
        this.vehicleCount = vehicleCount;
        this.displayName = displayName;
    }

    public int boardSize() {
        return boardSize;
    }

    public int vehicleCount() {
        return vehicleCount;
    }

    public String displayName() {
        return displayName;
    }
}
