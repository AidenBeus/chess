package dataaccess;

import model.AuthData;
import model.ChessList;
import model.GameData;
import model.UserData;

import java.sql.Connection;
import java.sql.SQLException;

public class mySqlDataAccess implements DataAccess{
    private mySqlDataAccess() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    public ChessList listGames() {
        return null;
    }

    public AuthData register(UserData user) throws AlreadyTakenException {
        return null;
    }

    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    public AuthData login(String username) throws DataAccessException {
        return null;
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    public void logout(String authToken) throws DataAccessException {

    }

    public GameData createGame(String gameName) throws DataAccessException {
        return null;
    }

    public void joinGame(String playerColor, String username, int gameId) throws DataAccessException, AlreadyTakenException {

    }

    public GameData getGame(int gameId) throws DataAccessException {
        return null;
    }

    public void clear() throws DataAccessException {

    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  pet (
              `id` int NOT NULL AUTO_INCREMENT,
              `name` varchar(256) NOT NULL,
              `type` ENUM('CAT', 'DOG', 'FISH', 'FROG', 'ROCK') DEFAULT 'CAT',
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(type),
              INDEX(name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };
    private void configureDatabase() throws ResponseException, DataAccessException {
        DatabaseManager.initializeSchema();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
