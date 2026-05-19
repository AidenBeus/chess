package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.ChessList;
import model.GameData;
import model.UserData;

public interface DataAccess {
    ChessList listGames();
    AuthData register(UserData user) throws AlreadyTakenException;
    UserData getUser(String username) throws DataAccessException;
    AuthData login(String username) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void logout(String authToken) throws DataAccessException;
    GameData createGame(String gameName) throws DataAccessException;
    void joinGame(String playerColor, String username, int gameId) throws DataAccessException, AlreadyTakenException;
    GameData getGame(int gameId) throws DataAccessException;
    void clear() throws DataAccessException;
}
