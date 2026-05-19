package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.ChessList;
import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    final private HashMap<Integer, ChessGame> games = new HashMap<>();
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

    public void clear() throws DataAccessException {
        games.clear();
        users.clear();
        authTokens.clear();
        nextId = 1;
    }
}
