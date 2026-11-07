package org.example.model.mainmenu.settings;

public enum GameMode {
    SINGLE_PLAYER("Egyszemélyes"),
    TWO_PLAYER("Kétszemélyes");

    private final String displayName;

    GameMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean includesBot() {
        return this == SINGLE_PLAYER;
    }
}
