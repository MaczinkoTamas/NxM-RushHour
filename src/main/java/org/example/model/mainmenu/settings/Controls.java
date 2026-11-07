package org.example.model.mainmenu.settings;

import org.example.model.map.Direction;

public enum Controls {
    WASD("WASD", 'w', 's', 'a', 'd'),
    IJKL("IJKL", 'i', 'k', 'j', 'l');

    private final String displayName;
    private final char up;
    private final char down;
    private final char left;
    private final char right;

    Controls(String displayName, char up, char down, char left, char right) {
        this.displayName = displayName;
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
    }

    public String displayName() {
        return displayName;
    }

    public Direction directionFor(String key) {
        if (key == null || key.length() != 1) {
            if (key == null) {
                return null;
            }
            return switch (key.trim().toLowerCase()) {
                case "up", "fel" -> Direction.UP;
                case "down", "le" -> Direction.DOWN;
                case "left", "balra" -> Direction.LEFT;
                case "right", "jobbra" -> Direction.RIGHT;
                default -> null;
            };
        }
        char pressed = Character.toLowerCase(key.charAt(0));
        if (pressed == up) {
            return Direction.UP;
        }
        if (pressed == down) {
            return Direction.DOWN;
        }
        if (pressed == left) {
            return Direction.LEFT;
        }
        if (pressed == right) {
            return Direction.RIGHT;
        }
        return null;
    }

    public char keyFor(Direction direction) {
        return switch (direction) {
            case UP -> up;
            case DOWN -> down;
            case LEFT -> left;
            case RIGHT -> right;
        };
    }

    public Controls next() {
        return this == WASD ? IJKL : WASD;
    }
}
