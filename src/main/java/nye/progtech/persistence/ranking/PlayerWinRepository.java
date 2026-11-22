package nye.progtech.persistence.ranking;

import nye.progtech.model.ranking.PlayerWins;

import java.sql.SQLException;
import java.util.List;

public interface PlayerWinRepository {
    void recordWin(String playerName) throws SQLException;

    List<PlayerWins> ranking() throws SQLException;
}
