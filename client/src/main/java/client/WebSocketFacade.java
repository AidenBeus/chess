package client;

import com.google.gson.Gson;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;

import java.io.IOException;

public class WebSocketFacade extends Endpoint {
    private Session session;
    private final Gson gson = new Gson();

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;

        session.addMessageHandler(String.class, message -> {
            System.out.println("Server says: " + message);
        });
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