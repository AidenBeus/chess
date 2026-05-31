package client;

import ui.ServerFacade;

public class ChessClient {
    private ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }
}
