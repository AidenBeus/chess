package client;

import chess.ResponseException;
import model.AuthData;
import model.ChessList;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;
import ui.ServerFacade;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static String serverUrl;

    private ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        serverUrl = "http://localhost:" + port;
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/db"))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        facade = new ServerFacade(serverUrl);
    }

    @Test
    public void registerPositive() throws Exception {
        AuthData auth = facade.register(new UserData("aiden", "password", "aiden@email.com"));

        assertNotNull(auth);
        assertEquals("aiden", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    public void registerNegativeDuplicateUsername() throws Exception {
        facade.register(new UserData("aiden", "password", "aiden@email.com"));

        ResponseException ex = assertThrows(
                ResponseException.class,
                () -> facade.register(new UserData("aiden", "differentPassword", "Aiden!@email.com"))
        );

        assertTrue(ex.getMessage().toLowerCase().contains("already") ||
                ex.getMessage().toLowerCase().contains("taken"));
    }

    @Test
    public void signInPositive() throws Exception {
        facade.register(new UserData("julia", "password", "julia@email.com"));

        AuthData auth = facade.signIn(new UserData("julia", "password", null));

        assertNotNull(auth);
        assertEquals("julia", auth.username());
        assertNotNull(auth.authToken());
        assertFalse(auth.authToken().isBlank());
    }

    @Test
    public void signInNegativeWrongPassword() throws Exception {
        facade.register(new UserData("parker", "password", "parker@email.com"));

        ResponseException ex = assertThrows(
                ResponseException.class,
                () -> facade.signIn(new UserData("parker", "wrongPassword", null))
        );

        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized")
                || ex.getMessage().toLowerCase().contains("incorrect"));
    }

    @Test
    public void logoutPositive() throws Exception {
        AuthData auth = facade.register(new UserData("collin", "password", "collin@email.com"));

        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutNegativeInvalidToken() {
        ResponseException ex = assertThrows(
                ResponseException.class,
                () -> facade.logout("not-a-real-token")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    public void createGamePositive() throws Exception {
        AuthData auth = facade.register(new UserData("alan", "password", "alan@email.com"));

        GameData game = facade.createGame(auth.authToken(), "my game");

        assertNotNull(game);
        assertNotNull(game.gameID());
        assertEquals("my game", game.gameName());
    }

    @Test
    public void createGameNegativeInvalidToken() throws Exception {
        ResponseException ex = assertThrows(
                ResponseException.class,
                () -> facade.createGame("bad-token", "my game")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    public void listGamesPositive() throws Exception {
        AuthData auth = facade.register(new UserData("oliver", "password", "oliver@email.com"));
        facade.createGame(auth.authToken(), "first game");

        ChessList games = facade.listGames(auth.authToken());

        assertNotNull(games);
        assertNotNull(games.games());
        assertEquals(1, games.games().size());
        GameData firstGame = games.games().iterator().next();

        assertEquals("first game", firstGame.gameName());
    }

    @Test
    public void listGamesNegativeInvalidToken() {
        ResponseException ex = assertThrows(
                ResponseException.class,
                () -> facade.listGames("bad-token")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    public void joinGamePositive() throws Exception {
        AuthData auth = facade.register(new UserData("kal", "password", "kal@email.com"));
        GameData game = facade.createGame(auth.authToken(), "joinable game");

        assertDoesNotThrow(() -> facade.joinGame(auth.authToken(), "WHITE", game.gameID()));
    }

    @Test
    public void joinGameNegativeAlreadyTaken() throws Exception {
        AuthData whiteUser = facade.register(new UserData("white", "password", "white@email.com"));
        AuthData blackUser = facade.register(new UserData("black", "password", "black@email.com"));
        GameData game = facade.createGame(whiteUser.authToken(), "competitive game");

        facade.joinGame(whiteUser.authToken(), "WHITE", game.gameID());

        ResponseException ex = assertThrows(
                ResponseException.class,
                () -> facade.joinGame(blackUser.authToken(), "WHITE", game.gameID())
        );

        assertTrue(ex.getMessage().toLowerCase().contains("already")
                || ex.getMessage().toLowerCase().contains("taken"));
    }
}