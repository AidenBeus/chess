package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.*;
import org.jetbrains.annotations.NotNull;
import service.ChessService;

import java.util.Map;
import java.util.Objects;

public class Server {

    private final Javalin javalin;
    private final ChessService service;

    public Server() {
        service = (new ChessService(new MemoryDataAccess()));
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

    private void register(Context context) throws DataAccessException, AlreadyTakenException {
        UserData user = new Gson().fromJson(context.body(), UserData.class);
        if(user.username() == null || user.password() == null || user.email() == null){
            context.status(400);
            context.result(new Gson().toJson(Map.of("message", "Error: Bad Request")));
            return;
        }
        if (service.getUser(user.username()) != null){
            context.status(403);
            context.result(new Gson().toJson(Map.of("message", "Error: Already Taken")));
            return;
        }
        AuthData result = service.register(user);
        context.result(new Gson().toJson(result));
    }
    private void login(Context context) throws DataAccessException{
        UserData user = new Gson().fromJson(context.body(), UserData.class);
        if(user.username() == null || user.password() == null){
            context.status(400);
            context.result(new Gson().toJson(Map.of("message", "Error: Bad Request")));
            return;
        }
        UserData existingUser = service.getUser(user.username());
        if(existingUser == null || !user.password().equals(existingUser.password())){
                context.status(401);
                context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
                return;
        }
        AuthData result = service.login(user.username(), user.password());
        context.result(new Gson().toJson(result));
    }
    private void logout(Context context) throws DataAccessException {
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
            context.status(401);
            context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
        }
    }
    private void listGames(Context context) throws DataAccessException{
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
            context.status(401);
            context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
        }
    }
    private void createGame(Context context) throws DataAccessException{
        String authToken = context.header("authorization");
        GameData request = new Gson().fromJson(context.body(), GameData.class);
        String gameName = request.gameName();
        if(gameName == null){
            context.status(400);
            context.result(new Gson().toJson(Map.of("message", "Error: No game name")));
            return;
        }
        if (authToken == null || authToken.isBlank()) {
            context.status(401);
            context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
            return;
        }
        try {
            GameData game = service.createGame(authToken, gameName);
            context.status(200);
            context.result(new Gson().toJson(game));
        } catch (DataAccessException e) {
            context.status(401);
            context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
        }
    }
    private void joinGame(Context context) throws DataAccessException{
        String authToken = context.header("authorization");
        JoinGameRequest request = new Gson().fromJson(context.body(), JoinGameRequest.class);
        String playerColor = request.playerColor();
        int gameId = request.gameId();
        if(playerColor == null){
            context.status(400);
            context.result(new Gson().toJson(Map.of("message", "Error: No color")));
            return;
        }
        if (authToken == null || authToken.isBlank()) {
            context.status(401);
            context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
            return;
        }
        String username = service.getAuth(authToken).username();
        GameData game = service.getGame(gameId);
        if (game == null){
            context.status(400);
            context.result(new Gson().toJson(Map.of("message", "Error: Bad Request")));
            return;
        }
        try {
            service.joinGame(authToken, playerColor, username, gameId);
            context.status(200);
        } catch (DataAccessException e) {
            context.status(401);
            context.result(new Gson().toJson(Map.of("message", "Error: Unauthorized")));
        }
    }
    private void clear(Context context) throws DataAccessException{
        service.clear();
        context.status(200);
    }
}
