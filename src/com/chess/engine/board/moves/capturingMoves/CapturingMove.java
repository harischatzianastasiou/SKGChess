package com.chess.engine.board.moves.capturingMoves;

import com.chess.engine.board.moves.Move;
import com.chess.engine.board.moves.MoveResult;
import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Board.Builder;
import com.chess.engine.pieces.Piece;
import com.chess.engine.tiles.Tile;

import java.util.List;

public class CapturingMove extends Move {
    private final Piece capturedPiece;

    public CapturingMove(final List<Tile> boardTiles, final int sourceCoordinate, final int targetCoordinate, final Piece pieceToMove, final Piece capturedPiece) {
        super(boardTiles, sourceCoordinate, targetCoordinate, pieceToMove);
        this.capturedPiece = capturedPiece;
    }
    
    @Override
    public Piece getCapturedPiece() {
        return this.capturedPiece;
    }

    public Builder createBuilderAfterCapturingMove() {
        // Create a new board builder
        Board.Builder builder = new Board.Builder();
            
        for (final Tile tile : super.getBoardTiles()) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                if(!this.getPieceToMove().equals(piece) && this.getCapturedPiece().getPieceCoordinate() != piece.getPieceCoordinate()) {
                    builder.setPiece(piece);
                }
            }   
        }

        // Create the moved piece on the new board
        Piece movedPiece = super.getPieceToMove().movePiece(super.getTargetCoordinate());
        builder.setPiece(movedPiece);
            
        // Set the next player's alliance
        builder.setCurrentPlayerAlliance(this.getPieceToMove().getPieceAlliance().isWhite() ? Alliance.BLACK : Alliance.WHITE);
        return builder;
    }

    @Override
    public MoveResult simulate() {
        return MoveResult.create(this,createBuilderAfterCapturingMove().build());
    }

    @Override
    public Board execute() {
        return this.createBuilderAfterCapturingMove().build();
    }
} 