package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.ClearRequest;
import model.ClearResult;

import java.util.Collection;

public class ChessService {
    private final DataAccess dataAccess;

    public ChessService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear() throws DataAccessException {
        Collection<ChessGame> games = dataAccess.listGames();
        if(!games.isEmpty()){
            dataAccess.clear();
        }
    }
}
