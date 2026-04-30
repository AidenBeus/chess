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
    private List<ChessMove> getBishopMoves (ChessBoard board,
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
        if (row - 1 >= 1 && col - 1 >= 1) {
            ChessPiece spot = board.getPiece(new ChessPosition(row - 1, col - 1)); //below to the left
            if (spot == null || !sameTeam(spot, piece)) {
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col - 1), null));
            }
        }
        if (row >= 1 && col-1>=1) {
            ChessPiece spot = board.getPiece(new ChessPosition(row, col - 1));
            //same row , col to the left
            if (spot == null || !sameTeam(spot, piece)) {
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row, col - 1), null));
            }
        }
        if (row <=8 && col - 1>=1) {
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
    private List<ChessMove> getPawnMoves (ChessBoard board, ChessPiece piece,List<ChessMove> valMoves, int row, int col) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {  //white
            if (row == 2) {
                ChessPiece closeSpot = board.getPiece(new ChessPosition(row + 1, col));
                ChessPiece spot = board.getPiece(new ChessPosition(row + 2, col));
                if (closeSpot == null && spot == null){
                    valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 2, col), null));
                }
            }
            if (row + 1 <= 8) {
                ChessPiece spot = board.getPiece(new ChessPosition(row + 1, col));
                if (spot == null) {
                    if (row + 1 == 8) {
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col), PieceType.BISHOP));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col), PieceType.KNIGHT));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col), PieceType.QUEEN));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col), PieceType.ROOK));
                    } else{
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col), null));
                    }
                }
                if (col - 1 >= 1) {
                    ChessPiece diag = board.getPiece(new ChessPosition(row + 1, col - 1));
                    if (diag != null && !sameTeam(piece, diag) && row + 1 == 8) {
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col - 1), PieceType.BISHOP));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col - 1), PieceType.KNIGHT));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col - 1), PieceType.QUEEN));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col - 1), PieceType.ROOK));
                    } else if (diag != null && !sameTeam(piece, diag)){
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col - 1), null));
                    }
                }
                if (col + 1 <= 8) {
                    ChessPiece diag = board.getPiece(new ChessPosition(row + 1, col + 1));
                    if (diag != null && !sameTeam(piece, diag) && row + 1 == 8) {
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col + 1), PieceType.BISHOP));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col + 1), PieceType.KNIGHT));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col + 1), PieceType.QUEEN));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col + 1), PieceType.ROOK));
                    } else if (diag != null && !sameTeam(piece, diag)){
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row + 1, col + 1), null));
                    }
                }
            }
        }
        else { //black
            if (row == 7) {
                ChessPiece closeSpot = board.getPiece(new ChessPosition(row - 1, col));
                ChessPiece spot = board.getPiece(new ChessPosition(row - 2, col));
                if (closeSpot == null && spot == null){
                    valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 2, col), null));
                }
            }
            if (row - 1 >= 1) {
                ChessPiece spot = board.getPiece(new ChessPosition(row - 1, col));
                if (spot == null) {
                    if (row - 1 == 1) {
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col), PieceType.BISHOP));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col), PieceType.KNIGHT));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col), PieceType.QUEEN));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col), PieceType.ROOK));
                    } else{
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col), null));
                    }
                }
                if (col - 1 >= 1) {
                    ChessPiece diag = board.getPiece(new ChessPosition(row - 1, col - 1));
                    if (diag != null && !sameTeam(piece, diag) && row - 1 == 1) {
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col - 1), PieceType.BISHOP));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col - 1), PieceType.KNIGHT));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col - 1), PieceType.QUEEN));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col - 1), PieceType.ROOK));
                    } else if (diag != null && !sameTeam(piece, diag)){
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col - 1), null));
                    }
                }
                if (col + 1 <= 8) {
                    ChessPiece diag = board.getPiece(new ChessPosition(row - 1, col + 1));
                    if (diag != null && !sameTeam(piece, diag) && row - 1 == 1) {
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col + 1), PieceType.BISHOP));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col + 1), PieceType.KNIGHT));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col + 1), PieceType.QUEEN));
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col + 1), PieceType.ROOK));
                    } else if (diag != null && !sameTeam(piece, diag)){
                        valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row - 1, col + 1), null));
                    }
                }
            }
        }
        return valMoves;
    }

    private List<ChessMove> getQueenMoves (ChessBoard board,
                                           ChessPiece piece,List<ChessMove> valMoves, int row, int col) {
        List<ChessMove> valMoves1 = getRookMoves(board, piece, valMoves, row, col);
        List<ChessMove> valMoves2 = getBishopMoves(board, piece, valMoves, row, col);
        for (ChessMove temp : valMoves2) {
            if (!valMoves1.contains(temp)){
                valMoves1.add(temp);
            }
        }
        return valMoves1;
    }
    private List<ChessMove> getRookMoves (ChessBoard board, ChessPiece piece,List<ChessMove> valMoves,
                                          int row, int col) {
        for(int i = row - 1; i >=1; i--){
            ChessPiece space = board.getPiece(new ChessPosition(i, col));
            if (space == null){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, col), null));
            }
            else if(!sameTeam(piece, space)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(i, col), null));
                break;
            }
            else{
                break;
            }
        }
        for(int k = row + 1; k <=8; k++) {
            ChessPiece space = board.getPiece(new ChessPosition(k, col));
            if (space == null){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(k, col), null));
            }
            else if(!sameTeam(piece, space)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(k, col), null));
                break;
            }
            else{
                break;
            }
        }
        for (int j = col - 1; j >= 1; j--) {
            ChessPiece space = board.getPiece(new ChessPosition(row, j));
            if (space == null){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row, j), null));
            }
            else if(!sameTeam(piece, space)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row, j), null));
                break;
            }
            else{
                break;
            }
        }
        for (int c = col + 1; c <= 8; c++) {
            ChessPiece space = board.getPiece(new ChessPosition(row, c));
            if (space == null){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row, c), null));
            }
            else if(!sameTeam(piece, space)){
                valMoves.add(new ChessMove(new ChessPosition(row, col), new ChessPosition(row, c), null));
                break;
            }
            else{
                break;
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
        if (piece.getPieceType() == PieceType.BISHOP){
            return getBishopMoves(board, piece, valMoves, row, col);
        }
        if (piece.getPieceType() == PieceType.KING){
            return getKingMoves(board, myPosition, piece, valMoves, row, col);
        }
        if (piece.getPieceType() == PieceType.KNIGHT){
            return getKnightMoves(board, myPosition, piece, valMoves, row, col);
        }
        if (piece.getPieceType() == PieceType.PAWN){
            return getPawnMoves(board, piece, valMoves, row, col);
        }
        if (piece.getPieceType() == PieceType.QUEEN){
            return getQueenMoves(board,piece, valMoves, row, col);
        }
        if (piece.getPieceType() == PieceType.ROOK){
            return getRookMoves(board,piece, valMoves, row, col);
        }
        return null;
    }
}