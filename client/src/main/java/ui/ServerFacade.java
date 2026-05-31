package ui;

import chess.ResponseException;
import com.google.gson.Gson;
import model.AuthData;
import model.ChessList;
import model.GameData;
import model.UserData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    public ServerFacade(String url) {
        serverUrl = url;
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder() .uri(URI.create(serverUrl + path)) .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        if (request != null) { return HttpRequest.BodyPublishers.ofString(new Gson().toJson(request));
        }
        else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();

        if (!isSuccessful(status)) {
            String message = "Error: HTTP " + status + " body: " + response.body();

            var body = response.body();
            if (body != null && !body.isBlank()) {
                try {
                    var error = new Gson().fromJson(body, java.util.Map.class);
                    Object errorMessage = error.get("message");

                    if (errorMessage != null) {
                        message = errorMessage.toString();
                    }
                } catch (Exception ignored) {
                    message = body;
                }
            }

            throw new ResponseException(ResponseException.Code.ServerError, message);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }


    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

    public AuthData signIn(UserData user) throws ResponseException {
        var request = buildRequest("POST", "/session", user);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public AuthData register(UserData user) throws ResponseException {
        var request = buildRequest("POST", "/user", user);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        var request = buildRequest("DELETE", "/session", authToken);
        sendRequest(request);
    }

    public GameData createGame(String authToken, String gameName) throws ResponseException {
        GameData gameRequest = new GameData(null, null, null, gameName, null);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .header("Content-Type", "application/json")
                .header("authorization", authToken)
                .POST(makeRequestBody(gameRequest))
                .build();

        var response = sendRequest(request);
        return handleResponse(response, GameData.class);
    }

    public ChessList listGames(String authToken) throws ResponseException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/game"))
                .header("authorization", authToken)
                .GET()
                .build();
        var response = sendRequest(request);
        return handleResponse(response, ChessList.class);
    }
}
