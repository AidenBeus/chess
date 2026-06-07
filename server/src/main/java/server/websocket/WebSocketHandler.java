package server.websocket;

import com.google.gson.Gson;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsErrorContext;
import io.javalin.websocket.WsMessageContext;
import service.ChessService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;

public class WebSocketHandler {
    private final Gson gson = new Gson();
    private final ConnectionManager connections;

    public WebSocketHandler(ChessService service) {
        this.connections = new ConnectionManager(service);
    }

    public void onConnect(WsConnectContext ctx) {
    }

    public void onMessage(WsMessageContext ctx) {
        try {
            UserGameCommand base = gson.fromJson(ctx.message(), UserGameCommand.class);

            switch (base.getCommandType()) {
                case CONNECT -> connections.connect(
                        base.getAuthToken(),
                        base.getGameID(),
                        ctx.session
                );

                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = gson.fromJson(ctx.message(), MakeMoveCommand.class);
                    connections.makeMove(moveCommand, ctx.session);
                }

                case LEAVE -> connections.leave(ctx.session, base);
                case RESIGN -> connections.resign(ctx.session, base);
            }
        } catch (Exception e) {
            try {
                ctx.send(gson.toJson(new ErrorMessage("Error: " + e.getMessage())));
            } catch (Exception ignored) {
            }
        }
    }

    public void onClose(WsCloseContext ctx) {
        connections.cleanup(ctx.session);
    }
    public void onError(WsErrorContext ctx) {
        if (ctx.error() != null) {
            ctx.error().printStackTrace();
        }
    }
}