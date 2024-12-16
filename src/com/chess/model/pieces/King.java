package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.moves.noncapturing.KingSideCastleMove;
import com.chess.model.moves.noncapturing.QueenSideCastleMove;
import com.chess.model.tiles.Tile;
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
	public Collection<Move> calculateMoves(final List<Tile> boardTiles,final Collection<Move> checkingMoves1, final Collection<Move> oppositePlayerMoves) {

		final List<Move> kingPotentialLegalMoves = new ArrayList<>();
		final Collection<Move> checkingMoves = (checkingMoves1 != null) ? ImmutableList.copyOf(checkingMoves1) : new ArrayList<>();
		
		kingPotentialLegalMoves.addAll(CalculateMoveUtils.calculate(boardTiles, this, CANDIDATE_MOVE_OFFSETS, checkingMoves, oppositePlayerMoves));

		if(checkingMoves.isEmpty()){
			addPotentialCastlingMoves(boardTiles, kingPotentialLegalMoves, oppositePlayerMoves);
		}
		
		return ImmutableList.copyOf(kingPotentialLegalMoves);
    }

	private void addPotentialCastlingMoves(final List<Tile> boardTiles, final List<Move> kingPotentialLegalMoves, final Collection<Move> oppositePlayerMoves) {
		if (this.isFirstMove()) {
			// Add kingside castle if rook is present
			final Tile kingSideRookTile = boardTiles.get(this.pieceCoordinate + 3);
			if (kingSideRookTile.isTileOccupied() && kingSideRookTile.getPiece() instanceof Rook) {
				Rook kingSideRook = (Rook) kingSideRookTile.getPiece();
				if (kingSideRook.isFirstMove()) {
					if (!boardTiles.get(this.pieceCoordinate + 1).isTileOccupied() && 
						!boardTiles.get(this.pieceCoordinate + 2).isTileOccupied()) {
						
						if( ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate)
						|| ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate + 1)
						|| ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate + 2)){
							return;
						}

						// Check if castling path is under attack
						int[] castlingPath = {this.pieceCoordinate, this.pieceCoordinate + 1, this.pieceCoordinate + 2};
						boolean isCastlingPathSafe = oppositePlayerMoves.stream()
							.noneMatch(move -> {
								if ((move.getTargetCoordinate() == this.pieceCoordinate
									|| move.getTargetCoordinate() == this.pieceCoordinate + 1 
									|| move.getTargetCoordinate() == this.pieceCoordinate + 2)) {
									return true;
								}
								int targetSquare = move.getTargetCoordinate();
								for (int pathSquare : castlingPath) {
									if (targetSquare == pathSquare) return true;
								}
								return false;
							});

						

						if (isCastlingPathSafe) {
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
			}

			// Add queenside castle if rook is present
			final Tile queenSideRookTile = boardTiles.get(this.pieceCoordinate - 4);
			if (queenSideRookTile.isTileOccupied() && queenSideRookTile.getPiece() instanceof Rook) {
				Rook queenSideRook = (Rook) queenSideRookTile.getPiece();
				if (queenSideRook.isFirstMove()) {
					if (!boardTiles.get(this.pieceCoordinate - 1).isTileOccupied() &&
						!boardTiles.get(this.pieceCoordinate - 2).isTileOccupied() &&
						!boardTiles.get(this.pieceCoordinate - 3).isTileOccupied()) {

						// Check if castling path is under attack
						int[] castlingPath = {this.pieceCoordinate, this.pieceCoordinate - 1, this.pieceCoordinate - 2};
						if( ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate)
						|| ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate - 1)
						|| ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate - 2)
						|| ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate - 3)){
							return;
						}
						boolean isCastlingPathSafe = oppositePlayerMoves.stream()
							.noneMatch(move -> {
								if ((move.getTargetCoordinate() == this.pieceCoordinate
									|| move.getTargetCoordinate() == this.pieceCoordinate - 1 
									|| move.getTargetCoordinate() == this.pieceCoordinate - 2
									|| move.getTargetCoordinate() == this.pieceCoordinate - 3)) {
									return true;
								}
								int targetSquare = move.getTargetCoordinate();
								for (int pathSquare : castlingPath) {
									if (targetSquare == pathSquare) return true;
								}
								return false;
							});

						if (isCastlingPathSafe) {
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
	}
	
    @Override
    public Piece movePiece(int destinationCoordinate) {
        return new King(destinationCoordinate, this.getPieceAlliance(),false);
    }
}
