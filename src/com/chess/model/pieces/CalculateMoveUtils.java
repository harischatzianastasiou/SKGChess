package com.chess.model.pieces;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.moves.capturing.PawnEnPassantAttack;
import com.chess.model.moves.capturing.PawnPromotionCapturingMove;
import com.chess.model.moves.noncapturing.NonCapturingMove;
import com.chess.model.moves.noncapturing.PawnJumpMove;
import com.chess.model.moves.noncapturing.PawnMove;
import com.chess.model.moves.noncapturing.PawnPromotionMove;
import com.chess.model.pieces.Piece.PieceSymbol;
import com.chess.model.player.CurrentPlayer;
import com.chess.model.player.Player;
import com.chess.model.tiles.Tile;
import com.chess.util.GameHistory;
import com.google.common.collect.ImmutableList;

public final class CalculateMoveUtils {

    public static final int NUM_TILES = 64;
	public static final int NUM_TILES_PER_ROW = 8;
    public static final Collection<Move> NO_LEGAL_MOVES = ImmutableList.copyOf(new ArrayList<>());

    public static Collection<Move> calculate(List<Tile> boardTiles,final Piece piece,final int[] moveOffsets ,final Player opponentPlayer){
        final Collection<Move> moves = new ArrayList<>();
        final Collection<Move> opponentMoves =  (opponentPlayer == null) ? null : opponentPlayer.getMoves();
        final Collection<Move> checkingMoves= (opponentPlayer == null) ? null : CurrentPlayer.getOpponentCheckingMoves(boardTiles, opponentPlayer.getOppositeAlliance(), opponentPlayer);
        List<Integer> checkingPieceAttackPath = new ArrayList<>();// will be used in single check
        List<Integer> pinningPieceAttackPath = new ArrayList<>();// will be used in pinning
        List<Integer> checkingPiecesThroughKingPath = new ArrayList<>();
        List<Integer> checkingPiecesCoordinates = new ArrayList<>();

        if(opponentPlayer!= null){
            if(!(piece instanceof King)){
                if(!checkingMoves.isEmpty()){
                    if(checkingMoves.size() > 1){ //in double check only the king can move.
                        System.out.println("1");
                        return NO_LEGAL_MOVES;
                    }
                    final Move checkingMove = checkingMoves.iterator().next();
                    final Piece checkingPiece = checkingMove.getPieceToMove();
                    final int kingCoordinate = checkingMove.getTargetCoordinate();
                    checkingPieceAttackPath.addAll(calculateAttackPath(checkingPiece, kingCoordinate, boardTiles));
                }
                pinningPieceAttackPath = calculateAttackPathOfPinningPiece(piece, boardTiles, opponentMoves);
                if(pinningPieceAttackPath == null){
                    System.out.println("sourcecoordinate" + piece.getPieceCoordinate());
                    return NO_LEGAL_MOVES;
                } 
            }

            if(piece instanceof King){
                for (Move checkingMove : checkingMoves) {
                    checkingPiecesCoordinates.add(checkingMove.getPieceToMove().getPieceCoordinate());
                    Piece checkingPiece = checkingMove.getPieceToMove();
                    int kingCoordinate = checkingMove.getTargetCoordinate();
                    int throughCoordinate = CalculateMoveUtils.getNextCoordinateInDirection(checkingPiece.getPieceCoordinate(), kingCoordinate);
                    if (isCoordinateInBounds(throughCoordinate)) {
                        checkingPiecesThroughKingPath.add(throughCoordinate); // add the next coordinate that the attacking piece would target if king was not in the way of the attacking piece
                    }
                }
            }
        }
        
        for (final int candidateOffset : moveOffsets) {
            for(int squaresMoved=1; squaresMoved <= getMaxSquaresMoved(piece); squaresMoved++ ) {
                int total_offset = candidateOffset * squaresMoved;
                final int candidateDestinationCoordinate;
                if(piece instanceof Pawn){
                    Pawn pawn = (Pawn) piece;
                    candidateDestinationCoordinate = piece.getPieceCoordinate() +(pawn.getAdvanceDirection() * total_offset);
                }else{
                    candidateDestinationCoordinate = piece.getPieceCoordinate() + total_offset;
                }

                if(!(isCoordinateInBounds(candidateDestinationCoordinate))){
                    break;
                }

                final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);

                if(opponentMoves != null){
                    if(!(piece instanceof King)){
                        if (!checkingMoves.isEmpty() && !checkingPieceAttackPath.contains(candidateDestinationCoordinate)) {
                            break;
                        }
                        if(!pinningPieceAttackPath.isEmpty() && !pinningPieceAttackPath.contains(candidateDestinationCoordinate)){
                            break;
                        }
                    }
                    if(piece instanceof King){
                        boolean isCandidateCoordinateUnderAttack = opponentMoves.stream()
						.anyMatch(move -> {
							// If it's a pawn's forward move, ignore it (pawns can't attack(capture) forward)
							if (move instanceof PawnMove || move instanceof PawnPromotionMove || move instanceof PawnJumpMove) {
								return false;
							}
							// For all other moves, check if they target the square
							return move.getTargetCoordinate() == candidateDestinationCoordinate;
						});

                        boolean isCandidateCoordinateInCheckingPieceThroughPath = checkingPiecesThroughKingPath.contains(candidateDestinationCoordinate);
			
                        boolean isCandidateCoordinateProtectedByOpponentPiece = ProtectedCoordinatesTracker.getProtectedCoordinates().contains(candidateDestinationCoordinate);

                        if (isCandidateCoordinateUnderAttack || isCandidateCoordinateInCheckingPieceThroughPath || isCandidateCoordinateProtectedByOpponentPiece ) {
                            break;
                        }
                    }
                }
                
                if(!(validateCandidateRankAndFile(piece, candidateDestinationCoordinate,candidateOffset))
                   || !(isValidAllianceOfTile(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, candidateOffset, piece))){
                    break;
                }

                if(isCandidateTileEmpty(candidateDestinationTile)){
                   moves.addAll(addNonCapturingMoves(piece, boardTiles, candidateDestinationCoordinate, candidateOffset, opponentPlayer));
                }else{
                    if(isCandidateTileOccupiedByOpponent(piece,candidateDestinationTile))
                        moves.addAll(addCapturingMoves(piece, boardTiles,  candidateDestinationCoordinate, candidateOffset));
                    else
                        if(opponentPlayer == null)
                            ProtectedCoordinatesTracker.addProtectedCoordinate(candidateDestinationCoordinate); // Store the protected piece
                    break; //for sliding pieces if there is a piece in the direction that sliding piece can move, stop further checking in this direction.
                }
            }
        }
        return ImmutableList.copyOf(moves);
    }

    public static Collection<Move> addNonCapturingMoves(Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset, Player opponentPlayer){
        final List<Move> moves = new ArrayList<>();

        if(!(piece instanceof Pawn)){
            moves.add(new NonCapturingMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
        }

        if(piece instanceof Pawn){
            Pawn pawn = (Pawn) piece;
            switch (Math.abs(candidateOffset)) {
                case 8:
                    if(pawn.getCurrentRank() != pawn.getPromotionRank()){
                        moves.add(new PawnMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
                    } else {
                        moves.add(new PawnPromotionMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
                    }
                    break;
                case 16:
                    if(pawn.getCurrentRank() == pawn.getInitialRank()){
                        int coordinateBeforeCandidate = piece.getPieceCoordinate() + ( 8 * pawn.getAdvanceDirection());
                        if(isCandidateTileEmpty(boardTiles.get(coordinateBeforeCandidate))){
                            moves.add(new PawnJumpMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece));
                        }
                    }
                    break;
                case 7:
                case 9:
                    if(opponentPlayer == null)
                        ProtectedCoordinatesTracker.addProtectedCoordinate(candidateDestinationCoordinate);
                    if(pawn.getCurrentRank() == pawn.getEnPassantRank()){
                        Move lastMove = GameHistory.getInstance().getLastMove();
                        if (!(lastMove instanceof PawnJumpMove) || lastMove.getPieceToMove().getPieceAlliance() == piece.getPieceAlliance() || getCoordinateFile(lastMove.getTargetCoordinate()) != getCoordinateFile(candidateDestinationCoordinate)) {
                            return ImmutableList.copyOf(moves);
                        }
                        Piece pieceToCapture = boardTiles.get(lastMove.getTargetCoordinate()).getPiece();
                        moves.add(new PawnEnPassantAttack(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece, pieceToCapture));
                    }
                    break;
            }
        }
        return ImmutableList.copyOf(moves);
    }

    public static Collection<Move> addCapturingMoves( Piece piece, List<Tile> boardTiles, int candidateDestinationCoordinate, int candidateOffset){
        final List<Move> moves = new ArrayList<>();
        Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
        Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
        
        if(!(piece instanceof Pawn)){
            moves.add(new CapturingMove(boardTiles,piece.getPieceCoordinate(), candidateDestinationCoordinate, piece, pieceOnCandidateDestinationTile));
        }
        if(piece instanceof Pawn && (Math.abs(candidateOffset) == 7 || Math.abs(candidateOffset) == 9)){
            Pawn pawn = (Pawn) piece;
            if(pawn.getCurrentRank() != pawn.getPromotionRank()){
                moves.add(new CapturingMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece, pieceOnCandidateDestinationTile));
            }else
                moves.add(new PawnPromotionCapturingMove(boardTiles, piece.getPieceCoordinate(), candidateDestinationCoordinate, piece, pieceOnCandidateDestinationTile));
        }
        return ImmutableList.copyOf(moves);
    }

    public static boolean isCoordinateInBounds(final int tileCoordinate) {
        return tileCoordinate >= 0 && tileCoordinate < NUM_TILES;
    }

    public static boolean validateCandidateRankAndFile(Piece piece, int candidateDestinationCoordinate, int candidateOffset){
        int rankDifference = getCoordinateRankDifference(candidateDestinationCoordinate, piece.getPieceCoordinate());
        int fileDifference = getCoordinateFileDifference(candidateDestinationCoordinate, piece.getPieceCoordinate());
        switch(piece.getPieceSymbol()){
            case ROOK:
                return rankDifference == 0 || fileDifference == 0;
            case KING:
                return rankDifference <= 1 && fileDifference <= 1;
            case QUEEN:
                if((Math.abs(candidateOffset) == 1 || Math.abs(candidateOffset) == 8)){
                    return rankDifference == 0 || fileDifference == 0;
                }
                break;
        }
        return true;
    }
    
    public static boolean isValidAllianceOfTile(List<Tile> boardTiles, int pieceCoordinate, int candidateDestinationCoordinate, int candidateOffset, Piece piece){
        final Tile candidateDestinationTile = boardTiles.get(candidateDestinationCoordinate);
        final Alliance allianceOfCandidateDestinationTile = candidateDestinationTile.getTileAlliance();
        final Tile currentTile = boardTiles.get(pieceCoordinate);
        final Alliance allianceOfCurrentTile = currentTile.getTileAlliance();

        switch(piece.getPieceSymbol()){
            case BISHOP:
                return allianceOfCandidateDestinationTile == allianceOfCurrentTile;
            case QUEEN:
                if(Math.abs(candidateOffset) == 7 || Math.abs(candidateOffset) == 9){
                    return allianceOfCandidateDestinationTile == allianceOfCurrentTile;
                }
                break;
            case KNIGHT:
                return allianceOfCandidateDestinationTile != allianceOfCurrentTile;
            case PAWN:
                if(Math.abs(candidateOffset) == 7 || Math.abs(candidateOffset) == 9){
                    return allianceOfCandidateDestinationTile == allianceOfCurrentTile;
                }
                break;
        }
        return true;
    }

    public static boolean isCandidateTileEmpty(Tile candidateDestinationTile){
        return !candidateDestinationTile.isTileOccupied();
    }    

    public static boolean isCandidateTileOccupiedByOpponent(Piece piece, Tile candidateDestinationTile){
        Piece pieceOnCandidateDestinationTile = candidateDestinationTile.getPiece();
        final Alliance allianceOfPieceOnCandidateDestinationTile = pieceOnCandidateDestinationTile.getPieceAlliance();
        return piece.getPieceAlliance() != allianceOfPieceOnCandidateDestinationTile;
    }

    public static int getCoordinateRank(int tileCoordinate) {
        return (Math.abs(tileCoordinate) / NUM_TILES_PER_ROW) + 1;
    }

    public static int getCoordinateFile(int tileCoordinate) {
        return (Math.abs(tileCoordinate) % NUM_TILES_PER_ROW) + 1;
    }

    public static int getCoordinateRankDifference(int destinationCoordinate, int sourceCoordinate) {
        return Math.abs(getCoordinateRank(destinationCoordinate)) - Math.abs(getCoordinateRank(sourceCoordinate));
    }

    public static int getCoordinateFileDifference(int destinationCoordinate, int sourceCoordinate) {
        return Math.abs(getCoordinateFile(destinationCoordinate)) - Math.abs(getCoordinateFile(sourceCoordinate));
    }

    public static Alliance getCoordinateAlliance(int tileCoordinate) {
        return (getCoordinateRank(tileCoordinate) + getCoordinateFile(tileCoordinate)) % 2 == 0 ? Alliance.BLACK : Alliance.WHITE;
    }

    public static int getMaxSquaresMoved(final Piece piece){
        switch(piece.getPieceSymbol()){
            case PAWN:
            case KING:
            case KNIGHT:
                return 1;
            case BISHOP:
            case ROOK:
            case QUEEN:
                return 7;
            default:
                return 0;
        }
    }

    public static List<Integer> calculateAttackPath(final Piece checkingPiece, final int kingCoordinate, final List<Tile> boardTiles) {
        List<Integer> pathCoordinates = new ArrayList<>();
        if (checkingPiece == null || boardTiles == null) {
            return pathCoordinates;
        }    
        final int checkingPieceCoordinate = checkingPiece.getPieceCoordinate();
        switch(checkingPiece.getPieceSymbol()) {
            case BISHOP:
                return calculateDiagonalAttackPath(checkingPieceCoordinate, kingCoordinate);
                
            case ROOK:
                return calculateStraightAttackPath(checkingPieceCoordinate, kingCoordinate);
                
            case QUEEN:
                if (isOnDiagonal(checkingPieceCoordinate, kingCoordinate)) {
                    return calculateDiagonalAttackPath(checkingPieceCoordinate, kingCoordinate);
                } else {
                    return calculateStraightAttackPath(checkingPieceCoordinate, kingCoordinate);
                }
                
            case KNIGHT:
                pathCoordinates.add(checkingPieceCoordinate);
                return pathCoordinates;
                
            case PAWN:
                pathCoordinates.add(checkingPieceCoordinate);
                return pathCoordinates;
                
            default:
                return pathCoordinates;
        }
    }

    public static List<Integer> calculateDiagonalAttackPath(final int startCoordinate, final int endCoordinate) {
        List<Integer> pathCoordinates = new ArrayList<>();
        pathCoordinates.add(startCoordinate);
        
        int offset;
        if (Math.abs(endCoordinate - startCoordinate) % 7 == 0) {
            offset = 7 * Integer.signum(endCoordinate - startCoordinate);
        } else if (Math.abs(endCoordinate - startCoordinate) % 9 == 0) {
            offset = 9 * Integer.signum(endCoordinate - startCoordinate);
        } else {
            return pathCoordinates;
        }
        
        int currentCoordinate = startCoordinate + offset;
        while (currentCoordinate != endCoordinate) {
            pathCoordinates.add(currentCoordinate);
            currentCoordinate += offset;
        }
        
        return pathCoordinates;
    }

    public static List<Integer> calculateStraightAttackPath(final int startCoordinate, final int endCoordinate) {
        List<Integer> pathCoordinates = new ArrayList<>();
        pathCoordinates.add(startCoordinate);
        
        int offset;
        if (Math.abs(endCoordinate - startCoordinate) >= 8) {
            offset = 8 * Integer.signum(endCoordinate - startCoordinate);
        } else {
            offset = Integer.signum(endCoordinate - startCoordinate);
        }
        
        int currentCoordinate = startCoordinate + offset;
        while (currentCoordinate != endCoordinate) {
            pathCoordinates.add(currentCoordinate);
            currentCoordinate += offset;
        }
        
        return pathCoordinates;
    }

    public static boolean isOnDiagonal(final int startCoordinate, final int endCoordinate) {
        int diff = Math.abs(endCoordinate - startCoordinate);
        return diff % 7 == 0 || diff % 9 == 0;
    }

    public static int getNextCoordinateInDirection(final int startCoordinate, final int throughCoordinate) {
        int diff = throughCoordinate - startCoordinate;
        
        if (Math.abs(diff) % 7 == 0) {
            return throughCoordinate + (7 * Integer.signum(diff));
        } else if (Math.abs(diff) % 9 == 0) {
            return throughCoordinate + (9 * Integer.signum(diff));
        } else if (Math.abs(diff) % 8 == 0) {
            return throughCoordinate + (8 * Integer.signum(diff));
        } else {
            return throughCoordinate + Integer.signum(diff);
        }
    }

    public static List<Integer> calculateAttackPathOfPinningPiece(Piece piece, List<Tile> boardTiles, Collection<Move> oppositePlayerMoves){
        int numOfPinningPieces = 0;
        final List<Integer> pinningPieceAttackPath = new ArrayList<>();// will be used in pinning
        final int kingCoordinate = CurrentPlayer.getKingCoordinate(boardTiles, piece.getPieceAlliance());     
        List<Move> movesPinningPiece= oppositePlayerMoves.stream()
            .filter(move -> move.getTargetCoordinate() == piece.getPieceCoordinate())
            .collect(Collectors.toList());
            
        for(Move move : movesPinningPiece) {
            if(move.getPieceToMove().getPieceSymbol() == PieceSymbol.PAWN 
            || move.getPieceToMove().getPieceSymbol() == PieceSymbol.KING
            || move.getPieceToMove().getPieceSymbol() == PieceSymbol.KNIGHT){
                continue; // Pawns, Kings and Knights cannot pin other pieces
            }
            Piece pinningPiece= move.getPieceToMove();
            int throughCoordinate =  piece.getPieceCoordinate();
            // Keep looking through coordinates until we hit a piece or board edge
            while(true) {
                //must check alliance of throughTile and currentPieceTile
                throughCoordinate = getNextCoordinateInDirection( pinningPiece.getPieceCoordinate(), throughCoordinate);
            
                if (!isCoordinateInBounds(throughCoordinate)) {
                    break;  // Stop if we hit board edge
                }
                
                Tile throughTile = boardTiles.get(throughCoordinate);
                
                if (throughTile.isTileOccupied()) {
                    Piece pieceInPath = throughTile.getPiece();
                    // If it's our king, this pawn is pinned - can only capture the attacking piece
                    if (pieceInPath.getPieceSymbol() == PieceSymbol.KING && 
                        pieceInPath.getPieceAlliance() == piece.getPieceAlliance()) {
                        pinningPieceAttackPath.addAll(calculateAttackPath(pinningPiece, kingCoordinate, boardTiles));
                        numOfPinningPieces++;    
                    }
                    break;  // Stop when we hit any piece
                }
            }
        }
        if(numOfPinningPieces > 1){
            return null;
        }
        return ImmutableList.copyOf(pinningPieceAttackPath);
    }
}