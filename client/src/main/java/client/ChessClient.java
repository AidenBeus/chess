package client;

import chess.*;
import com.google.gson.Gson;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.DeploymentException;
import model.GameData;
import model.UserData;
import org.glassfish.tyrus.client.ClientManager;
import ui.ServerFacade;
import ui.WebSocketFacade;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String authToken;
    private final List<GameData> listedGames = new ArrayList<>();
    private String color = "WHITE";
    private Integer currentGameId;
    private WebSocketFacade ws;
    private final Gson gson = new Gson();
    private volatile ChessGame currentGameState = new ChessGame();
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
            else if (state == State.INGAME){
                return switch (cmd) {
                    case "leave" -> leaveGame();
                    case "redrawchessboard" -> redraw();
                    case "makemove" -> makeMove();
//                    case "resign" -> resign();
//                    case "highlightlegalmoves" -> highlightLegalMoves();
                    case "quit" -> "quit";
                    default -> "valid commands\n" + help();
                };
            }
            else{
                return switch (cmd){  //This is the observe state
                    case "redrawchessboard" -> redraw();
                    case "leave" -> leaveGame();
                    case "quit" -> "quit";
                    default -> "valid commands\n" + help();
                };
            }
        } catch (ResponseException ex) {
            return ex.getMessage();
        } catch (IOException | URISyntaxException | DeploymentException e) {
            throw new RuntimeException(e);
        }
    }

    private String makeMove() throws IOException {
        if (ws == null || currentGameId == null) {
            return "You are not currently connected to a game.\n";
        }

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Piece location: ");
            String input = scanner.nextLine().trim();

            if (input.length() != 2) {
                return "Input must be in format like a1";
            }

            ChessPosition start = getPosition(input);

            if (start.getRow() < 1 || start.getRow() > 8 || start.getColumn() == -999) {
                return "Invalid position";
            }

            System.out.print("Move piece to: ");
            input = scanner.nextLine().trim();

            if (input.length() != 2) {
                return "Input must be in format like a1";
            }

            ChessPosition end = getPosition(input);

            if (end.getRow() < 1 || end.getRow() > 8 || end.getColumn() == -999) {
                return "Invalid position";
            }

            ChessGame game = currentGameState;

            ChessPiece piece = game.getBoard().getPiece(start);

            ChessGame.TeamColor playerColor =
                    color.equalsIgnoreCase("WHITE")
                            ? ChessGame.TeamColor.WHITE
                            : ChessGame.TeamColor.BLACK;

            if (game.getTeamTurn() != playerColor) {
                return "It is not your turn\n";
            }

            if (piece == null) {
                return "No piece at that square.\n";
            }

            if (color.equalsIgnoreCase("WHITE") &&
                    piece.getTeamColor() != ChessGame.TeamColor.WHITE) {
                return "Not your piece.\n";
            }

            if (color.equalsIgnoreCase("BLACK") &&
                    piece.getTeamColor() != ChessGame.TeamColor.BLACK) {
                return "Not your piece.\n";
            }

            ChessMove move = new ChessMove(start, end, null);

            var legalMoves = game.validMoves(start);

            if (legalMoves == null || !legalMoves.contains(move)) {
                return "Illegal move.\n";
            }

            MakeMoveCommand command =
                    new MakeMoveCommand(
                            authToken,
                            currentGameId,
                            move
                    );

            ws.sendCommand(gson.toJson(command));
            return "Move sent to server.\n";

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private ChessPosition getPosition(String input) {
        String letter = String.valueOf(input.charAt(0)).toLowerCase();
        int row = Character.getNumericValue(input.charAt(1));
        int col;
        switch (letter){
            case "a" -> col = 1;
            case "b" -> col = 2;
            case "c" -> col = 3;
            case "d" -> col = 4;
            case "e" -> col = 5;
            case "f" -> col = 6;
            case "g" -> col = 7;
            case "h" -> col = 8;
            default -> col = -999;
        }
        return new ChessPosition(row, col);
    }

    private String redraw() {
        drawBoard(color);
        return "Board Redrawn";
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

        return String.format("Created game '%s'.\n", game.gameName());
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

    private String playGame() throws ResponseException, DeploymentException, IOException, URISyntaxException {
        if (listedGames.isEmpty()) {
            return "Use the command listgames to populate the list and/or create a game\n";
        }

        Scanner scanner = new Scanner(System.in);

        String gameNumberText;
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
        currentGameId = selectedGame.gameID();

        server.joinGame(authToken, color, selectedGame.gameID());
        connectWebSocket();
        sendConnectCommand();
        System.out.println("Connected websocket");
        state = State.INGAME;
        return String.format(
                "Joined game '%s' as %s.\n",
                selectedGame.gameName(),
                color
        );
    }

    private String observeGame() throws DeploymentException, IOException, URISyntaxException {
        if (listedGames.isEmpty()) {
            return "Use the command listgames to populate the list and/or create a game\n";
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
        currentGameId = selectedGame.gameID();
        state = State.OBSERVE;
        color = "WHITE";
        connectWebSocket();
        sendConnectCommand();
        return String.format(
                "Observing game '%s'.\n",
                selectedGame.gameName()
        );
    }

    private void drawBoard(String color) {
        ChessBoard board = currentGameState.getBoard();

        boolean whitePerspective = color.equalsIgnoreCase("WHITE");

        String[] columns = whitePerspective
                ? new String[]{"a", "b", "c", "d", "e", "f", "g", "h"}
                : new String[]{"h", "g", "f", "e", "d", "c", "b", "a"};

        int[] rows = whitePerspective
                ? new int[]{8, 7, 6, 5, 4, 3, 2, 1}
                : new int[]{1, 2, 3, 4, 5, 6, 7, 8};

        System.out.print(ERASE_SCREEN);

        printColumnLabels(columns);

        for (int row : rows) {
            printLabelCell(String.valueOf(row));

            for (String columnName : columns) {
                int col = columnName.charAt(0) - 'a' + 1;

                boolean lightSquare = (row + col) % 2 != 0;
                System.out.print(lightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_BLACK);

                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                System.out.print(pieceToString(piece));
            }

            printLabelCell(String.valueOf(row));
            System.out.println();
        }

        printColumnLabels(columns);

        System.out.print(RESET_BG_COLOR);
        System.out.print(RESET_TEXT_COLOR);
    }

    private void printColumnLabels(String[] columns) {
        System.out.print(SET_BG_COLOR_DARK_GREY);
        System.out.print("  ");

        for (String column : columns) {
            printLabelCell(column);
        }

        System.out.print("    ");
        System.out.println();

        System.out.print(RESET_BG_COLOR);
        System.out.print(RESET_TEXT_COLOR);
    }

    private void printLabelCell(String label) {
        System.out.print(SET_BG_COLOR_DARK_GREY);
        System.out.print(SET_TEXT_COLOR_MAGENTA);
        System.out.print("  " + label + " ");
    }

    private String pieceToString(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }

        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            System.out.print(SET_TEXT_COLOR_RED);

            return switch (piece.getPieceType()) {
                case KING -> WHITE_KING;
                case QUEEN -> WHITE_QUEEN;
                case BISHOP -> WHITE_BISHOP;
                case KNIGHT -> WHITE_KNIGHT;
                case ROOK -> WHITE_ROOK;
                case PAWN -> WHITE_PAWN;
            };
        } else {
            System.out.print(SET_TEXT_COLOR_BLUE);

            return switch (piece.getPieceType()) {
                case KING -> BLACK_KING;
                case QUEEN -> BLACK_QUEEN;
                case BISHOP -> BLACK_BISHOP;
                case KNIGHT -> BLACK_KNIGHT;
                case ROOK -> BLACK_ROOK;
                case PAWN -> BLACK_PAWN;
            };
        }
    }

    public String leaveGame() throws IOException {
        if (ws == null || currentGameId == null) {
            return "You are not currently connected to a game.\n";
        }

        UserGameCommand leave =
                new UserGameCommand(
                        UserGameCommand.CommandType.LEAVE,
                        authToken,
                        currentGameId);

        ws.sendCommand(gson.toJson(leave));
        ws.close();
        ws = null;
        currentGameId = null;
        currentGameState = new ChessGame();
        state = State.SIGNEDIN;

        return "You have left the game";
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
        if (state == State.INGAME){
            return """
                    - help
                    - redrawchessboard
                    - makemove
                    - resign
                    - highlightlegalmoves
                    - leave
                    - quit
                    """;
        }
        else{
            return """
                    - help
                    - redrawchessboard
                    - leave
                    - quit
                    """;
        }
    }
    private void connectWebSocket() throws IOException, DeploymentException, URISyntaxException {
        ClientManager client = ClientManager.createClient();
        ws = new WebSocketFacade(this);

        client.connectToServer(
                ws,
                ClientEndpointConfig.Builder.create().build(),
                URI.create("ws://localhost:8080/ws")
        );
    }
    private void sendConnectCommand() throws IOException {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken,
                currentGameId
        );
        ws.sendCommand(gson.toJson(command));
    }
    public void handleLoadGame(ChessGame game) {
        currentGameState = game;
        drawBoard(color);
    }

    public void handleNotification(String message) {
        System.out.println("\nNotification: " + message);
        printPrompt();
    }

    public void handleError(String message) {
        System.out.println("\nError: " + message);
        printPrompt();
    }
}
