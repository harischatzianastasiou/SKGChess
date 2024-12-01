package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.model.board.BoardUtils;
import com.chess.model.tiles.Tile;
import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.moves.noncapturing.KingSideCastleMove;
import com.chess.model.moves.noncapturing.NonCapturingMove;
import com.chess.model.moves.noncapturing.QueenSideCastleMove;
import com.google.common.collect.ImmutableList;

public class King extends Piece  {
	
	private static final int[] CANDIDATE_MOVE_OFFSETS = { -9, -8, -7, -1, 1, 7, 8, 9 };
	
	public King(final int pieceCoordinate, final Alliance pieceAlliance) {
        super(PieceSymbol.KING, pieceCoordinate, pieceAlliance, true);
	}
	
	public King(final int pieceCoordinate, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceSymbol.KING, pieceCoordinate, pieceAlliance, isFirstMove);
	}
	
    @Override
    public String toString() {
        return this.pieceSymbol.toString();
    }
    
    @Override
	public Collection<Move> calculatePotentialLegalMoves(final List<Tile> boardTiles) {
		final List<Move> kingPotentialLegalMoves = new ArrayList<>();
		
		// Add normal king moves
		addPotentialNormalMoves(boardTiles, kingPotentialLegalMoves);
		
		// Add potential castling moves without validation
		addPotentialCastlingMoves(boardTiles, kingPotentialLegalMoves);
		
		return ImmutableList.copyOf(kingPotentialLegalMoves);
    }

	private void addPotentialNormalMoves(final List<Tile> boardTiles, final List<Move> kingPotentialLegalMoves) {
		for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			int candidateDestinationCoordinate = this.pieceCoordinate + candidateOffset;
			if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
				final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
				int rankDifference = Math.abs(BoardUtils.getCoordinateRankDifference(candidateDestinationCoordinate,this.pieceCoordinate));
				int fileDifference = Math.abs(BoardUtils.getCoordinateFileDifference(candidateDestinationCoordinate,this.pieceCoordinate));
				if (rankDifference <= 1 && fileDifference <= 1) {
					if(!candidateDestinationTile.isTileOccupied()) {
						kingPotentialLegalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));
					} else {
						final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
						final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
						if(this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile) {
							kingPotentialLegalMoves.add(new CapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
						}
					}
				}
			}
		}
	}

	private void addPotentialCastlingMoves(final List<Tile> boardTiles, final List<Move> kingPotentialLegalMoves) {
		if (this.isFirstMove()) {
			// Add kingside castle if rook is present
			final Tile kingSideRookTile = boardTiles.get(this.pieceCoordinate + 3);
			if (kingSideRookTile.isTileOccupied() && kingSideRookTile.getPiece() instanceof Rook) {
				Rook kingSideRook = (Rook) kingSideRookTile.getPiece();
				if (kingSideRook.isFirstMove()) {
					if (!boardTiles.get(this.pieceCoordinate + 1).isTileOccupied() && 
						!boardTiles.get(this.pieceCoordinate + 2).isTileOccupied()) {
							kingPotentialLegalMoves.add(new KingSideCastleMove(boardTiles,
															 this.pieceCoordinate,
															 this.pieceCoordinate + 2,
															 this,
															 kingSideRook.getPieceCoordinate(),
															 this.pieceCoordinate + 1,
															 kingSideRook));
					}
				}
			}

			// Add queenside castle if rook is present
			final Tile queenSideRookTile = boardTiles.get(this.pieceCoordinate - 4);
			if (queenSideRookTile.isTileOccupied() && queenSideRookTile.getPiece() instanceof Rook) {
				Rook queenSideRook = (Rook) queenSideRookTile.getPiece();
				if (queenSideRook.isFirstMove()) {
					if (!boardTiles.get(this.pieceCoordinate - 1).isTileOccupied() &&
						!boardTiles.get(this.pieceCoordinate - 2).isTileOccupied() &&
						!boardTiles.get(this.pieceCoordinate - 3).isTileOccupied()) {
							kingPotentialLegalMoves.add(new QueenSideCastleMove(boardTiles,
															  this.pieceCoordinate,
															  this.pieceCoordinate - 2,
															  this,
															  queenSideRook.getPieceCoordinate(),
															  this.pieceCoordinate - 1,
															  queenSideRook));
					}
				}
			}
		}
	}
	
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new King(destinationCoordinate, this.getPieceAlliance(),false);
    }
}
