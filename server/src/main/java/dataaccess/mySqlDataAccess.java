package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.ChessList;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Objects;

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

    public UserData getUser(String username) throws DataAccessException {
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
            throw new DataAccessException("Unable to read user data", e);
        }
        return null;
    }

    public AuthData login(String username) throws DataAccessException {
        return addAuth(username);
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
            throw new DataAccessException("Unable to create auth data", ex);
        }
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("authToken"),
                                rs.getString("username")
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to read auth data", e);
        }
        return null;
    }

    public void logout(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, authToken);
            ps.executeUpdate();

        } catch (SQLException | ResponseException ex) {
            throw new DataAccessException("Unable to delete auth data", ex);
        }
    }

    public GameData createGame(String gameName) throws DataAccessException {
        var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, chessGame) VALUES (?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
            ps.setNull(1, NULL);
            ps.setNull(2, NULL);
            ps.setString(3, gameName);
            ps.setString(4, new Gson().toJson(new ChessGame()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new GameData(rs.getInt(1), null, null, gameName, new ChessGame());
                }
            }
            throw new DataAccessException("No game ID created");
        } catch (SQLException | ResponseException ex) {
            throw new DataAccessException("Unable to create auth data", ex);
        }
    }

    public void joinGame(String playerColor, String username, int gameId) throws DataAccessException, AlreadyTakenException {
        var statement = "UPDATE games SET whiteUsername = ? WHERE id = ?";
        if (Objects.equals(playerColor, "WHITE")) {
            statement = "UPDATE games SET whiteUsername = ? WHERE id = ?";
        }
        else {
            statement = "UPDATE games SET blackUsername = ? WHERE id = ?";
        }
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, username);
            ps.setInt(2, gameId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new DataAccessException("Game not found");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to update game", ex);
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }

    public GameData getGame(int gameId) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT chessGame FROM games WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String json = rs.getString("chessGame");
                        return new Gson().fromJson(json, GameData.class);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to read auth data", e);
        }
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
