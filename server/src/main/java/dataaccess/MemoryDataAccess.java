package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.ChessList;
import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess{
    final private HashMap<Integer, ChessGame> games = new HashMap<>();
    final private HashMap<String, UserData> users = new HashMap<>();
    private int nextId = 1;

    public ChessList listGames(){
        return new ChessList(games.values());
    }

    public AuthData register(UserData user) throws AlreadyTakenException{
        users.put(user.username(), user);
        return AuthData.generateToken(user.username());
    }

    public UserData getUser(String username){
        return users.get(username);
    }

    public AuthData login(String username) throws DataAccessException {
        return AuthData.generateToken(username);
    }

    public void clear() throws DataAccessException {
        games.clear();
    }
}
