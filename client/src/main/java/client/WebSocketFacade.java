package client;

import com.google.gson.Gson;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Objects;

public class WebSocketFacade extends Endpoint {
    private final ChessClient client;
    private final Gson gson = new Gson();
    private Session session;

    public WebSocketFacade(ChessClient client) {
        this.client = client;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        session.addMessageHandler(String.class, this::handleMessage);
    }

    private void handleMessage(String message) {
        var json = gson.fromJson(message, com.google.gson.JsonObject.class);
        String type = json.get("serverMessageType").getAsString();

        switch (type) {
            case "LOAD_GAME" -> {
                var game = gson.fromJson(json.get("game"), chess.ChessGame.class);
                client.handleLoadGame(game);
            }
            case "NOTIFICATION" -> {
                String notification = json.get("message").getAsString();
                client.handleNotification(notification);
            }
            case "ERROR" -> {
                String error = json.get("errorMessage").getAsString();
                client.handleError(error);
            }
            default -> System.out.println("Unknown server message: " + message);
        }
    }

    public void sendCommand(String json) throws IOException {
        session.getBasicRemote().sendText(json);
    }

    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}