package com.chess.model.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.model.Alliance;
import com.chess.model.board.BoardUtils;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.moves.noncapturing.KingSideCastleMove;
import com.chess.model.moves.noncapturing.NonCapturingMove;
import com.chess.model.moves.noncapturing.PawnJumpMove;
import com.chess.model.moves.noncapturing.PawnMove;
import com.chess.model.moves.noncapturing.PawnPromotionMove;
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
	public Collection<Move> calculateMoves(final List<Tile> boardTiles,final Collection<Move> checkingMoves, final Collection<Move> oppositePlayerMoves) {

		final List<Move> kingPotentialLegalMoves = new ArrayList<>();
		final Collection<Move> checkingMovesToUse = (checkingMoves != null) ? ImmutableList.copyOf(checkingMoves) : new ArrayList<>();
		final Collection<Move> oppositePlayerMovesToUse = (oppositePlayerMoves != null) ? ImmutableList.copyOf(oppositePlayerMoves) : new ArrayList<>();
		
		// Add normal king moves
		addPotentialNormalMoves(boardTiles, kingPotentialLegalMoves, checkingMovesToUse, oppositePlayerMovesToUse);
		
		// Add potential castling moves without validation
		if(checkingMovesToUse.isEmpty()){
			addPotentialCastlingMoves(boardTiles, kingPotentialLegalMoves, oppositePlayerMovesToUse);
		}
		
		return ImmutableList.copyOf(kingPotentialLegalMoves);
    }

	private void addPotentialNormalMoves(final List<Tile> boardTiles, final List<Move> kingPotentialLegalMoves, final Collection<Move> checkingMovesToUse, final Collection<Move> oppositePlayerMovesToUse) {
		// Get attack paths from checking pieces
		List<Integer> allCheckingPiecesAttackPaths = new ArrayList<>();
		List<Integer> checkingPiecesCoordinates = new ArrayList<>();//will be used when no check to find the pieces that may pin this.piece

		for (Move checkingMove : checkingMovesToUse) {
			checkingPiecesCoordinates.add(checkingMove.getPieceToMove().getPieceCoordinate());
			Piece checkingPiece = checkingMove.getPieceToMove();
			List<Integer> checkingPieceAttackPath = CalculateMoveUtils1.calculateAttackPath(
				checkingPiece,
				this.pieceCoordinate,  // current king position
				boardTiles
			);//add all the coordinates in the attack path to the list

			int throughCoordinate = CalculateMoveUtils1.getNextCoordinateInDirection(
				checkingPiece.getPieceCoordinate(),
				this.pieceCoordinate
			);
			if (BoardUtils.isValidTileCoordinate(throughCoordinate)) {
				checkingPieceAttackPath.add(throughCoordinate); // add the next coordinate that the attacking piece would target if king was not in the way of the attacking piece
			}
			allCheckingPiecesAttackPaths.addAll(checkingPieceAttackPath);
		}

		outerloop: for (final int candidateOffset : CANDIDATE_MOVE_OFFSETS) {
			int candidateDestinationCoordinate = this.pieceCoordinate + candidateOffset;
			if (BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)) {
				final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
				int rankDifference = Math.abs(BoardUtils.getCoordinateRankDifference(candidateDestinationCoordinate,this.pieceCoordinate));
				int fileDifference = Math.abs(BoardUtils.getCoordinateFileDifference(candidateDestinationCoordinate,this.pieceCoordinate));
				if (rankDifference <= 1 && fileDifference <= 1) {
					// Check if destination is under attack
					boolean isUnderAttack = oppositePlayerMovesToUse.stream()
						.anyMatch(move -> {
							// If it's a pawn's forward move, ignore it (pawns can't capture forward)
							if (move instanceof PawnMove || move instanceof PawnPromotionMove || move instanceof PawnJumpMove) {
								return false;
							}
							// For all other moves, check if they target the square
							return move.getTargetCoordinate() == candidateDestinationCoordinate;
						});

					// Check if destination is in any checking piece's attack path
					boolean isInAttackPath = false;
					if(!checkingPiecesCoordinates.contains(candidateDestinationCoordinate)){
						 isInAttackPath = allCheckingPiecesAttackPaths.contains(candidateDestinationCoordinate);
					}
	
					if (!isUnderAttack && !isInAttackPath) {

						if(!candidateDestinationTile.isTileOccupied()) {
							// Check opponent pawn "seeing" candidate destination	
							int advanceDirection = this.pieceAlliance.getMovingDirection();  
							int[] pawnOffsets = { (advanceDirection) * 7, (advanceDirection) * 9 };
							for (int offset : pawnOffsets) {
								int total_offset = offset;
								int protectorCoordinate = candidateDestinationCoordinate + total_offset;
								if (BoardUtils.isValidTileCoordinate(protectorCoordinate)) {
									Tile protectorTile = boardTiles.get(protectorCoordinate);
									final Alliance allianceOfProtectorTile = protectorTile.getTileAlliance();
									final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance(); 
									if (allianceOfProtectorTile == allianceOfCandidateDestinationTile) {
										if (protectorTile.isTileOccupied()) {
											Piece protector = protectorTile.getPiece();
											if (protector.getPieceAlliance() != this.pieceAlliance && 
												(protector instanceof Pawn)) {
													System.out.println("aa nai e " + candidateDestinationCoordinate);
													// System.out.println("piece not able to capture coordinate" + )
												continue outerloop;
											}
										}
									}
								}
							}
							kingPotentialLegalMoves.add(new NonCapturingMove(boardTiles,this.pieceCoordinate, candidateDestinationCoordinate, this));
						} else {
							final Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
							final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
							if(this.pieceAlliance != allianceOfPieceOnCandidateDestinationTile) {
								// Check if piece is protected by opponent pieces
								boolean isPieceProtected = false;

								// Check knight protectors
								if (!isPieceProtected) {
									int[] knightOffsets = { -17, -15, -10, -6, 6, 10, 15, 17 };
									for (int offset : knightOffsets) {
										int protectorCoordinate = candidateDestinationCoordinate + offset;
										if (BoardUtils.isValidTileCoordinate(protectorCoordinate)) {
											Tile protectorTile = boardTiles.get(protectorCoordinate);
											final Alliance allianceOfProtectorTile = protectorTile.getTileAlliance();
											final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance(); 
											if (allianceOfProtectorTile != allianceOfCandidateDestinationTile) {
												if (protectorTile.isTileOccupied()) {
													Piece protector = protectorTile.getPiece();
													if (protector.getPieceAlliance() != this.pieceAlliance && 
														protector instanceof Knight) {
														isPieceProtected = true;
														if(!checkingPiecesCoordinates.isEmpty()){
															System.out.println("knight");
															System.out.println("isprotectedby" + protectorTile.getPiece().getPieceCoordinate() );
														}
														break;
													}
												}
											}
										}
									}
								}

								// Check diagonal protectors (Bishop, Queen)
								if (!isPieceProtected) {
									int[] diagonalOffsets = { -9, -7, 7, 9 };
									diagonalLoop: for (int offset : diagonalOffsets) {
										for(int squaresMoved=1; squaresMoved <= 7; squaresMoved++ ) {
											int total_offset = offset * squaresMoved;
											int protectorCoordinate = candidateDestinationCoordinate + total_offset;
											if (BoardUtils.isValidTileCoordinate(protectorCoordinate)) {
												Tile protectorTile = boardTiles.get(protectorCoordinate);
												final Alliance allianceOfProtectorTile = protectorTile.getTileAlliance();
												final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance(); 
												if (allianceOfProtectorTile == allianceOfCandidateDestinationTile) {
													if (protectorTile.isTileOccupied()) {
														Piece protector = protectorTile.getPiece();
														if(protector.getPieceAlliance() == this.pieceAlliance){
															break;
														}
														if (protector.getPieceAlliance() != this.pieceAlliance && 
															(protector instanceof Bishop || protector instanceof Queen)) {
															isPieceProtected = true;
															if(!checkingPiecesCoordinates.isEmpty()){
																System.out.println("Bishop,Queen");
																System.out.println("isprotectedby" + protectorTile.getPiece().getPieceCoordinate() );
															}
															break diagonalLoop;
														}
													}
												}
											}
										}
									}
								}
								
								// Check straight protectors (Rook, Queen)
								if (!isPieceProtected) {
									int[] straightOffsets = { -8, -1, 1, 8 };
									straightLoop: for (int offset : straightOffsets) {
										for(int squaresMoved=1; squaresMoved <= 7; squaresMoved++ ) {
											int total_offset = offset * squaresMoved;
											int protectorCoordinate = candidateDestinationCoordinate + total_offset;
											if (BoardUtils.isValidTileCoordinate(protectorCoordinate)) {
												Tile protectorTile = boardTiles.get(protectorCoordinate);
												if (protectorTile.isTileOccupied()) {
													Piece protector = protectorTile.getPiece();
													if(protector.getPieceAlliance() == this.pieceAlliance){
														break;
													}
													if (protector.getPieceAlliance() != this.pieceAlliance && 
														(protector instanceof Rook || protector instanceof Queen)) {
														isPieceProtected = true;
														if(!checkingPiecesCoordinates.isEmpty()){
															System.out.println("Rook,Queen");
															System.out.println("isprotectedby" + protectorTile.getPiece().getPieceCoordinate() );
														}
														break straightLoop;
													}
												}
											}
										}
									}
								}

								// Check opponent king protector
								if (!isPieceProtected) {
									int[] offsets = { -9, -8, -7, -1, 1, 7, 8, 9 };
									for (int offset : offsets) {
										int total_offset = offset;
										int protectorCoordinate = candidateDestinationCoordinate + total_offset;
										if (BoardUtils.isValidTileCoordinate(protectorCoordinate)) {
											Tile protectorTile = boardTiles.get(protectorCoordinate);
											if (protectorTile.isTileOccupied()) {
												Piece protector = protectorTile.getPiece();
												if (protector.getPieceAlliance() != this.pieceAlliance && 
													(protector instanceof King)) {
													isPieceProtected = true;
													if(!checkingPiecesCoordinates.isEmpty()){
														System.out.println("Pawn");
														System.out.println("isprotectedby" + protectorTile.getPiece().getPieceCoordinate() );
													}
												}
												break;
											}
										}
									}
								}

								// Check opponent pawn "seeing" candidate destination	
								if (!isPieceProtected) {
									int advanceDirection = this.pieceAlliance.getMovingDirection();  
									int[] pawnOffsets = { (advanceDirection) * 7, (advanceDirection) * 9 };
									for (int offset : pawnOffsets) {
										int total_offset = offset;
										int protectorCoordinate = candidateDestinationCoordinate + total_offset;
										if (BoardUtils.isValidTileCoordinate(protectorCoordinate)) {
											Tile protectorTile = boardTiles.get(protectorCoordinate);
											final Alliance allianceOfProtectorTile = protectorTile.getTileAlliance();
											final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance(); 
											if (allianceOfProtectorTile == allianceOfCandidateDestinationTile) {
												if (protectorTile.isTileOccupied()) {
													Piece protector = protectorTile.getPiece();
													if (protector.getPieceAlliance() != this.pieceAlliance && 
														(protector instanceof Pawn)) {
															isPieceProtected = true;
															if(!checkingPiecesCoordinates.isEmpty()){
																System.out.println("Pawn");
																System.out.println("isprotectedby" + protectorTile.getPiece().getPieceCoordinate() );
															}
														}
												}
											}
										}
									}
								}

								// // Check pawn protectors
								// if (!isPieceProtected) {
								// 	int advanceDirection1 = this.pieceAlliance.getMovingDirection();  
								// 	int[] offsets = { (-advanceDirection1) * 7, (-advanceDirection1) * 9 };
								// 	for (int offset : offsets) {
								// 		int total_offset = offset;
								// 		int protectorCoordinate = candidateDestinationCoordinate + total_offset;
								// 		if (BoardUtils.isValidTileCoordinate(protectorCoordinate)) {
								// 			Tile protectorTile = boardTiles.get(protectorCoordinate);
								// 			if (protectorTile.isTileOccupied()) {
								// 				Piece protector = protectorTile.getPiece();
								// 				if (protector.getPieceAlliance() != this.pieceAlliance && 
								// 					(protector instanceof Rook || protector instanceof Queen)) {
								// 					return;
								// 				}
								// 			}
								// 		}
								// 	}
								// }

								if (!isPieceProtected) {
									kingPotentialLegalMoves.add(new CapturingMove(boardTiles,this.pieceCoordinate, 
										candidateDestinationCoordinate, this, pieceOnCandidateDestinationTile));
								}
							}
						}
					}
				}
			}
		}
	}

	private void addPotentialCastlingMoves(final List<Tile> boardTiles, final List<Move> kingPotentialLegalMoves, final Collection<Move> oppositePlayerMovesToUse) {
		if (this.isFirstMove()) {
			// Add kingside castle if rook is present
			final Tile kingSideRookTile = boardTiles.get(this.pieceCoordinate + 3);
			if (kingSideRookTile.isTileOccupied() && kingSideRookTile.getPiece() instanceof Rook) {
				Rook kingSideRook = (Rook) kingSideRookTile.getPiece();
				if (kingSideRook.isFirstMove()) {
					if (!boardTiles.get(this.pieceCoordinate + 1).isTileOccupied() && 
						!boardTiles.get(this.pieceCoordinate + 2).isTileOccupied()) {
						
						// Check if castling path is under attack
						int[] castlingPath = {this.pieceCoordinate, this.pieceCoordinate + 1, this.pieceCoordinate + 2};
						boolean isCastlingPathSafe = oppositePlayerMovesToUse.stream()
							.noneMatch(move -> {
								if (move instanceof PawnPromotionMove && 
									(move.getTargetCoordinate() == this.pieceCoordinate
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
						boolean isCastlingPathSafe = oppositePlayerMovesToUse.stream()
							.noneMatch(move -> {
								if (move instanceof PawnPromotionMove &&
									(move.getTargetCoordinate() == this.pieceCoordinate
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
