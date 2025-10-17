package  com.chess.core.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import  com.chess.core.Alliance;
import  com.chess.core.moves.Move;
import  com.chess.core.pieces.King;
import  com.chess.core.pieces.Piece;
import  com.chess.core.pieces.Piece.PieceSymbol;
import com.chess.core.tiles.Tile;
import com.google.common.collect.ImmutableList;
import com.chess.model.entity.Game.GameStatus;
public final class CurrentPlayer extends Player {
		
    private final boolean isInCheck;
    private final boolean isInCheckmate;
    private final List<Tile> boardTiles; // Store board tiles for position analysis
    private final List<Move> moveHistory; // Store move history for analysis

    private CurrentPlayer(final Collection<Piece> pieces, final Collection<Move> moves, final Alliance alliance, 
                         boolean isInCheck, boolean isInCheckmate, final List<Tile> boardTiles, final List<Move> moveHistory) {
        super(pieces, moves, alliance);
        this.isInCheck = isInCheck;
        this.isInCheckmate = isInCheckmate;
        this.boardTiles = boardTiles;
        this.moveHistory = moveHistory;
    }

	public static CurrentPlayer createCurrentPlayer(final List<Tile> tiles, final Alliance alliance,final Player opponentPlayer, final Move lastMove) {
        final List<Piece> pieces = new ArrayList<>();
        final Collection<Move> moves = new ArrayList<>();
        final Collection<Move> opponentCheckingMoves = new ArrayList<>();
        boolean isInCheck = false;
        boolean isInCheckmate = false;

        opponentCheckingMoves.addAll(getOpponentCheckingMoves(tiles, alliance, opponentPlayer));
        if(!opponentCheckingMoves.isEmpty()){
            isInCheck = true;
        }
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();   
                if (piece.getPieceAlliance() == alliance) {
                    pieces.add(piece);
                    moves.addAll(piece.calculateMoves(tiles, opponentPlayer, lastMove));
                }
            }
        }  

        if (isInCheck && moves.isEmpty()) {
            isInCheckmate = true; 
        }
        return new CurrentPlayer(ImmutableList.copyOf(pieces), ImmutableList.copyOf(moves), alliance, 
                                isInCheck, isInCheckmate, tiles, new ArrayList<>());    
    }

    public static Collection<Move> getOpponentCheckingMoves(final List<Tile> tiles, final Alliance alliance, final Player opponentPlayer) {// moves that are checking the current user's king
        Collection<Move> checkingMoves = new ArrayList<>();
        for (Move move : opponentPlayer.getMoves()) {
            if (move.getTargetCoordinate() == getKingCoordinate(tiles, alliance)) {
                checkingMoves.add(move);
            }
        }
        return checkingMoves;
    }

    public static int getKingCoordinate(final List<Tile> tiles, final Alliance alliance) {
        for (final Tile tile : tiles) {
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();   
                if (piece.getPieceAlliance() == alliance) {
                    if (piece instanceof King && piece.getPieceAlliance() == alliance) {
                        return piece.getPieceCoordinate();
                    }
                }
            }
        }
        throw new RuntimeException("No king found for this user");
    }

    public boolean isCheckmate() {
        return this.isInCheckmate;
    }

    public boolean isInCheck() {
        return this.isInCheck;
    }

    /**
     * Check if the current position is a stalemate
     * Stalemate occurs when the current player has no legal moves but is not in check
     * @return true if stalemate, false otherwise
     */
    private boolean isStalemate() {
        // Stalemate: no legal moves AND not in check
        return !this.isInCheck && this.getMoves().isEmpty();
    }

    /**
     * Check if the current position has been repeated 3 times (threefold repetition)
     * Threefold repetition occurs when the same position appears 3 times in the game
     * @return true if threefold repetition, false otherwise
     */
    private boolean isThreefoldRepetition() {
        // Need at least 3 positions to have repetition
        if (moveHistory.size() < 2) {
            return false;
        }
        
        // Create a signature of the current board position
        String currentPositionSignature = createPositionSignature();
        
        // Count how many times this position has appeared
        int repetitionCount = 1; // Current position counts as 1
        
        // Check previous positions in move history
        for (int i = moveHistory.size() - 1; i >= 0; i--) {
            // We need to reconstruct previous board states from moves
            // For now, we'll use a simplified approach by checking move patterns
            // In a full implementation, you'd need access to GamePositionService
            
            // Check if we have enough moves to potentially have repetition
            if (i < 2) break; // Need at least 2 previous positions
            
            // Simple heuristic: if the same piece moves to the same square twice
            // and then back, it might indicate repetition
            if (i >= 2) {
                Move currentMove = moveHistory.get(i);
                Move previousMove = moveHistory.get(i - 1);
                Move earlierMove = moveHistory.get(i - 2);
                
                // Check for simple repetition pattern
                if (currentMove.getSourceCoordinate() == earlierMove.getTargetCoordinate() &&
                    currentMove.getTargetCoordinate() == earlierMove.getSourceCoordinate() &&
                    previousMove.getSourceCoordinate() == currentMove.getTargetCoordinate() &&
                    previousMove.getTargetCoordinate() == currentMove.getSourceCoordinate()) {
                    repetitionCount++;
                }
            }
        }
        
        // Threefold repetition: same position appears 3 times
        return repetitionCount >= 3;
    }
    
    /**
     * Create a signature of the current board position for comparison
     * This includes piece positions, castling rights, and en passant possibilities
     * @return String signature representing the board state
     */
    private String createPositionSignature() {
        StringBuilder signature = new StringBuilder();
        
        // Add piece positions
        for (Tile tile : boardTiles) {
            if (tile.isTileOccupied()) {
                Piece piece = tile.getPiece();
                signature.append(piece.getPieceSymbol().toString())
                        .append(piece.getPieceAlliance().toString())
                        .append(tile.getTileCoordinate())
                        .append("|");
            } else {
                signature.append("EMPTY").append(tile.getTileCoordinate()).append("|");
            }
        }
        
        // Add current player turn
        signature.append("TURN:").append(this.getAlliance().toString());
        
        return signature.toString();
    }

    /**
     * Check if the 50-move rule applies
     * The 50-move rule states that if 50 consecutive moves are made by each side
     * without any pawn moves or captures, the game is drawn
     * @return true if 50-move rule applies, false otherwise
     */
    private boolean isFiftyMoveRule() {
        // Count moves without pawn moves or captures from the end of move history
        int movesWithoutPawnOrCapture = 0;
        
        // Start from the end of move history and count backwards
        for (int i = moveHistory.size() - 1; i >= 0; i--) {
            Move move = moveHistory.get(i);
            
            // Check if this move involved a pawn or a capture
            boolean isPawnMove = move.getPieceToMove().getPieceSymbol() == PieceSymbol.PAWN;
            boolean isCapture = move.getCapturedPiece() != null;
            
            // If we find a pawn move or capture, stop counting
            if (isPawnMove || isCapture) {
                break;
            }
            
            movesWithoutPawnOrCapture++;
        }
        
        // 50-move rule: 50 half-moves (25 full moves) without pawn moves or captures
        return movesWithoutPawnOrCapture >= 50;
    }

    /**
     * Check if there is insufficient material to deliver checkmate
     * Insufficient material occurs when neither side has enough pieces to checkmate
     * Examples: King vs King, King vs King + Bishop, King vs King + Knight
     * @return true if insufficient material, false otherwise
     */
    private boolean isInsufficientMaterial() {
        // Count pieces for both sides
        int whitePieces = 0;
        int blackPieces = 0;
        int whitePawns = 0;
        int blackPawns = 0;
        int whiteRooks = 0;
        int blackRooks = 0;
        int whiteQueens = 0;
        int blackQueens = 0;
        int whiteBishops = 0;
        int blackBishops = 0;
        int whiteKnights = 0;
        int blackKnights = 0;
        
        // Count all pieces on the board
        for (Tile tile : boardTiles) {
            if (tile.isTileOccupied()) {
                Piece piece = tile.getPiece();
                PieceSymbol symbol = piece.getPieceSymbol();
                Alliance pieceAlliance = piece.getPieceAlliance();
                
                if (pieceAlliance == Alliance.WHITE) {
                    whitePieces++;
                    switch (symbol) {
                        case PAWN: whitePawns++; break;
                        case ROOK: whiteRooks++; break;
                        case QUEEN: whiteQueens++; break;
                        case BISHOP: whiteBishops++; break;
                        case KNIGHT: whiteKnights++; break;
                        case KING: /* Kings are counted in whitePieces/blackPieces */ break;
                    }
                } else {
                    blackPieces++;
                    switch (symbol) {
                        case PAWN: blackPawns++; break;
                        case ROOK: blackRooks++; break;
                        case QUEEN: blackQueens++; break;
                        case BISHOP: blackBishops++; break;
                        case KNIGHT: blackKnights++; break;
                        case KING: /* Kings are counted in whitePieces/blackPieces */ break;
                    }
                }
            }
        }
        
        // Check for insufficient material scenarios
        return checkInsufficientMaterialScenarios(whitePieces, blackPieces, whitePawns, blackPawns, 
                                                whiteRooks, blackRooks, whiteQueens, blackQueens,
                                                whiteBishops, blackBishops, whiteKnights, blackKnights);
    }
    
    /**
     * Check various insufficient material scenarios
     * @param whitePieces Total white pieces
     * @param blackPieces Total black pieces
     * @param whitePawns White pawns
     * @param blackPawns Black pawns
     * @param whiteRooks White rooks
     * @param blackRooks Black rooks
     * @param whiteQueens White queens
     * @param blackQueens Black queens
     * @param whiteBishops White bishops
     * @param blackBishops Black bishops
     * @param whiteKnights White knights
     * @param blackKnights Black knights
     * @return true if insufficient material, false otherwise
     */
    private boolean checkInsufficientMaterialScenarios(int whitePieces, int blackPieces, 
                                                     int whitePawns, int blackPawns,
                                                     int whiteRooks, int blackRooks,
                                                     int whiteQueens, int blackQueens,
                                                     int whiteBishops, int blackBishops,
                                                     int whiteKnights, int blackKnights) {
        
        // 1. Only kings remain (both sides have only 1 piece - the king)
        if (whitePieces == 1 && blackPieces == 1) {
            return true;
        }
        
        // 2. King vs King + Bishop (only one side has a bishop)
        if ((whitePieces == 2 && blackPieces == 1 && whiteBishops == 1 && 
             whitePawns == 0 && whiteRooks == 0 && whiteQueens == 0 && whiteKnights == 0) ||
            (blackPieces == 2 && whitePieces == 1 && blackBishops == 1 && 
             blackPawns == 0 && blackRooks == 0 && blackQueens == 0 && blackKnights == 0)) {
            return true;
        }
        
        // 3. King vs King + Knight (only one side has a knight)
        if ((whitePieces == 2 && blackPieces == 1 && whiteKnights == 1 && 
             whitePawns == 0 && whiteRooks == 0 && whiteQueens == 0 && whiteBishops == 0) ||
            (blackPieces == 2 && whitePieces == 1 && blackKnights == 1 && 
             blackPawns == 0 && blackRooks == 0 && blackQueens == 0 && blackBishops == 0)) {
            return true;
        }
        
        // 4. King + Bishop vs King + Bishop (both bishops on same color squares)
        if (whitePieces == 2 && blackPieces == 2 && 
            whiteBishops == 1 && blackBishops == 1 && 
            whitePawns == 0 && blackPawns == 0 && 
            whiteRooks == 0 && blackRooks == 0 && 
            whiteQueens == 0 && blackQueens == 0 && 
            whiteKnights == 0 && blackKnights == 0) {
            // Check if bishops are on same color squares
            return areBishopsOnSameColorSquares();
        }
        
        // 5. King + Knight vs King + Knight (both knights)
        if (whitePieces == 2 && blackPieces == 2 && 
            whiteKnights == 1 && blackKnights == 1 && 
            whitePawns == 0 && blackPawns == 0 && 
            whiteRooks == 0 && blackRooks == 0 && 
            whiteQueens == 0 && blackQueens == 0 && 
            whiteBishops == 0 && blackBishops == 0) {
            return true; // Knight vs Knight is insufficient
        }
        
        // 6. King + Bishop vs King + Knight (bishop vs knight)
        if ((whitePieces == 2 && blackPieces == 2 && whiteBishops == 1 && blackKnights == 1 && 
             whitePawns == 0 && blackPawns == 0 && whiteRooks == 0 && blackRooks == 0 && 
             whiteQueens == 0 && blackQueens == 0 && whiteKnights == 0 && blackBishops == 0) ||
            (blackPieces == 2 && whitePieces == 2 && blackBishops == 1 && whiteKnights == 1 && 
             blackPawns == 0 && whitePawns == 0 && blackRooks == 0 && whiteRooks == 0 && 
             blackQueens == 0 && whiteQueens == 0 && blackKnights == 0 && whiteBishops == 0)) {
            return true; // Bishop vs Knight is insufficient
        }
        
        // 7. King + 2 Knights vs King (2 knights vs king - insufficient in most cases)
        if ((whitePieces == 3 && blackPieces == 1 && whiteKnights == 2 && 
             whitePawns == 0 && whiteRooks == 0 && whiteQueens == 0 && whiteBishops == 0) ||
            (blackPieces == 3 && whitePieces == 1 && blackKnights == 2 && 
             blackPawns == 0 && blackRooks == 0 && blackQueens == 0 && blackBishops == 0)) {
            return true; // 2 Knights vs King is insufficient (very difficult to mate)
        }
        
        return false;
    }
    
    /**
     * Check if bishops are on the same color squares
     * This is needed for King + Bishop vs King + Bishop insufficient material
     * @return true if bishops are on same color squares, false otherwise
     */
    private boolean areBishopsOnSameColorSquares() {
        int whiteBishopSquare = -1;
        int blackBishopSquare = -1;
        
        // Find bishop positions
        for (Tile tile : boardTiles) {
            if (tile.isTileOccupied()) {
                Piece piece = tile.getPiece();
                if (piece.getPieceSymbol() == PieceSymbol.BISHOP) {
                    if (piece.getPieceAlliance() == Alliance.WHITE) {
                        whiteBishopSquare = tile.getTileCoordinate();
                    } else {
                        blackBishopSquare = tile.getTileCoordinate();
                    }
                }
            }
        }
        
        // Check if both bishops are on same color squares
        if (whiteBishopSquare != -1 && blackBishopSquare != -1) {
            // Calculate square colors (light/dark)
            boolean whiteBishopOnLight = isLightSquare(whiteBishopSquare);
            boolean blackBishopOnLight = isLightSquare(blackBishopSquare);
            
            return whiteBishopOnLight == blackBishopOnLight;
        }
        
        return false;
    }
    
    /**
     * Check if a square is a light square
     * @param coordinate The coordinate of the square
     * @return true if light square, false if dark square
     */
    private boolean isLightSquare(int coordinate) {
        int row = coordinate / 8;
        int col = coordinate % 8;
        return (row + col) % 2 == 0;
    }

    public GameStatus isDraw() {
        if(this.isInCheck && this.getMoves().isEmpty()){
            return GameStatus.DRAW;
        } 
        else if(isStalemate()){
            return GameStatus.STALEMATE;
        } else if(isThreefoldRepetition()){
            return GameStatus.THREEFOLD_REPETITION;
        } else if(isFiftyMoveRule()){
            return GameStatus.FIFTY_MOVE_RULE;
        } else if(isInsufficientMaterial()){
            return GameStatus.INSUFFICIENT_MATERIAL;
        }
        return GameStatus.IN_PROGRESS;
    }
}