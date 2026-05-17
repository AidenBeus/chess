package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import model.AuthData;
import model.UserData;
import org.jetbrains.annotations.NotNull;
import service.ChessService;

public class Server {

    private final Javalin javalin;
    private final ChessService service;

    public Server() {
        service = (new ChessService(new MemoryDataAccess()));
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .post("/user", this::register)
                .post("/session", this::login)
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
        AuthData result = service.register(user);
        context.result(new Gson().toJson(result));
    }
    private void login(Context context) throws DataAccessException{
        UserData user = new Gson().fromJson(context.body(), UserData.class);
        AuthData result = service.login(user.username(), user.password());
        context.result(new Gson().toJson(result));
    }
    private void clear(Context context) throws DataAccessException{
        service.clear();
        context.status(200);
    }
}
