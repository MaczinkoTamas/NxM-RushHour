package nye.progtech.model.mainmenu.settings;

public enum Difficulty {
    EASY(6, "Könnyű"),
    MEDIUM(14, "Közepes"),
    HARD(18, "Nehéz");

    public static final int LEVELS_PER_DIFFICULTY = 6;

    private final int vehicleCount;
    private final String displayName;

    Difficulty(int vehicleCount, String displayName) {
        this.vehicleCount = vehicleCount;
        this.displayName = displayName;
    }

    public int vehicleCount() {
        return vehicleCount;
    }

    public String displayName() {
        return displayName;
    }
}
