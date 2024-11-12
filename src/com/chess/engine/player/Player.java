package com.chess.engine.player;

import java.util.Collection;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;

public abstract class Player {
	
	protected final Board board;
	protected final King playerKing;
	protected final Collection<Move> legalMoves;
	private final Collection<Move> opponentMoves;
	private final boolean isInCheck;
	
	Player(final Board board, final Collection<Move> legalMoves, final Collection<Move> opponentMoves) {
		this.board = board;
		this.playerKing = findKing();
        this.legalMoves = legalMoves;
        this.opponentMoves = opponentMoves;
        this.isInCheck = isKingInCheck();
	}
	
	public abstract Collection<Piece> getActivePieces();
	
	public abstract Alliance getAlliance();
	
	public abstract Player getOpponent();
	
	public King findKing() {
		for( final Piece piece : getActivePieces()) {
			if(piece instanceof King && piece.getPieceAlliance() == this.getAlliance()) {
            return (King) piece;}
		}
	    throw new RuntimeException("Invalid board: " + getAlliance() + " king not found!");
	}
	
	public boolean isMoveLegal(final Move move) {
        return legalMoves.contains(move);
    }
	
	public boolean isKingInCheck() { // since a new board is created with each new state, this methods reflects the king being in check after the opponent plays the move. If he misses it, then false.
		for( final Move opponentMove : this.getOpponentMoves() ){
				if(opponentMove.getTargetCoordinate() == playerKing.getPieceCoordinate()){
					return true;
				}
			}
		return false;
	}
	
	public boolean hasKingEscapeMoves() {
		for( final Move legalMove : this.legalMoves) {
            if(legalMove.getMovedPiece() == this.playerKing) {
                return true;
            }
        }
		return false;
	}
	
	public boolean isInCheckMate() {
	    return this.isInCheck && !this.hasKingEscapeMoves();
	}

	public boolean isInStaleMate() {
	    return !this.isInCheck && !this.hasKingEscapeMoves();
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
	
	public Board makeMove(final Move move) {
	    if (isMoveLegal(move)) {
	        // Create a new MoveMaker and execute the move to get the new board
	        MoveMaker moveMaker = new MoveMaker(board, move, this.getOpponent().getAlliance());
	        Board newBoard = moveMaker.executeMove();

	        // Update players with the new board state
	        Player currentPlayer = this.getAlliance().isWhite() ? new WhitePlayer(newBoard,legalMoves,opponentMoves) : new BlackPlayer(newBoard,opponentMoves,legalMoves);
	        Player opponentPlayer = currentPlayer.getOpponent();

	        // Check for game-ending conditions
	        if (currentPlayer.isInCheckMate()) {
	            System.out.println("Checkmate! " + currentPlayer.getAlliance() + " loses.");
	        } else if (currentPlayer.isInStaleMate()) {
	            System.out.println("Stalemate! The game is a draw.");
	        }

	        return newBoard;
	    } else {
	        throw new IllegalArgumentException("Illegal move: " + move);
	    }
	}

	public Collection<Move> getOpponentMoves() {
		return opponentMoves;
	}
	
	
}
