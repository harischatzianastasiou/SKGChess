package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.moves.noncapturing.KingSideCastleMove;
import com.chess.model.moves.noncapturing.QueenSideCastleMove;
import com.chess.model.player.CurrentPlayer;
import com.chess.model.player.Player;
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
	public Collection<Move> calculateMoves(final List<Tile> boardTiles, final Player opponentPlayer) {

		final List<Move> moves = new ArrayList<>();
		final Collection<Move> opponentMoves =  (opponentPlayer == null) ? null : opponentPlayer.getMoves();
        final Collection<Move> checkingMoves= (opponentMoves != null) ? CurrentPlayer.getOpponentCheckingMoves(boardTiles, opponentPlayer.getOppositeAlliance(), opponentPlayer) : new ArrayList<>();
        
		moves.addAll(CalculateMoveUtils.calculate(boardTiles, this, CANDIDATE_MOVE_OFFSETS, opponentPlayer));

		if(opponentPlayer != null){
			if(checkingMoves.isEmpty()){
				addPotentialCastlingMoves(boardTiles, moves, opponentMoves);
			}
		}
		
		return ImmutableList.copyOf(moves);
    }

	private void addPotentialCastlingMoves(final List<Tile> boardTiles, final List<Move> moves, final Collection<Move> opponentMoves) {
		if (this.isFirstMove()) {
			// Add kingside castle if rook is present
			final Tile kingSideRookTile = boardTiles.get(this.pieceCoordinate + 3);
			if (kingSideRookTile.isTileOccupied() && kingSideRookTile.getPiece() instanceof Rook) {
				Rook kingSideRook = (Rook) kingSideRookTile.getPiece();
				if (kingSideRook.isFirstMove()) {
					if (!boardTiles.get(this.pieceCoordinate + 1).isTileOccupied() && 
						!boardTiles.get(this.pieceCoordinate + 2).isTileOccupied()) {
						
						if( CalculateMoveUtils.ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate)
						|| CalculateMoveUtils.ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate + 1)
						|| CalculateMoveUtils.ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate + 2)){
							return;
						}

						// Check if castling path is under attack
						int[] castlingPath = {this.pieceCoordinate, this.pieceCoordinate + 1, this.pieceCoordinate + 2};
						boolean isCastlingPathSafe = opponentMoves.stream()
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
							moves.add(new KingSideCastleMove(boardTiles,
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
						if( CalculateMoveUtils.ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate)
						|| CalculateMoveUtils.ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate - 1)
						|| CalculateMoveUtils.ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate - 2)
						|| CalculateMoveUtils.ProtectedCoordinatesTracker.getProtectedCoordinates().contains(this.pieceCoordinate - 3)){
							return;
						}
						boolean isCastlingPathSafe = opponentMoves.stream()
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
							moves.add(new QueenSideCastleMove(boardTiles,
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
