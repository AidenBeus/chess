package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.ChessList;
import model.UserData;

public interface DataAccess {
    ChessList listGames();
    AuthData register(UserData user) throws AlreadyTakenException;
    UserData getUser(String username) throws DataAccessException;
    AuthData login(String username) throws DataAccessException;
    AuthData getAuth(String username) throws DataAccessException;
    void logout(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
}
