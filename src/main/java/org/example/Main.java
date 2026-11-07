package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        Path saveFile = Path.of(System.getProperty("user.home"),
                ".nxm-rushhour", "saves.xml");
        try (Scanner input = new Scanner(System.in)) {
            new ConsoleGame(input, System.out, saveFile).run();
        }
    }
}
