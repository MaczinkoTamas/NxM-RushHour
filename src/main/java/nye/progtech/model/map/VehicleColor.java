package nye.progtech.model.map;

public enum VehicleColor {
    RED("R", "Piros"),
    BLUE("B", "Kék"),
    GREEN("G", "Zöld"),
    YELLOW("Y", "Sárga"),
    ORANGE("O", "Narancs"),
    PURPLE("P", "Lila"),
    CYAN("C", "Cián"),
    WHITE("W", "Fehér"),
    BLACK("K", "Fekete"),
    MAGENTA("M", "Bíbor"),
    TURQUOISE("T", "Türkiz"),
    BROWN("N", "Barna"),
    SILVER("S", "Ezüst"),
    GOLD("D", "Arany"),
    EMERALD("E", "Smaragdzöld"),
    FLAME("F", "Lángvörös"),
    LAVENDER("H", "Levendula"),
    JADE("J", "Jáde");

    private final String marker;
    private final String displayName;

    VehicleColor(String marker, String displayName) {
        this.marker = marker;
        this.displayName = displayName;
    }

    public String marker() {
        return marker;
    }

    public String displayName() {
        return displayName;
    }

    public static VehicleColor fromMarker(String marker) {
        for (VehicleColor color : values()) {
            if (color.marker.equals(marker) || (color.marker + color.marker).equals(marker)) {
                return color;
            }
        }
        throw new IllegalArgumentException("Unknown vehicle color marker: " + marker);
    }
}
