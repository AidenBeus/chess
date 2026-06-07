package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import service.ChessService;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;

public class ConnectionManager {
    private final ChessService service;
    private final Gson gson = new Gson();

    private final Map<Integer, Set<Connection>> connectionsByGame = new HashMap<>();

    public ConnectionManager(ChessService service) {
        this.service = service;
    }

    public void connect(String authToken, Integer gameId, Consumer<String> send)
            throws DataAccessException {
        AuthData auth = service.getAuth(authToken);
        GameData game = service.getGame(gameId);

        if (auth == null || game == null) {
            sendError(send, "Error: invalid auth token or game id");
            return;
        }

        String username = auth.username();
        String role =
                username.equals(game.whiteUsername()) ? "WHITE" :
                        username.equals(game.blackUsername()) ? "BLACK" :
                                "OBSERVER";

        Connection connection = new Connection(gameId, username, role, send);
        connectionsByGame.computeIfAbsent(gameId, k -> new HashSet<>()).add(connection);

        sendLoadGame(send, game.game());
        broadcastExcept(gameId,
                username + " connected as " + role.toLowerCase(),
                connection);
    }

    public void cleanup(Object sessionKey) {
        for (Set<Connection> connections : connectionsByGame.values()) {
            connections.removeIf(c -> c.equals(sessionKey));
        }
    }

    private void broadcastExcept(Integer gameId, String message, Connection except) {
        Set<Connection> recipients = new HashSet<>(connectionsByGame.getOrDefault(gameId, Set.of()));

        NotificationMessage msg = new NotificationMessage(message);
        String json = gson.toJson(msg);

        for (Connection c : recipients) {
            if (!c.equals(except)) {
                c.send().accept(json);
            }
        }
    }

    private void sendLoadGame(Consumer<String> send, ChessGame game) {
        LoadGameMessage msg = new LoadGameMessage(game);
        send.accept(gson.toJson(msg));
    }

    public void makeMove(Object session, UserGameCommand command) {
        // implement later
    }

    public void leave(Object session, UserGameCommand command) {
        // implement later
    }

    public void resign(Object session, UserGameCommand command) {
        // implement later
    }

    private void sendError(Consumer<String> send, String text) {
        ErrorMessage msg = new ErrorMessage(text);
        send.accept(gson.toJson(msg));
    }

    private record Connection(
            int gameId,
            String username,
            String role,
            Consumer<String> send
    ) {}
}