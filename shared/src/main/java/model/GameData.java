package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public GameData changeWhite(String username){
        return new GameData(gameID, username, blackUsername, gameName, game);
    }
    public GameData changeBlack(String username){
        return new GameData(gameID, whiteUsername, username, gameName, game);
    }
}
