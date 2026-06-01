package client;

import chess.ResponseException;
import model.GameData;
import model.UserData;
import ui.ServerFacade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;
import static ui.EscapeSequences.SET_TEXT_COLOR_GREEN;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String authToken;
    private final List<GameData> listedGames = new ArrayList<>();

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println(" Welcome to the Chess Server. Register or sign in to start.");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();
            try {
                result = eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }
    private void printPrompt() {
        System.out.print("\n"  + ">>> " + SET_TEXT_COLOR_GREEN);
    }
    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            if (state == State.SIGNEDOUT) {
                return switch (cmd) {
                    case "quit" -> "quit";
                    case "login" -> signIn();
                    case "register" -> register();
                    default -> "valid commands\n" + help();
                };
            }
            else if (state == State.SIGNEDIN){
                return switch (cmd) {
                    case "logout" -> logout();
                    case "creategame" -> createGame();
                    case "listgames" -> listGames();
                    case "playgame" -> playGame();
                    case "observegame" -> observeGame();
                    case "quit" -> "quit";
                    default -> "valid commands\n" + help();
                };
            }
            else{
                return switch (cmd) {
                    case"ssdsd" -> observeGame();
                    case "quit" -> "quit";
                    default -> "valid commands\n" + help();
                };
            }
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }


    public String signIn() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        UserData user = new UserData(username, password, null);

        try {
            var authData = server.signIn(user);

            state = State.SIGNEDIN;
            authToken = authData.authToken();

            return String.format("You signed in as %s.\n", authData.username());

        } catch (ResponseException ex) {
            return "Login failed: incorrect username or password.\n";
        }
    }

    public String register() throws ResponseException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        UserData user = new UserData(username, password, email);
        var authData = server.register(user);
        authToken = authData.authToken();
        state = State.SIGNEDIN;
        return String.format("You signed in as %s.", username);
    }

    private String logout() throws ResponseException {
        server.logout(authToken);
        state = State.SIGNEDOUT;
        return "You signed out" + "\n" + " Welcome to the Chess Server. Register or sign in to start.";
    }

    private String createGame() throws ResponseException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your desired game name: ");
        String gameName = scanner.nextLine();
        var game = server.createGame(authToken, gameName);

        return String.format("Created game '%s' with ID %d.\n", game.gameName(), game.gameID());
    }

    private String listGames() throws ResponseException {
        var chessList = server.listGames(authToken);

        listedGames.clear();

        if (chessList.games() == null || chessList.games().isEmpty()) {
            return "No games found.\n";
        }

        listedGames.addAll(chessList.games());

        StringBuilder result = new StringBuilder();

        int number = 1;
        for (var game : listedGames) {
            result.append(String.format(
                    "%d. Name: %s | White: %s | Black: %s%n",
                    number++,
                    game.gameName(),
                    game.whiteUsername() == null ? "empty" : game.whiteUsername(),
                    game.blackUsername() == null ? "empty" : game.blackUsername()
            ));
        }

        return result.toString();
    }

    private String playGame() throws ResponseException {
        if (listedGames.isEmpty()) {
            return "Use the command listgames to populate the list or create a game\n";
        }

        Scanner scanner = new Scanner(System.in);

        String gameNumberText;
        String color;

        System.out.print("Enter game number: ");
        gameNumberText = scanner.nextLine();

        System.out.print("Enter color WHITE or BLACK: ");
        color = scanner.nextLine();

        int gameNumber;
        try {
            gameNumber = Integer.parseInt(gameNumberText);
        } catch (NumberFormatException e) {
            return "Invalid game number. Please enter a number from listgames.\n";
        }

        if (gameNumber < 1 || gameNumber > listedGames.size()) {
            return "Invalid game number. Please choose a number from listgames.\n";
        }

        color = color.toUpperCase();

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return "Invalid color. Please enter WHITE or BLACK.\n";
        }

        GameData selectedGame = listedGames.get(gameNumber - 1);

        server.joinGame(authToken, color, selectedGame.gameID());

        state = State.INGAME;

        return String.format(
                "Joined game '%s' as %s.\n",
                selectedGame.gameName(),
                color
        );
    }

    private String observeGame() {
        if (listedGames.isEmpty()) {
            return "Please run listgames first so I know which game numbers are available.\n";
        }

        Scanner scanner = new Scanner(System.in);

        String gameNumberText;

        System.out.print("Enter game number: ");
        gameNumberText = scanner.nextLine();

        int gameNumber;
        try {
            gameNumber = Integer.parseInt(gameNumberText);
        } catch (NumberFormatException e) {
            return "Invalid game number. Please enter a number from listgames.\n";
        }

        if (gameNumber < 1 || gameNumber > listedGames.size()) {
            return "Invalid game number. Please choose a number from listgames.\n";
        }

        GameData selectedGame = listedGames.get(gameNumber - 1);

        return String.format(
                "Observing game '%s'.\n",
                selectedGame.gameName()
        );
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - login
                    - register
                    - help
                    - quit
                    """;
        }
        if (state == State.SIGNEDIN) {
            return """
                    - help
                    - logout
                    - creategame
                    - listgames
                    - playgame 
                    - observegame 
                    """;
        }
        else{
            return """
                    - help
                    - quit
                    """;
        }
    }
}
