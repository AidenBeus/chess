package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && turn == chessGame.turn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, turn);
    }

    ChessBoard board = new ChessBoard();
    TeamColor turn = TeamColor.WHITE;

    public ChessGame() {
        board.resetBoard();
        turn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    private ChessBoard copyBoard(ChessBoard original){
        ChessBoard copy = new ChessBoard();
        for(int i = 1; i <= 8; i++){
            for(int j = 1; j <= 8; j++){
                copy.addPiece(new ChessPosition(i, j), original.getPiece(new ChessPosition(i, j)));
            }
        }
        return copy;
    }

    private boolean inRange(ChessPosition pos) {
        return pos.getRow() >= 1 && pos.getRow() <= 8 &&
                pos.getColumn() >= 1 && pos.getColumn() <= 8;
    }
    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if(piece == null){
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> results = new ArrayList<>();
        for (ChessMove temp: moves){
            if (!inRange(temp.getEndPosition())) {
                continue;
            }
            ChessBoard test = copyBoard(board);
            test.addPiece(temp.getStartPosition(), null);
            test.addPiece(temp.getEndPosition(), piece);
            ChessGame testGame = new ChessGame();
            testGame.setBoard(test);
            if(!testGame.isInCheck(piece.getTeamColor())){
                results.add(temp);
            }
        }
        return results;
    }


    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (move == null){
            throw new InvalidMoveException();
        }
        Collection<ChessMove> valMoves = validMoves(move.getStartPosition());
        if(valMoves == null || !valMoves.contains(move)){
            throw new InvalidMoveException();
        }
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if(piece.getTeamColor() != getTeamTurn()){
            throw new InvalidMoveException();
        }
        board.addPiece(move.getStartPosition(), null);
        if(move.getPromotionPiece()!= null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(getTeamTurn(), move.getPromotionPiece()));
        }
        else{
            board.addPiece(move.getEndPosition(), piece);
        }
        if(getTeamTurn() == TeamColor.WHITE){
            setTeamTurn(TeamColor.BLACK);
        }
        else{
            setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        int row = 0;
        int col = 0;
        for(int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    row = i;
                    col = j;
                    break;
                }
            }
        }
        for(int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = piece.pieceMoves(board, new ChessPosition(i, j));
                    for (ChessMove temp : moves) {
                        if (Objects.equals(temp.getEndPosition(), new ChessPosition(row, col))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        for(int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                Collection<ChessMove> valMoves;
                if (piece != null && piece.getTeamColor() == teamColor){
                    valMoves = validMoves(new ChessPosition(i, j));
                    if (!valMoves.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && isInCheckmate(teamColor);
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", turn=" + turn +
                '}';
    }
}
