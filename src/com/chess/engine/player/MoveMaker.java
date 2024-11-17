package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;

public class MoveMaker {
    
    private final Board board;
    private final Move move;
    //add here moveHistory class to keep track of all moves made, and correctly implement en passant, 3 repetion rule etc
	// make moveHistory with singletton pattern
    private final Player currentPlayer;
	private final Player opponentPlayer;
	private final King currentPlayerKing;
	private final boolean isCurrentPlayerKingInCheck;

    public MoveMaker(final Board board,final Move move) { 
        this.board = board;
        this.move = move;
        this.currentPlayer = board.getCurrentPlayer();
        this.opponentPlayer = board.getOpponentPlayer();
        this.currentPlayerKing = findKing();
    	this.isCurrentPlayerKingInCheck = isCurrentPlayerKingInCheck();
    }
    
    public Board makeMove(Move move) {
        //check if move is legal
    	if(isMoveLegal(move)) {
    		// Create a new board builder
	        Board.Builder builder = new Board.Builder();
	        
	        // Iterate over all current pieces on the board
	        for (Piece piece : this.board.getCurrentPlayer().getPieces()) {
	        	
	            // If the piece is not the moved piece, place it on the new board
	            if (move.getMovedPiece().equals(piece)) {
	                builder.setPiece(piece);
	            }
	        }
	        
	        // Move the moved piece to the new position
	        builder.setPiece(this.move.getMovedPiece()); // MUST DO -->Piece Position: The builder.setPiece(this.move.getMovedPiece()) should correctly place the moved piece at its new position. Ensure that the Move class correctly updates the piece's position.
	        //can get piece type and then create a new piece with alliance and destination of move.
	        // Set the next player's alliance
	        builder.setCurrentPlayerAlliance(this.opponentPlayer.getAlliance()); //Correctness: Ensure that the Move class correctly updates the piece's position and that the Builder class correctly handles setting pieces on specific coordinates.
	        
	        // Build and return the new board
	        return builder.build();
	    }else {
	        // Handle illegal move case
	        throw new IllegalArgumentException("Illegal move: " + move);
	        // can also return current board here
	    }
    	
    }

    public King findKing() {
		for( final Piece piece : currentPlayer.getPieces()) {
			if(piece instanceof King && piece.getPieceAlliance() == this.currentPlayer.getAlliance()) {
            return (King) piece;}
		}
	    throw new RuntimeException("Invalid board: " + this.currentPlayer.getAlliance() + " king not found!");
	}
	
    public boolean isMoveLegal(final Move move) {
        // Check if the move is in the current player's legal moves
        if (!this.currentPlayer.getMoves().contains(move)) {
            return false;
        }

        // If the moved piece is the King, check opponent's legal moves
        if (move.getMovedPiece() instanceof King) {
            for (final Move opponentMove : this.opponentPlayer.getMoves()) {
                if (opponentMove.getTargetCoordinate() == move.getTargetCoordinate()) {
                    return false; // The move is not legal if the King would be captured
                }
            }
        }

        return true;
    }
	
	public boolean isCurrentPlayerKingInCheck() { 
		for( final Move opponentMove : this.opponentPlayer.getMoves() ){
				if(opponentMove.getTargetCoordinate() == this.currentPlayerKing.getPieceCoordinate()){
					return true;
				}
			}
		return false;
	}
	
	public boolean hasCurrentPlayerKingEscapeMoves() {
	    for (final Move legalMove : this.currentPlayer.getMoves()) {
	        if (legalMove.getMovedPiece() == this.currentPlayerKing) {
	            boolean isSafeMove = true;
	            for (final Move opponentMove : this.opponentPlayer.getMoves()) {
	                if (opponentMove.getTargetCoordinate() == legalMove.getTargetCoordinate()) {
	                    isSafeMove = false;
	                    break;
	                }
	            }
	            if (isSafeMove) {
	                return true; // The King has at least one escape move
	            }
	        }
	    }
	    return false; // No escape moves for the King
	}
	
	public boolean isInCheckMate() {
	    return this.isCurrentPlayerKingInCheck && !this.hasCurrentPlayerKingEscapeMoves();
	}

	public boolean isInStaleMate() {
	    return !this.isCurrentPlayerKingInCheck && !this.hasCurrentPlayerKingEscapeMoves();
	}
	public boolean isThreefoldRepetition() {
        return false; //TODO implement threefold repetition detection
    }
	
	public boolean isFiftyMoveRule() {
        return false; //TODO implement fifty move rule detection
    }
	
	public boolean isCastled() {
		return false; //TODO implement castling detection
	}
	
	public boolean isEnPassantLegal() {
        return false; //TODO implement en passant detection
    }
}