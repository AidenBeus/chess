package server;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import service.ChessService;

public class Server {

    private final Javalin javalin;
    private final ChessService service;

    public Server() {
        service = (new ChessService(new MemoryDataAccess()));
        javalin = Javalin.create(config -> config.staticFiles.add("web"))
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

    private void clear(Context context) throws DataAccessException{
        service.clear();
        context.status(200);
    }
}
