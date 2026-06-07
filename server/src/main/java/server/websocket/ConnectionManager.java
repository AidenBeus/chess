package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import service.ChessService;
import websocket.commands.MakeMoveCommand;
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

    public void makeMove(MakeMoveCommand command, Consumer<String> rootSend) {
        try {
            AuthData auth = service.getAuth(command.getAuthToken());
            GameData gameData = service.getGame(command.getGameID());

            if (auth == null || gameData == null) {
                sendError(rootSend, "Error: invalid auth token or game id");
                return;
            }

            Connection mover = findConnection(command.getGameID(), auth.username());
            if (mover == null) {
                sendError(rootSend, "Error: you are not connected to this game");
                return;
            }

            if ("OBSERVER".equals(mover.role())) {
                sendError(rootSend, "Error: observers cannot make moves");
                return;
            }

            ChessGame game = gameData.game();
            ChessGame.TeamColor moverColor =
                    mover.role().equals("WHITE")
                            ? ChessGame.TeamColor.WHITE
                            : ChessGame.TeamColor.BLACK;

            if (game.getTeamTurn() != moverColor) {
                sendError(rootSend, "Error: it is not your turn");
                return;
            }

            ChessMove move = command.getMove();
            if (move == null) {
                sendError(rootSend, "Error: missing move");
                return;
            }

            ChessPiece piece = game.getBoard().getPiece(move.getStartPosition());
            if (piece == null) {
                sendError(rootSend, "Error: no piece at the starting square");
                return;
            }

            if (piece.getTeamColor() != moverColor) {
                sendError(rootSend, "Error: you can only move your own pieces");
                return;
            }

            if (!game.validMoves(move.getStartPosition()).contains(move)) {
                sendError(rootSend, "Error: invalid move");
                return;
            }

            game.makeMove(move);
            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            );
            service.updateGame(updatedGame);

            broadcastGame(gameData.gameID(), game);

            String moveText = auth.username() + " moved from "
                    + squareName(move.getStartPosition()) + " to "
                    + squareName(move.getEndPosition());

            broadcastExcept(gameData.gameID(), moveText, mover);

            ChessGame.TeamColor opponent =
                    moverColor == ChessGame.TeamColor.WHITE
                            ? ChessGame.TeamColor.BLACK
                            : ChessGame.TeamColor.WHITE;

            if (game.isInCheckmate(opponent)) {
                broadcastAll(gameData.gameID(), auth.username() + " put " + opponent + " in checkmate");
            } else if (game.isInStalemate(opponent)) {
                broadcastAll(gameData.gameID(), "Game ended in stalemate");
            } else if (game.isInCheck(opponent)) {
                broadcastAll(gameData.gameID(), opponent + " is in check");
            }

        } catch (DataAccessException e) {
            sendError(rootSend, "Error: " + e.getMessage());
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
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

    private Connection findConnection(Integer gameId, String username) {
        Set<Connection> set = connectionsByGame.get(gameId);
        if (set == null) return null;

        for (Connection c : set) {
            if (c.username().equals(username)) {
                return c;
            }
        }
        return null;
    }

    private void broadcastGame(Integer gameId, ChessGame game) {
        Set<Connection> set = connectionsByGame.get(gameId);
        if (set == null) return;

        String json = gson.toJson(new LoadGameMessage(game));
        for (Connection c : set) {
            c.send().accept(json);
        }
    }

    private void broadcastAll(Integer gameId, String message) {
        Set<Connection> set = connectionsByGame.get(gameId);
        if (set == null) return;

        String json = gson.toJson(new NotificationMessage(message));
        for (Connection c : set) {
            c.send().accept(json);
        }
    }

    private String squareName(ChessPosition pos) {
        char file = (char) ('a' + pos.getColumn() - 1);
        return "" + file + pos.getRow();
    }
    private record Connection(
            int gameId,
            String username,
            String role,
            Consumer<String> send
    ) {}
}