package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import service.ChessService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConnectionManager {
    private final ChessService service;
    private final Gson gson = new Gson();

    private final Map<Integer, Set<Connection>> connectionsByGame = new HashMap<>();
    private final Set<Integer> resignedGames = new HashSet<>();

    public ConnectionManager(ChessService service) {
        this.service = service;
    }

    public void connect(String authToken, Integer gameId, Session session)
            throws DataAccessException {
        AuthData auth = service.getAuth(authToken);
        GameData game = service.getGame(gameId);

        if (auth == null || game == null) {
            sendError(session, "Error: invalid auth token or game id");
            return;
        }

        String username = auth.username();
        String role =
                username.equals(game.whiteUsername()) ? "WHITE" :
                        username.equals(game.blackUsername()) ? "BLACK" :
                                "OBSERVER";

        Connection connection = new Connection(session, gameId, username, role);
        connectionsByGame.computeIfAbsent(gameId, k -> new HashSet<>()).add(connection);

        sendLoadGame(session, game.game());
        broadcastExcept(gameId, username + " connected as " + role.toLowerCase(), connection);
    }

    public void cleanup(Session session) {
        System.out.println("cleanup called for session: " + session);
        for (Map.Entry<Integer, Set<Connection>> entry : connectionsByGame.entrySet()) {
            Integer gameId = entry.getKey();
            Set<Connection> connections = entry.getValue();

            boolean removed = connections.removeIf(c -> c.session().equals(session));
            if (removed) {
                System.out.println("Removed session from game " + gameId);
            }
        }
    }

    public void makeMove(MakeMoveCommand command, Session rootSession) {
        try {
            AuthData auth = service.getAuth(command.getAuthToken());
            GameData gameData = service.getGame(command.getGameID());
            if (auth == null || gameData == null) {
                sendError(rootSession, "Error: invalid auth token or game id");
                return;
            }
            if (gameData.isGameOver()) {
                sendError(rootSession, "Error: game is over");
                return;
            }

            Connection mover = findConnection(command.getGameID(), auth.username());
            if (mover == null) {
                sendError(rootSession, "Error: you are not connected to this game");
                return;
            }

            if ("OBSERVER".equals(mover.role())) {
                sendError(rootSession, "Error: observers cannot make moves");
                return;
            }

            ChessGame game = gameData.game();
            ChessGame.TeamColor moverColor =
                    mover.role().equals("WHITE")
                            ? ChessGame.TeamColor.WHITE
                            : ChessGame.TeamColor.BLACK;

            if (game.getTeamTurn() != moverColor) {
                sendError(rootSession, "Error: it is not your turn");
                return;
            }

            ChessMove move = command.getMove();
            if (move == null) {
                sendError(rootSession, "Error: missing move");
                return;
            }

            ChessPiece piece = game.getBoard().getPiece(move.getStartPosition());
            if (piece == null) {
                sendError(rootSession, "Error: no piece at the starting square");
                return;
            }

            if (piece.getTeamColor() != moverColor) {
                sendError(rootSession, "Error: you can only move your own pieces");
                return;
            }

            var legalMoves = game.validMoves(move.getStartPosition());
            if (legalMoves == null || !legalMoves.contains(move)) {
                sendError(rootSession, "Error: invalid move");
                return;
            }

            System.out.println("before makeMove");
            try {
                game.makeMove(move);
                System.out.println("after makeMove");
            } catch (Exception e) {
                System.out.println("game.makeMove failed");
                e.printStackTrace();
                sendError(rootSession, "Error: " + e);
                return;
            }

            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    game
            );

            System.out.println("before updateGame");
            service.updateGame(updatedGame);
            System.out.println("after updateGame");

            System.out.println("before broadcastGame");
            broadcastGame(gameData.gameID(), game);
            System.out.println("after broadcastGame");

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

        } catch (Exception e) {
            System.out.println("MAKE MOVE FAILED");
            e.printStackTrace();
            sendError(rootSession, "Error: " + e);
        }
    }

    public void leave(Session session, UserGameCommand command) {
        try {
            AuthData auth = service.getAuth(command.getAuthToken());
            GameData gameData = service.getGame(command.getGameID());

            if (auth == null || gameData == null) {
                sendError(session, "Error: invalid auth token or game id");
                return;
            }

            if (gameData.isGameOver()) {
                removeConnection(command.getGameID(), auth.username());
                return;
            }

            Connection connection = findConnection(command.getGameID(), auth.username());
            if (connection == null) {
                sendError(session, "Error: not connected to this game");
                return;
            }

            String username = auth.username();
            boolean changed = false;

            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    gameData.game()
            );

            if (username.equals(gameData.whiteUsername())) {
                updatedGame = new GameData(
                        gameData.gameID(),
                        null,
                        gameData.blackUsername(),
                        gameData.gameName(),
                        gameData.game()
                );
                changed = true;
            } else if (username.equals(gameData.blackUsername())) {
                updatedGame = new GameData(
                        gameData.gameID(),
                        gameData.whiteUsername(),
                        null,
                        gameData.gameName(),
                        gameData.game()
                );
                changed = true;
            }

            if (changed) {
                service.updateGame(updatedGame);
            }

            removeConnection(command.getGameID(), username);

            broadcastExcept(
                    command.getGameID(),
                    username + " left the game",
                    connection
            );

        } catch (Exception e) {
            sendError(session, "Error: " + e);
        }
    }

    public void resign(Session session, UserGameCommand command) {
        try {
            AuthData auth = service.getAuth(command.getAuthToken());
            GameData gameData = service.getGame(command.getGameID());

            if (auth == null || gameData == null) {
                sendError(session, "Error: invalid auth token or game id");
                return;
            }

            if (gameData.isGameOver()) {
                sendError(session, "Error: game is over");
                return;
            }

            Connection connection = findConnection(command.getGameID(), auth.username());
            if (connection == null) {
                sendError(session, "Error: you are not connected to this game");
                return;
            }

            if ("OBSERVER".equals(connection.role())) {
                sendError(session, "Error: observers cannot resign");
                return;
            }

            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    gameData.game(),
                    true
            );
            service.updateGame(updatedGame);
            resignedGames.add(command.getGameID());
            broadcastAll(command.getGameID(), auth.username() + " resigned the game");

        } catch (Exception e) {
            sendError(session, "Error: " + e);
        }
    }

    private void sendLoadGame(Session session, ChessGame game) {
        try {
            session.getRemote().sendString(gson.toJson(new LoadGameMessage(game)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendError(Session session, String text) {
        try {
            ErrorMessage msg = new ErrorMessage(text);
            session.getRemote().sendString(gson.toJson(msg));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        Set<Connection> dead = new HashSet<>();

        for (Connection c : set) {
            try {
                c.session().getRemote().sendString(json);
            } catch (Exception e) {
                System.out.println("broadcastGame failed for " + c.username());
                e.printStackTrace();
                dead.add(c);
            }
        }

        set.removeAll(dead);
    }

    private void broadcastAll(Integer gameId, String message) {
        Set<Connection> set = connectionsByGame.get(gameId);
        if (set == null) return;

        String json = gson.toJson(new NotificationMessage(message));
        Set<Connection> dead = new HashSet<>();

        for (Connection c : set) {
            try {
                c.session().getRemote().sendString(json);
            } catch (Exception e) {
                System.out.println("broadcastAll failed for " + c.username());
                e.printStackTrace();
                dead.add(c);
            }
        }

        set.removeAll(dead);
    }

    private void broadcastExcept(Integer gameId, String message, Connection except) {
        Set<Connection> set = connectionsByGame.get(gameId);
        if (set == null) return;

        String json = gson.toJson(new NotificationMessage(message));
        Set<Connection> dead = new HashSet<>();

        for (Connection c : set) {
            if (c.equals(except)) {
                continue;
            }
            try {
                c.session().getRemote().sendString(json);
            } catch (Exception e) {
                System.out.println("broadcastExcept failed for " + c.username());
                e.printStackTrace();
                dead.add(c);
            }
        }

        set.removeAll(dead);
    }

    private String squareName(ChessPosition pos) {
        char file = (char) ('a' + pos.getColumn() - 1);
        return "" + file + pos.getRow();
    }

    private void removeConnection(Integer gameId, String username) {
        Set<Connection> set = connectionsByGame.get(gameId);
        if (set == null) {
            return;
        }
        set.removeIf(c -> c.username().equals(username));
    }

    private boolean isGameResigned(Integer gameId) {
        return resignedGames.contains(gameId);
    }

    private boolean gameIsOver(int gameId) {
        return resignedGames.contains(gameId);
    }

    private record Connection(
            Session session,
            int gameId,
            String username,
            String role
    ) {}
}