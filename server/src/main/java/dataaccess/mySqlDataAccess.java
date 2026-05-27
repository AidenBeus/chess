package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.ChessList;
import model.GameData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class mySqlDataAccess implements DataAccess{
    private mySqlDataAccess() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    public ChessList listGames() {
        return null;
    }

    public AuthData register(UserData user) throws AlreadyTakenException, ResponseException, DataAccessException {
        UserData existingUser = getUser(user.username());
        if (existingUser != null) {
            throw new AlreadyTakenException("This user already exists!");
        }
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, user.username());
            ps.setString(2, BCrypt.hashpw(user.password(), BCrypt.gensalt()));
            ps.setString(3, user.email());

            ps.executeUpdate();
            return addAuth(user.username());

        } catch (SQLException ex) {
            throw new DataAccessException("Unable to create user", ex);
        }
    }

    public UserData getUser(String username) throws DataAccessException, ResponseException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, username, password, email FROM users WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(ResponseException.Code.ServerError, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public AuthData login(String username) throws DataAccessException {
        return null;
    }

    public AuthData addAuth(String username) throws DataAccessException {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            AuthData auth = AuthData.generateToken(username);
            ps.setString(1, auth.authToken());
            ps.setString(2, auth.username());

            ps.executeUpdate();
            return auth;

        } catch (SQLException | ResponseException ex) {
            throw new DataAccessException("Unable to create user", ex);
        }
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
