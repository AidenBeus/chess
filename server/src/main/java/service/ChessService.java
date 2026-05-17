package service;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.ClearRequest;
import model.ClearResult;
import model.UserData;

import java.util.Collection;
import java.util.Objects;

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
    public AuthData register(UserData user) throws AlreadyTakenException, DataAccessException {
        UserData exists = dataAccess.getUser(user.username());
        if (exists == null){
            return dataAccess.register(user);
        }
        else{
            throw new AlreadyTakenException("This user already exists");
        }
    }
    public AuthData login(String username, String password) throws DataAccessException {
        UserData user = dataAccess.getUser(username);
        if (user == null || !Objects.equals(user.username(), username)){
            throw new DataAccessException("Wrong username");
        }
        if (!Objects.equals(user.password(), password)){
            throw new DataAccessException("Wrong password");
        }
        else{
            return dataAccess.login(username);
        }
    }
}
