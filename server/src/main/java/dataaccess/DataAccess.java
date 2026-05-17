package dataaccess;

import chess.ChessGame;
import model.ChessList;
import model.UserData;

public interface DataAccess {
    ChessList listGames();
    UserData register(UserData user) throws AlreadyTakenException;
    UserData getUser(String username) throws DataAccessException;
    void clear() throws DataAccessException;
}
