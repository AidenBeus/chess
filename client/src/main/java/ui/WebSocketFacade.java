package ui;

import client.ChessClient;
import com.google.gson.Gson;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;

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

        ServerMessage base =
                gson.fromJson(message, ServerMessage.class);

        switch (base.getServerMessageType()) {

            case LOAD_GAME -> {
                LoadGameMessage load =
                        gson.fromJson(message, LoadGameMessage.class);

                client.handleLoadGame(load.getGame());
            }

            case NOTIFICATION -> {
                NotificationMessage notification =
                        gson.fromJson(message, NotificationMessage.class);

                client.handleNotification(
                        notification.getMessage()
                );
            }

            case ERROR -> {
                ErrorMessage error =
                        gson.fromJson(message, ErrorMessage.class);

                client.handleError(
                        error.getErrorMessage()
                );
            }
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