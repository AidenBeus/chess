package model;

import chess.ChessGame;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;

public class ChessList extends ArrayList<ChessGame> {
    public ChessList() {

    }

    public ChessList(Collection<ChessGame> games) {
        super(games);
    }

    public String toString() {
        return new Gson().toJson(this.toArray());
}
    }
