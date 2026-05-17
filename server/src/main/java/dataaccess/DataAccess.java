package dataaccess;

import chess.ChessGame;
import model.ChessList;

public interface DataAccess {
    public ChessList listGames();
    void clear() throws DataAccessException;
}
