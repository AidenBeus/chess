package dataaccess;

import chess.ChessGame;
import model.ChessList;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    final private HashMap<Integer, ChessGame> games = new HashMap<>();
    private int nextId;

    public ChessList listGames(){
        return new ChessList(games.values());
    }
    @Override
    public void clear() throws DataAccessException {
        games.clear();
    }
}
