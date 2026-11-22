package nye.progtech.model.map;

public enum ExitSide {
    RIGHT,
    LEFT,
    TOP,
    BOTTOM;

    public ExitSide oppositeHorizontalSide() {
        return switch (this) {
            case RIGHT -> LEFT;
            case LEFT -> RIGHT;
            case TOP, BOTTOM -> throw new IllegalStateException(
                    "Only horizontal exits are supported.");
        };
    }
}
