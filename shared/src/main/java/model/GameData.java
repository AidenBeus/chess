package model;

import chess.ChessGame;

public record GameData(
        Integer gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessGame game,
        Boolean over
) {
    public GameData(
            Integer gameID,
            String whiteUsername,
            String blackUsername,
            String gameName,
            ChessGame game
    ) {
        this(gameID, whiteUsername, blackUsername, gameName, game, false);
    }

    public GameData changeWhite(String username) {
        return new GameData(gameID, username, blackUsername, gameName, game, over);
    }

    public GameData changeBlack(String username) {
        return new GameData(gameID, whiteUsername, username, gameName, game, over);
    }

    public boolean isGameOver() {
        return Boolean.TRUE.equals(over);
    }
}