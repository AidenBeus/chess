package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import model.*;
import org.mindrot.jbcrypt.BCrypt;
import service.ChessService;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final ChessService service;

    public Server() {
        service = new ChessService(new mySqlDataAccess());
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::register)
                .post("/session", this::login)
                .delete("/session", this::logout)
                .get("/game", this::listGames)
                .post("/game", this::createGame)
                .put("/game", this::joinGame)
                .delete("/db", this::clear)
        ;
        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void register(Context context) {
        try {
            UserData user = new Gson().fromJson(context.body(), UserData.class);

            if (user.username() == null || user.password() == null || user.email() == null) {
                context.status(400);
                context.result(new Gson().toJson(Map.of("message", "Error: Bad Request")));
                return;
            }

            if (service.getUser(user.username()) != null) {
                context.status(403);
                context.result(new Gson().toJson(Map.of("message", "Error: Already Taken")));
                return;
            }

            AuthData result = service.register(user);
            context.status(200);
            context.result(new Gson().toJson(result));

        } catch (AlreadyTakenException e) {
            context.status(403);
            context.result(new Gson().toJson(Map.of("message", "Error: Already Taken")));
        } catch (DataAccessException | ResponseException e) {
            context.status(500);
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void login(Context context) {
        try {
            UserData user = new Gson().fromJson(context.body(), UserData.class);

            if (user.username() == null || user.password() == null) {
                context.status(400);
                context.result(new Gson().toJson(Map.of("message", "Error: Bad Request")));
                return;
            }

            UserData existingUser = service.getUser(user.username());
            if (existingUser == null) {
                context.status(401);
                context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
                return;
            }

            if (!BCrypt.checkpw(user.password(), existingUser.password())) {
                context.status(401);
                context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
                return;
            }

            AuthData result = service.login(user.username(), user.password());
            context.status(200);
            context.result(new Gson().toJson(result));

        } catch (DataAccessException | ResponseException e) {
            context.status(500);
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
    private void logout(Context context) {
        String authToken = context.header("authorization");
        if (authToken == null || authToken.isBlank()) {
            context.status(401);
            context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
            return;
        }
        try {
            service.logout(authToken);
            context.status(200);
            context.result("{}");
        } catch (DataAccessException e) {
            context.status(500);
            context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
        }
    }
    private void listGames(Context context) {
        String authToken = context.header("authorization");
        if (authToken == null || authToken.isBlank()) {
            context.status(401);
            context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
            return;
        }
        try {
            ChessList games = service.listGames(authToken);
            context.status(200);
            context.result(new Gson().toJson(games));
        } catch (DataAccessException e) {
            context.status(500);
            context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
        }
    }
    private void createGame(Context context) {
        try {
            String authToken = context.header("authorization");
            if (authToken == null || authToken.isBlank()) {
                context.status(401);
                context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
                return;
            }

            GameData request = new Gson().fromJson(context.body(), GameData.class);
            String gameName = request.gameName();
            if (gameName == null) {
                context.status(400);
                context.result(new Gson().toJson(Map.of("message", "Error: No game name")));
                return;
            }

            GameData game = service.createGame(authToken, gameName);
            context.status(200);
            context.result(new Gson().toJson(game));

        } catch (DataAccessException e) {
            context.status(500);
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void joinGame(Context context) {
        try {
            String authToken = context.header("authorization");
            if (authToken == null || authToken.isBlank()) {
                context.status(401);
                context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
                return;
            }

            JoinGameRequest request = new Gson().fromJson(context.body(), JoinGameRequest.class);
            if (request == null || request.playerColor() == null) {
                context.status(400);
                context.result(new Gson().toJson(Map.of("message", "Error: Bad Request")));
                return;
            }

            AuthData auth = service.getAuth(authToken);
            if (auth == null) {
                context.status(401);
                context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
                return;
            }

            GameData game = service.getGame(request.gameID());
            if (game == null) {
                context.status(400);
                context.result(new Gson().toJson(Map.of("message", "Error: Bad Request")));
                return;
            }

            if (request.playerColor().equals("WHITE") || request.playerColor().equals("BLACK")) {
                service.joinGame(authToken, request.playerColor(), auth.username(), request.gameID());
                context.status(200);
                context.result("{}");
            } else {
                context.status(400);
                context.result(new Gson().toJson(Map.of("message", "Error: Color not WHITE or BLACK")));
            }

        } catch (AlreadyTakenException e) {
            context.status(403);
            context.result(new Gson().toJson(Map.of("message", "Error: Already Taken")));
        } catch (DataAccessException e) {
            context.status(500);
            context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }

    private void clear(Context context) {
            try {
                service.clear();
                context.status(200);
                context.result("{}");
            } catch (DataAccessException e) {
                context.status(500);
                context.result(new Gson().toJson(Map.of("message", "Error: " + e.getMessage())));
            }
    }
}
