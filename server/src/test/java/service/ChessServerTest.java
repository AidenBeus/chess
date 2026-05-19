package service;

import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChessServerTest {

    private MemoryDataAccess dataAccess;
    private ChessService service;

    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        service = new ChessService(dataAccess);
    }

    private AuthData registerUser(String username) throws Exception {
        return service.register(new UserData(username, "password", username + "@gmail.com"));
    }

    @Test
    void registerPositive() throws Exception {
        AuthData auth = registerUser("Aiden");

        assertNotNull(auth);
        assertNotNull(service.getUser("Aiden"));
    }

    @Test
    void registerNegative() throws Exception {
        UserData user = new UserData("Aiden", "password", "Aiden@gmail.com");
        service.register(user);

        assertThrows(AlreadyTakenException.class, () -> service.register(user));
    }

    @Test
    void getUserPositive() throws Exception {
        registerUser("Aiden");

        assertNotNull(service.getUser("Aiden"));
    }

    @Test
    void getUserNegative() throws DataAccessException {
        assertNull(service.getUser("missing"));
    }

    @Test
    void loginPositive() throws Exception {
        registerUser("Aiden");

        AuthData auth = service.login("Aiden", "password");

        assertNotNull(auth);
        assertNotNull(service.getAuth(auth.authToken()));
    }

    @Test
    void loginNegative() throws Exception {
        registerUser("Aiden");

        assertThrows(DataAccessException.class, () -> service.login("Aiden", "wrong-password"));
    }

    @Test
    void getAuthPositive() throws Exception {
        AuthData auth = registerUser("Aiden");

        assertNotNull(service.getAuth(auth.authToken()));
    }

    @Test
    void getAuthNegative() throws DataAccessException {
        assertNull(service.getAuth("missing-token"));
    }

    @Test
    void logoutPositive() throws Exception {
        AuthData auth = registerUser("Aiden");

        service.logout(auth.authToken());

        assertNull(service.getAuth(auth.authToken()));
    }

    @Test
    void logoutNegative() {
        assertThrows(DataAccessException.class, () -> service.logout("missing-token"));
    }

    @Test
    void clearPositive() throws Exception {
        AuthData auth = registerUser("Aiden");
        GameData game = service.createGame(auth.authToken(), "game-one");

        assertNotNull(service.getUser("Aiden"));
        assertNotNull(service.getAuth(auth.authToken()));
        assertNotNull(service.getGame(game.gameID()));

        service.clear();

        assertNull(service.getUser("Aiden"));
        assertNull(service.getAuth(auth.authToken()));
        assertNull(service.getGame(game.gameID()));
        assertTrue(dataAccess.listGames().games().isEmpty());
    }

    @Test
    void createGamePositive() throws Exception {
        AuthData auth = registerUser("Aiden");

        GameData game = service.createGame(auth.authToken(), "game-one");

        assertNotNull(game);
        assertTrue(game.gameID() > 0);
        assertEquals("game-one", game.gameName());
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    void createGameNegative() {
        assertThrows(DataAccessException.class, () -> service.createGame("bad-token", "game-one"));
    }

    @Test
    void listGamesPositive() throws Exception {
        AuthData auth = registerUser("Aiden");
        service.createGame(auth.authToken(), "game-one");
        service.createGame(auth.authToken(), "game-two");

        var games = service.listGames(auth.authToken()).games();

        assertEquals(2, games.size());
        assertTrue(games.stream().anyMatch(game -> "game-one".equals(game.gameName())));
        assertTrue(games.stream().anyMatch(game -> "game-two".equals(game.gameName())));
    }

    @Test
    void listGamesNegative() {
        assertThrows(DataAccessException.class, () -> service.listGames("bad-token"));
    }

    @Test
    void joinGamePositive() throws Exception {
        AuthData auth = registerUser("Aiden");
        GameData game = service.createGame(auth.authToken(), "game-one");

        service.joinGame(auth.authToken(), "WHITE", "Aiden", game.gameID());

        GameData updated = service.getGame(game.gameID());
        assertEquals("Aiden", updated.whiteUsername());
        assertNull(updated.blackUsername());
    }

    @Test
    void joinGameNegativeAlreadyTaken() throws Exception {
        AuthData alice = registerUser("Aiden");
        AuthData bob = registerUser("Julia");
        GameData game = service.createGame(alice.authToken(), "game-one");

        service.joinGame(alice.authToken(), "WHITE", "Aiden", game.gameID());

        assertThrows(AlreadyTakenException.class,
                () -> service.joinGame(bob.authToken(), "WHITE", "Julia", game.gameID()));
    }

    @Test
    void getGamePositive() throws Exception {
        AuthData auth = registerUser("Aiden");
        GameData game = service.createGame(auth.authToken(), "game-one");

        assertNotNull(service.getGame(game.gameID()));
    }

    @Test
    void getGameNegative() throws DataAccessException {
        assertNull(service.getGame(999999));
    }
}