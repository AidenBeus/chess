package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsMessageContext;
import service.ChessService;
import websocket.commands.UserGameCommand;

import java.io.IOException;

public class WebSocketHandler {
    private final Gson gson = new Gson();
    private final ConnectionManager connections;

    public WebSocketHandler(ChessService service) {
        this.connections = new ConnectionManager(service);
    }

    public void onMessage(WsMessageContext ctx) throws DataAccessException {
        UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT ->
                    connections.connect(
                            command.getAuthToken(),
                            command.getGameID(),
                            ctx::send
                    );

            case MAKE_MOVE -> connections.makeMove(ctx.session, command);
            case LEAVE -> connections.leave(ctx.session, command);
            case RESIGN -> connections.resign(ctx.session, command);
        }
    }

    public void onClose(WsCloseContext ctx) {
        connections.cleanup(ctx.session);
    }
}