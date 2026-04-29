package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
            return pieceColor;

    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        List<ChessMove> valMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        if (piece.getPieceType() == PieceType.BISHOP) {
            int i = row;
            int j = col;
            do{ //down left
                i--;
                j--;
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
            }while(i > 1 && j > 1);
            i = row;
            j = col;
            do{ //down right
                i--;
                j++;
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
            }while(i > 1 && j < 8);
            i = row;
            j = col;
            do{ //up left
                i++;
                j--;
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
            }while(i < 8 && j > 1);
            i = row;
            j = col;
            do{ //up right
                i++;
                j++;
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
            }while(i < 8 && j < 8);
        }
        return valMoves;
    }
}
