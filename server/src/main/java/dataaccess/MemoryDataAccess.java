package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.ChessList;
import model.GameData;
import model.UserData;

import java.util.HashMap;
import java.util.Objects;

public class MemoryDataAccess implements DataAccess{
    final private HashMap<Integer, GameData> games = new HashMap<>();
    final private HashMap<String, UserData> users = new HashMap<>();
    final private HashMap<String, AuthData> authTokens = new HashMap<>();
    private int nextId = 1;

    public ChessList listGames(){
        return new ChessList(games.values());
    }

    public AuthData register(UserData user) throws AlreadyTakenException{
        users.put(user.username(), user);
        AuthData auth = AuthData.generateToken(user.username());
        authTokens.put(auth.authToken(), auth);
        return auth;
    }

    public UserData getUser(String username){
        return users.get(username);
    }

    public AuthData login(String username) throws DataAccessException {
        AuthData auth = AuthData.generateToken(username);
        authTokens.put(auth.authToken(), auth);
        return auth;
    }

    public void logout(String authToken) {
        authTokens.remove(authToken);
    }

    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    public GameData createGame(String gameName){
        GameData game = new GameData(nextId, "", "", gameName, new ChessGame());
        games.put(nextId++, game);
        return game;
    }

    public void joinGame(String playerColor, String username, int gameId){
        GameData game = games.get(gameId);
        if (Objects.equals(playerColor, "WHITE")){
            games.replace(gameId, game.changeWhite(username));
        }
        else{
            games.replace(gameId, game.changeBlack(username));

        }
    }

    public GameData getGame(int gameId) throws DataAccessException {
        return games.get(gameId);
    }

    public void clear() throws DataAccessException {
        games.clear();
        users.clear();
        authTokens.clear();
        nextId = 1;
    }
}
