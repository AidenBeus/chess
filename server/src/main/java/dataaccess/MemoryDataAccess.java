package dataaccess;

import chess.ChessGame;
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

    public UserData register(UserData user) throws AlreadyTakenException{
        users.put(user.username(), user);
        return user;
    }

    public UserData getUser(String username){
        return users.get(username);
    }
    @Override
    public void clear() throws DataAccessException {
        games.clear();
    }
}
