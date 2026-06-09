package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Set;

import static ui.EscapeSequences.*;

public class BoardRenderer {

    public void render(ChessGame game, String color) {
        render(game, color, Set.of(), null);
    }

    public void render(ChessGame game, String color, Set<ChessPosition> highlights, String highlightColor) {
        ChessBoard board = game.getBoard();

        boolean whitePerspective = color.equalsIgnoreCase("WHITE");

        String[] columns = whitePerspective
                ? new String[]{"a", "b", "c", "d", "e", "f", "g", "h"}
                : new String[]{"h", "g", "f", "e", "d", "c", "b", "a"};

        int[] rows = whitePerspective
                ? new int[]{8, 7, 6, 5, 4, 3, 2, 1}
                : new int[]{1, 2, 3, 4, 5, 6, 7, 8};

        System.out.print(ERASE_SCREEN);

        System.out.println();
        printColumnLabels(columns);

        for (int row : rows) {
            printLabelCell(String.valueOf(row));

            for (String columnName : columns) {
                int col = columnName.charAt(0) - 'a' + 1;
                ChessPosition pos = new ChessPosition(row, col);

                boolean lightSquare = (row + col) % 2 != 0;
                boolean highlighted = highlights.contains(pos);

                if (highlighted && highlightColor != null) {
                    System.out.print(highlightColor);
                } else {
                    System.out.print(lightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_BLACK);
                }

                ChessPiece piece = board.getPiece(pos);
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
}