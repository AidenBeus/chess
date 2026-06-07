package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.ChessList;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySqlDataAccess implements DataAccess{
    public MySqlDataAccess() {
        try {
            configureDatabase();
        } catch (ResponseException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public ChessList listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();

        var statement = "SELECT id, whiteUsername, blackUsername, gameName, chessGame FROM games";

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                ChessGame chessGame = new Gson().fromJson(
                        rs.getString("chessGame"),
                        ChessGame.class
                );

                games.add(new GameData(
                        rs.getInt("id"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        chessGame
                ));
            }

            return new ChessList(games);

        } catch (SQLException | ResponseException e) {
            throw new DataAccessException("Unable to list games", e);
        }
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
        } catch (SQLException | ResponseException e) {
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
        } catch (SQLException | ResponseException e) {
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
        var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, chessGame)VALUES (?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {

            ps.setNull(1, Types.VARCHAR);
            ps.setNull(2, Types.VARCHAR);
            ps.setString(3, gameName);
            ps.setString(4, new Gson().toJson(new ChessGame()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new GameData(
                            rs.getInt(1),
                            null,
                            null,
                            gameName,
                            new ChessGame()
                    );
                }
            }

            throw new DataAccessException("No game ID created");
        } catch (SQLException | ResponseException ex) {
            throw new DataAccessException("Unable to create game", ex);
        }
    }

    public void joinGame(String playerColor, String username, int gameId)
            throws DataAccessException, AlreadyTakenException {

        String statement;

        if (Objects.equals(playerColor, "WHITE")) {
            statement = "UPDATE games SET whiteUsername = ? WHERE id = ? AND whiteUsername IS NULL";
        } else if (Objects.equals(playerColor, "BLACK")) {
            statement = "UPDATE games SET blackUsername = ? WHERE id = ? AND blackUsername IS NULL";
        } else {
            throw new DataAccessException("Invalid player color");
        }

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, username);
            ps.setInt(2, gameId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new AlreadyTakenException("That seat is already taken or the game does not exist.");
            }

        } catch (SQLException | ResponseException ex) {
            throw new DataAccessException("Unable to update game", ex);
        }
    }

    public GameData getGame(int gameId) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, whiteUsername, blackUsername, gameName, chessGame FROM games WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ChessGame chessGame = new Gson().fromJson(
                                rs.getString("chessGame"),
                                ChessGame.class
                        );

                        return new GameData(
                                rs.getInt("id"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                chessGame
                        );
                    }
                }
            }
        } catch (SQLException | ResponseException e) {
            throw new DataAccessException("Unable to read game data", e);
        }
        return null;
    }

    public void clear() throws DataAccessException {
            try (var conn = DatabaseManager.getConnection()) {

                try (var ps = conn.prepareStatement("DELETE FROM auth")) {
                    ps.executeUpdate();
                }

                try (var ps = conn.prepareStatement("DELETE FROM games")) {
                    ps.executeUpdate();
                }

                try (var ps = conn.prepareStatement("DELETE FROM users")) {
                    ps.executeUpdate();
                }

            } catch (SQLException | ResponseException ex) {
                throw new DataAccessException("Unable to clear database", ex);
            }
    }

    private void configureDatabase() throws ResponseException, DataAccessException {
        DatabaseManager.initializeSchema();
    }

    public void updateGame(GameData game) throws DataAccessException {
        String statement =
                """
                UPDATE games
                SET whiteUsername = ?,
                    blackUsername = ?,
                    gameName = ?,
                    chessGame = ?
                WHERE id = ?
                """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {

            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, new Gson().toJson(game.game()));
            ps.setInt(5, game.gameID());

            ps.executeUpdate();

        } catch (SQLException | ResponseException e) {
            throw new DataAccessException("Unable to update game", e);
        }
    }
}
