package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;

public class MoveMaker {
    
    private final Board board;
    private final Move move;
    private final Alliance nextMovePlayer;
    
    public MoveMaker(Board board, Move move, Alliance nextMovePlayer) { 
        this.board = board;
        this.move = move;
        this.nextMovePlayer = nextMovePlayer;
    }
    
    public Board executeMove() {
        // Create a new board builder
        Board.Builder builder = new Board.Builder();
        
        // Iterate over all current pieces on the board
        for (Piece piece : this.board.getAllPieces()) {
            // If the piece is not the moved piece, place it on the new board
            if (!this.move.getMovedPiece().equals(piece)) {
                builder.setPiece(piece);
            }
        }
        
        // Move the moved piece to the new position
        builder.setPiece(this.move.getMovedPiece()); // MUST DO -->Piece Position: The builder.setPiece(this.move.getMovedPiece()) should correctly place the moved piece at its new position. Ensure that the Move class correctly updates the piece's position.
        //can get piece type and then create a new piece with alliance and destination of move.
        // Set the next player's alliance
        builder.setMovePlayer(this.nextMovePlayer); //Correctness: Ensure that the Move class correctly updates the piece's position and that the Builder class correctly handles setting pieces on specific coordinates.
        
        // Build and return the new board
        return builder.build();
    }
}