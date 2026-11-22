package nye.progtech.persistence.ranking;

import nye.progtech.model.ranking.PlayerWins;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class JdbcPlayerWinRepository implements PlayerWinRepository {
    private final Path databaseFile;
    private final String jdbcUrl;

    public JdbcPlayerWinRepository(Path databaseFile) {
        this.databaseFile = Objects.requireNonNull(databaseFile, "databaseFile")
                .toAbsolutePath();
        String path = this.databaseFile.toString().replace('\\', '/');
        jdbcUrl = "jdbc:h2:file:" + path;
    }

    @Override
    public void recordWin(String playerName) throws SQLException {
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name must not be blank");
        }
        String name = playerName.trim();
        if (name.length() > 255) {
            throw new IllegalArgumentException("Player name must be at most 255 characters");
        }
        String playerKey = name.toLowerCase(Locale.ROOT);
        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE player_wins SET wins = wins + 1 WHERE player_key = ?")) {
                update.setString(1, playerKey);
                if (update.executeUpdate() == 0) {
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO player_wins (player_key, player_name, wins) VALUES (?, ?, 1)")) {
                        insert.setString(1, playerKey);
                        insert.setString(2, name);
                        insert.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    @Override
    public List<PlayerWins> ranking() throws SQLException {
        List<PlayerWins> result = new ArrayList<>();
        try (Connection connection = open();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT player_name, wins FROM player_wins ORDER BY wins DESC, player_name ASC");
             ResultSet rows = query.executeQuery()) {
            while (rows.next()) {
                result.add(new PlayerWins(rows.getString("player_name"), rows.getInt("wins")));
            }
        }
        return List.copyOf(result);
    }

    private Connection open() throws SQLException {
        Path parent = databaseFile.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException exception) {
                throw new SQLException("Could not create database directory: " + parent, exception);
            }
        }
        Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS player_wins ("
                    + "player_key VARCHAR(255) PRIMARY KEY, player_name VARCHAR(255) NOT NULL, "
                    + "wins INTEGER NOT NULL CHECK (wins >= 0))");
        } catch (SQLException exception) {
            connection.close();
            throw exception;
        }
        return connection;
    }
}
