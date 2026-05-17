package service;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.ClearRequest;
import model.ClearResult;
import model.UserData;

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
    public UserData register(UserData user) throws AlreadyTakenException, DataAccessException {
        UserData exists = dataAccess.getUser(user.username());
        if (exists == null){
            dataAccess.register(user);
            return user;
        }
        else{
            throw new AlreadyTakenException("This user already exists");
        }
    }
}
