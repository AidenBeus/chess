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
    public boolean sameTeam(ChessPiece original, ChessPiece newSpot){
        //if same team color, can't move in that direction
        return newSpot.getTeamColor().equals(original.pieceColor);
    }
    private List<ChessMove> getBishopMoves (ChessBoard board, ChessPosition myPosition,
                                           ChessPiece piece,List<ChessMove> valMoves, int row, int col){
        int i = row;
        int j = col;
        do{ //down left
            i--;
            j--;
            if(i < 1 || j < 1){
                break;
            }
            ChessPiece spot = board.getPiece(new ChessPosition(i, j));
            if(spot == null){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
            }
            else if (sameTeam(piece, spot)){
                break;
            }
            else{
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
                break;
            }

        }while(i > 1 && j > 1);
        i = row;
        j = col;
        do{ //down right
            i--;
            j++;
            if(i < 1 || j > 8){
                break;
            }
            ChessPiece spot = board.getPiece(new ChessPosition(i, j));
            if(spot == null){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
            }
            else if (sameTeam(piece, spot)){
                break;
            }
            else{
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
                break;
            }
        }while(i > 1 && j < 8);
        i = row;
        j = col;
        do{ //up left
            i++;
            j--;
            if(i > 8 || j < 1){
                break;
            }
            ChessPiece spot = board.getPiece(new ChessPosition(i, j));
            if(spot == null){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
            }
            else if (sameTeam(piece, spot)){
                break;
            }
            else{
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
                break;
            }
        }while(i < 8 && j > 1);
        i = row;
        j = col;
        do{ //up right
            i++;
            j++;
            if(i > 8 || j > 8){
                break;
            }
            ChessPiece spot = board.getPiece(new ChessPosition(i, j));
            if(spot == null){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
            }
            else if (sameTeam(piece, spot)){
                break;
            }
            else{
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, j), null));
                break;
            }
        }while(i < 8 && j < 8);
        return valMoves;
    }
    private List<ChessMove> getKingMoves (ChessBoard board, ChessPosition myPosition,
                                           ChessPiece piece,List<ChessMove> valMoves, int row, int col){
        if (row - 1 >= 1 && col >= 1) {
            ChessPiece spot = board.getPiece(new ChessPosition(row - 1, col - 1)); //below to the left
                if (spot == null || !sameTeam(spot, piece)) {
                    valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col - 1), null));
                }
        }
        if (row >= 1 && col>=1) {
            ChessPiece spot = board.getPiece(new ChessPosition(row, col - 1));
            //same row , col to the left
            if (spot == null || !sameTeam(spot, piece)) {
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row, col - 1), null));
            }
        }
        if (row <=8 && col>=1) {
            ChessPiece spot = board.getPiece(new ChessPosition(row, col - 1));
            //up to the left
            if (spot == null || !sameTeam(spot, piece)) {
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col - 1), null));
            }
        }
        if (row + 1 <= 8 && col>=1) {
            ChessPiece spot = board.getPiece(new ChessPosition(row + 1, col));
            if (spot == null || !sameTeam(spot, piece)) {
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col), null));
            }
        }
        if (row + 1 <= 8 && col + 1 <=8 ){
            ChessPiece spot = board.getPiece(new ChessPosition(row + 1, col + 1));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col + 1), null));
            }
        }
        if (row <= 8 && col + 1 <= 8){
            ChessPiece spot = board.getPiece(new ChessPosition(row, col + 1));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row, col + 1), null));
            }
        }
        if (row - 1 >= 1 && col + 1 <= 8){
            ChessPiece spot = board.getPiece(new ChessPosition(row - 1, col + 1));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col + 1), null));
            }
        }
        if (row - 1 >= 1 && col <= 8){
        ChessPiece spot = board.getPiece(new ChessPosition(row - 1, col));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col), null));
            }
        }
        return valMoves;
    }

    private List<ChessMove> getKnightMoves (ChessBoard board, ChessPosition myPosition,
                                          ChessPiece piece,List<ChessMove> valMoves, int row, int col){
        if (row - 1 >= 1 && col - 2 >=1){
            ChessPiece spot = board.getPiece(new ChessPosition(row - 1, col - 2));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col - 2), null));
            }
        }
        if (row + 1 <= 8 && col - 2 >=1){
            ChessPiece spot = board.getPiece(new ChessPosition(row + 1, col - 2));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col - 2), null));
            }
        }
        if (row + 1 <= 8 && col + 2 <= 8){
            ChessPiece spot = board.getPiece(new ChessPosition(row + 1, col + 2));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col + 2), null));
            }
        }
        if (row - 1 >= 1 && col + 2 <= 8){
            ChessPiece spot = board.getPiece(new ChessPosition(row - 1, col + 2));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col + 2), null));
            }
        }
        if (row - 2 >= 1 && col - 1 >= 1){
            ChessPiece spot = board.getPiece(new ChessPosition(row - 2, col - 1));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 2, col - 1), null));
            }
        }
        if (row + 2 <= 8 && col - 1 >= 1){
            ChessPiece spot = board.getPiece(new ChessPosition(row + 2, col - 1));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 2, col - 1), null));
            }
        }
        if (row + 2 <= 8 && col + 1 <= 8){
            ChessPiece spot = board.getPiece(new ChessPosition(row + 2, col + 1));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 2, col + 1), null));
            }
        }
        if (row - 2 >= 1 && col + 1 <= 8){
            ChessPiece spot = board.getPiece(new ChessPosition(row - 2, col + 1));
            if (spot == null || !sameTeam(spot, piece)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 2, col + 1), null));
            }
        }
        return valMoves;
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
            return getBishopMoves(board, myPosition, piece, valMoves, row, col);
        }
        if (piece.getPieceType() == PieceType.KING)
            return getKingMoves(board, myPosition, piece, valMoves, row, col);
        if (piece.getPieceType() == PieceType.KNIGHT)
            return getKnightMoves(board, myPosition, piece, valMoves, row, col);
        return null;
    }
}
