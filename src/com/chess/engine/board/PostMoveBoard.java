package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.Bishop;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Knight;
import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Queen;
import com.chess.engine.pieces.Rook;
import com.chess.engine.player.Player;

public class PostMoveBoard {
    
    private final Board board;
    private final Move move;
    //add here moveHistory class to keep track of all moves made, and correctly implement en passant, 3 repetion rule etc
	// make moveHistory with singletton pattern
    private final Player currentPlayer;
	private final Player opponentPlayer;
	private final boolean isCurrentPlayerInCheck;

    public PostMoveBoard(final Board board,final Move move) { 
        this.board = board;
        this.move = move;
        this.currentPlayer = board.getCurrentPlayer();
        this.opponentPlayer = board.getOpponentPlayer();
    	this.isCurrentPlayerInCheck = currentPlayer.isInCheck();
    }
    
    public Board makeMove(Move move) {
		// Create a new board builder
        Board.Builder builder = new Board.Builder();
	        
        // Iterate over all current pieces on the board
        for (Piece piece : this.board.getCurrentPlayer().getPieces()) {
            // If the piece is not the moved piece, place it on the new board
            if (!move.getMovedPiece().equals(piece)) {
                builder.setPiece(piece);
            }
        }
        
        // Iterate over all opponent pieces on the board
        for (Piece piece : this.board.getOpponentPlayer().getPieces()) {
            // If the piece is not captured, place it on the new board
            if (move.getTargetCoordinate() != piece.getPieceCoordinate()) {
                builder.setPiece(piece);
            }
        }
        
        // Create the moved piece on the new board
        Piece movedPiece = createMovedPiece(move);
        builder.setPiece(movedPiece);
	        
        // Set the next player's alliance
        builder.setCurrentPlayerAlliance(this.opponentPlayer.getAlliance());
       
        // Build and return the new board
        return builder.build();
    }
    
    private Piece createMovedPiece(Move move) {
        Piece movedPiece = move.getMovedPiece();
        int destinationCoordinate = move.getTargetCoordinate();
        Alliance pieceAlliance = movedPiece.getPieceAlliance();

        if (movedPiece instanceof Pawn) {
            return new Pawn(destinationCoordinate, pieceAlliance);
        } else if (movedPiece instanceof Rook) {
            return new Rook(destinationCoordinate, pieceAlliance);
        } else if (movedPiece instanceof Knight) {
            return new Knight(destinationCoordinate, pieceAlliance);
        } else if (movedPiece instanceof Bishop) {
            return new Bishop(destinationCoordinate, pieceAlliance);
        } else if (movedPiece instanceof Queen) {
            return new Queen(destinationCoordinate, pieceAlliance);
        } else if (movedPiece instanceof King) {
            return new King(destinationCoordinate, pieceAlliance);
        }
        throw new RuntimeException("Unknown piece type");
    }
}