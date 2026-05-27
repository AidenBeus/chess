package dataaccess;

import model.AuthData;
import model.ChessList;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlDataAccessTests {

    private mySqlDataAccess dao;

    @BeforeEach
    public void setUp() throws DataAccessException {
        dao = new mySqlDataAccess();
        dao.clear();
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        dao.clear();
    }

    @Test
    @DisplayName("Register Success")
    public void registerSuccess() throws Exception {
        UserData user = new UserData("user1", "password", "user1@email.com");

        AuthData auth = dao.register(user);

        assertNotNull(auth);
        assertEquals("user1", auth.username());
        assertNotNull(auth.authToken());

        UserData savedUser = dao.getUser("user1");
        assertNotNull(savedUser);
        assertEquals("user1", savedUser.username());
        assertEquals("user1@email.com", savedUser.email());

        assertNotEquals("password", savedUser.password());
    }

    @Test
    @DisplayName("Register Duplicate User Fails")
    public void registerDuplicateUserFails() throws Exception {
        UserData user = new UserData("user1", "password", "user1@email.com");

        dao.register(user);

        assertThrows(DataAccessException.class, () -> dao.register(user));
    }

    @Test
    @DisplayName("Get User Success")
    public void getUserSuccess() throws Exception {
        UserData user = new UserData("user1", "password", "user1@email.com");
        dao.register(user);

        UserData result = dao.getUser("user1");

        assertNotNull(result);
        assertEquals("user1", result.username());
        assertEquals("user1@email.com", result.email());
    }

    @Test
    @DisplayName("Get Missing User Returns Null")
    public void getMissingUserReturnsNull() throws Exception {
        UserData result = dao.getUser("missingUser");

        assertNull(result);
    }

    @Test
    @DisplayName("Login Success")
    public void loginSuccess() throws Exception {
        dao.register(new UserData("user1", "password", "user1@email.com"));

        AuthData auth = dao.login("user1");

        assertNotNull(auth);
        assertEquals("user1", auth.username());
        assertNotNull(auth.authToken());

        AuthData savedAuth = dao.getAuth(auth.authToken());
        assertNotNull(savedAuth);
        assertEquals("user1", savedAuth.username());
    }

    @Test
    @DisplayName("Login Missing User Still Creates Auth")
    public void loginMissingUserStillCreatesAuth() throws Exception {
        AuthData auth = dao.login("missingUser");

        assertNotNull(auth);
        assertEquals("missingUser", auth.username());
        assertNotNull(auth.authToken());

        AuthData savedAuth = dao.getAuth(auth.authToken());
        assertNotNull(savedAuth);
        assertEquals("missingUser", savedAuth.username());
    }

    @Test
    @DisplayName("Get Auth Success")
    public void getAuthSuccess() throws Exception {
        AuthData auth = dao.register(new UserData("user1", "password", "user1@email.com"));

        AuthData result = dao.getAuth(auth.authToken());

        assertNotNull(result);
        assertEquals(auth.authToken(), result.authToken());
        assertEquals("user1", result.username());
    }

    @Test
    @DisplayName("Get Missing Auth Returns Null")
    public void getMissingAuthReturnsNull() throws Exception {
        AuthData result = dao.getAuth("bad-token");

        assertNull(result);
    }

    @Test
    @DisplayName("Logout Success")
    public void logoutSuccess() throws Exception {
        AuthData auth = dao.register(new UserData("user1", "password", "user1@email.com"));

        dao.logout(auth.authToken());

        AuthData result = dao.getAuth(auth.authToken());
        assertNull(result);
    }

    @Test
    @DisplayName("Logout Missing Token Does Not Crash")
    public void logoutMissingTokenDoesNotCrash() {
        assertDoesNotThrow(() -> dao.logout("bad-token"));
    }

    @Test
    @DisplayName("Create Game Success")
    public void createGameSuccess() throws Exception {
        GameData game = dao.createGame("game1");

        assertNotNull(game);
        assertTrue(game.gameID() > 0);
        assertEquals("game1", game.gameName());
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
        assertNotNull(game.game());
    }

    @Test
    @DisplayName("Create Game Null Name Fails")
    public void createGameNullNameFails() {
        assertThrows(DataAccessException.class, () -> dao.createGame(null));
    }

    @Test
    @DisplayName("Get Game Success")
    public void getGameSuccess() throws Exception {
        GameData game = dao.createGame("game1");

        GameData result = dao.getGame(game.gameID());

        assertNotNull(result);
        assertEquals(game.gameID(), result.gameID());
        assertEquals("game1", result.gameName());
        assertNull(result.whiteUsername());
        assertNull(result.blackUsername());
        assertNotNull(result.game());
    }

    @Test
    @DisplayName("Get Missing Game Returns Null")
    public void getMissingGameReturnsNull() throws Exception {
        GameData result = dao.getGame(999999);

        assertNull(result);
    }

    @Test
    @DisplayName("List Games Success")
    public void listGamesSuccess() throws Exception {
        dao.createGame("game1");
        dao.createGame("game2");

        ChessList result = dao.listGames();

        assertNotNull(result);
        assertEquals(2, result.games().size());
    }

    @Test
    @DisplayName("List Games Empty")
    public void listGamesEmpty() throws Exception {
        ChessList result = dao.listGames();

        assertNotNull(result);
        assertEquals(0, result.games().size());
    }

    @Test
    @DisplayName("Join Game White Success")
    public void joinGameWhiteSuccess() throws Exception {
        GameData game = dao.createGame("game1");

        dao.joinGame("WHITE", "user1", game.gameID());

        GameData updatedGame = dao.getGame(game.gameID());
        assertNotNull(updatedGame);
        assertEquals("user1", updatedGame.whiteUsername());
        assertNull(updatedGame.blackUsername());
    }

    @Test
    @DisplayName("Join Game Black Success")
    public void joinGameBlackSuccess() throws Exception {
        GameData game = dao.createGame("game1");

        dao.joinGame("BLACK", "user1", game.gameID());

        GameData updatedGame = dao.getGame(game.gameID());
        assertNotNull(updatedGame);
        assertNull(updatedGame.whiteUsername());
        assertEquals("user1", updatedGame.blackUsername());
    }

    @Test
    @DisplayName("Join Occupied White Fails")
    public void joinOccupiedWhiteFails() throws Exception {
        GameData game = dao.createGame("game1");

        dao.joinGame("WHITE", "user1", game.gameID());

        assertThrows(AlreadyTakenException.class,
                () -> dao.joinGame("WHITE", "user2", game.gameID()));
    }

    @Test
    @DisplayName("Join Occupied Black Fails")
    public void joinOccupiedBlackFails() throws Exception {
        GameData game = dao.createGame("game1");

        dao.joinGame("BLACK", "user1", game.gameID());

        assertThrows(AlreadyTakenException.class,
                () -> dao.joinGame("BLACK", "user2", game.gameID()));
    }

    @Test
    @DisplayName("Join Missing Game Fails")
    public void joinMissingGameFails() {
        assertThrows(AlreadyTakenException.class,
                () -> dao.joinGame("WHITE", "user1", 999999));
    }

    @Test
    @DisplayName("Join Invalid Color Fails")
    public void joinInvalidColorFails() throws Exception {
        GameData game = dao.createGame("game1");

        assertThrows(DataAccessException.class,
                () -> dao.joinGame("GREEN", "user1", game.gameID()));
    }

    @Test
    @DisplayName("Clear Success")
    public void clearSuccess() throws Exception {
        AuthData auth = dao.register(new UserData("user1", "password", "user1@email.com"));
        GameData game = dao.createGame("game1");

        dao.joinGame("WHITE", "user1", game.gameID());

        dao.clear();

        assertNull(dao.getUser("user1"));
        assertNull(dao.getAuth(auth.authToken()));
        assertNull(dao.getGame(game.gameID()));
        assertEquals(0, dao.listGames().games().size());
    }

}