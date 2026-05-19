package service;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;

import java.util.Collection;
import java.util.Objects;

public class ChessService {
    private final DataAccess dataAccess;

    public ChessService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public UserData getUser(String username) throws DataAccessException {
        return dataAccess.getUser(username);
    }
    public void clear() throws DataAccessException {
        dataAccess.clear();
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
    public AuthData getAuth(String authToken) throws DataAccessException {
        return dataAccess.getAuth(authToken);
    }
    public void logout(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("No auth exists");
        }
        dataAccess.logout(authToken);
    }
    public ChessList listGames(String authToken) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("No auth exists");
        }
        return dataAccess.listGames();
    }
    public GameData createGame(String authToken, String gameName) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("No auth exists");
        }
        return dataAccess.createGame(gameName);
    }
    public void joinGame(String authToken, String playerColor, String username, int gameId) throws DataAccessException, AlreadyTakenException {
        AuthData auth = dataAccess.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("No auth exists");
        }
        GameData game = dataAccess.getGame(gameId);
        if(Objects.equals(playerColor, "WHITE")){
            if (game.whiteUsername() == null){
                dataAccess.joinGame(playerColor, username, gameId);
            }
            else{
                throw new AlreadyTakenException("Player with same color already exists");
            }
        }
        if(Objects.equals(playerColor, "BLACK")){
            if (game.blackUsername() == null){
                dataAccess.joinGame(playerColor, username, gameId);
            }
            else{
                throw new AlreadyTakenException("Player with same color already exists");
            }
        }
    }
    public GameData getGame(int gameId) throws DataAccessException {
        return dataAccess.getGame(gameId);
    }
}
