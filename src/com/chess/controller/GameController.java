package com.chess.controller;

import java.util.Collection;
import java.util.List;

import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.pieces.Bishop;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.Piece.PieceSymbol;
import com.chess.model.player.CurrentPlayer;
import com.chess.model.tiles.Tile;
import com.chess.util.GameHistory;
import com.chess.util.SoundPlayer;
import com.chess.view.ChessBoardUI;

public class GameController {

    private boolean isCheckmate = false;
    private boolean isDraw = false;
    private IBoard board;
    private final GameHistory history;

    public GameController(IBoard board) {
        this.board = board;
        this.history = GameHistory.getInstance();
    }

    public IBoard executeMove(ChessBoardUI chessBoardUI) {
        chessBoardUI.waitForPlayerMove();
        Collection<Move> currentPlayerMoves = board.getCurrentPlayer().getMoves();

        for (Move currentPlayerMove : currentPlayerMoves) {
            if (currentPlayerMove.getSourceCoordinate() == chessBoardUI.getSourceTile().getTileCoordinate() &&
                currentPlayerMove.getTargetCoordinate() == chessBoardUI.getTargetTile().getTileCoordinate()) {
                GameHistory.getInstance().addBoard(board);
                GameHistory.getInstance().addMove(currentPlayerMove);
                
                // Play appropriate sound based on move type
                if(currentPlayerMove instanceof CapturingMove) {
                    SoundPlayer.playCaptureSound();
                } else {
                    SoundPlayer.playMoveSound();
                }
    
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ///   With each move a new board is created to represent the new state of the tiles and the turn of the next players.     //           
                ///   At least one tile has now changed to occupied or empty (tiles hold pieces).                                         // 
                ///   Also Current and Opponent player have changed.                                                                      //
                ///   The new current player gets the opposite color of the previous current player. Same applies for the opponent.       //
                ///   Tiles, current player and opponent player are created with the board, and are immutable afterwards.                 //
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                
                board = currentPlayerMove.execute(); 
                
                CurrentPlayer currentPlayer = (CurrentPlayer) board.getCurrentPlayer();

                if (currentPlayer.isCheckmate()) {
                    chessBoardUI.addMoveToHistory(currentPlayerMove, "#");       
                    SoundPlayer.playCheckmateSound();
                    this.isCheckmate = true;
                } else if(currentPlayer.isInCheck()) {
                    System.out.println("Checking");
                    chessBoardUI.addMoveToHistory(currentPlayerMove, "+");       
                    SoundPlayer.playCheckSound();
                } else {
                    chessBoardUI.addMoveToHistory(currentPlayerMove, "");
                }
                
                break;
            }
        }
        return board;
    }

    public boolean isCheckmate() {
        return this.isCheckmate;
    }

    public boolean isDraw() {
        return isStalemate() || isThreefoldRepetition() || isFiftyMoveRule() || isInsufficientMaterial();
    }

    public boolean isStalemate() {
        CurrentPlayer currentPlayer = (CurrentPlayer) board.getCurrentPlayer();
        return !currentPlayer.isInCheck() && currentPlayer.getMoves().isEmpty();
    }

    public boolean isThreefoldRepetition() {
        IBoard currentPosition = board;
        int repetitionCount = 1; // Current position counts as 1
        
        List<IBoard> previousPositions = history.getBoards();
        for (int i = previousPositions.size() - 1; i >= 0; i--) {
            IBoard historicBoard = previousPositions.get(i);
            if (isSamePosition(currentPosition, historicBoard)) {
                repetitionCount++;
                if (repetitionCount >= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isSamePosition(IBoard board1, IBoard board2) {
        // Compare piece positions and colors
        List<Tile> tiles1 = board1.getTiles();
        List<Tile> tiles2 = board2.getTiles();
        
        for (int i = 0; i < tiles1.size(); i++) {
            Piece piece1 = tiles1.get(i).getPiece();
            Piece piece2 = tiles2.get(i).getPiece();
            
            if (piece1 == null && piece2 != null) return false;
            if (piece1 != null && piece2 == null) return false;
            if (piece1 != null && piece2 != null) {
                if (piece1.getPieceSymbol() != piece2.getPieceSymbol() ||
                    piece1.getPieceAlliance() != piece2.getPieceAlliance()) {
                    return false;
                }
            }
        }
        
        // Same side to move
        return board1.getCurrentPlayer().getAlliance() == board2.getCurrentPlayer().getAlliance();
    }

    public boolean isFiftyMoveRule() {
        Move lastMove = history.getLastMove();
        if (lastMove == null) return false;
        
        // Check last 50 moves (100 half-moves) for pawn moves or captures
        int moveCount = 0;
        List<Move> moves = history.getMoveHistory();
        for (int i = moves.size() - 1; i >= 0 && moveCount < 100; i--, moveCount++) {
            Move move = moves.get(i);
            if (move.getPieceToMove().getPieceSymbol() == PieceSymbol.PAWN || 
                move.getCapturedPiece() != null) {
                return false;
            }
        }
        
        return moveCount >= 100;
    }

    public boolean isInsufficientMaterial() {
        Collection<Piece> whitePieces = board.getCurrentPlayer().getPieces();
        Collection<Piece> blackPieces = board.getOpponentPlayer().getPieces();
        
        // King vs King
        if (whitePieces.size() == 1 && blackPieces.size() == 1) {
            return true;
        }
        
        // King and Bishop/Knight vs King
        if ((whitePieces.size() == 2 && blackPieces.size() == 1) ||
            (whitePieces.size() == 1 && blackPieces.size() == 2)) {
            Collection<Piece> morePieces = whitePieces.size() > blackPieces.size() ? whitePieces : blackPieces;
            for (Piece piece : morePieces) {
                if (piece.getPieceSymbol() == PieceSymbol.BISHOP ||
                    piece.getPieceSymbol() == PieceSymbol.KNIGHT) {
                    return true;
                }
            }
        }
        
        // King and Bishop vs King and Bishop (same colored squares)
        if (whitePieces.size() == 2 && blackPieces.size() == 2) {
            Bishop whiteBishop = null;
            Bishop blackBishop = null;
            
            for (Piece piece : whitePieces) {
                if (piece.getPieceSymbol() == PieceSymbol.BISHOP) {
                    whiteBishop = (Bishop) piece;
                }
            }
            for (Piece piece : blackPieces) {
                if (piece.getPieceSymbol() == PieceSymbol.BISHOP) {
                    blackBishop = (Bishop) piece;
                }
            }
            
            if (whiteBishop != null && blackBishop != null) {
                // Check if bishops are on same colored squares
                return (whiteBishop.getPieceCoordinate() + whiteBishop.getPieceCoordinate() % 2) ==
                       (blackBishop.getPieceCoordinate() + blackBishop.getPieceCoordinate() % 2);
            }
        }
        
        return false;
    }
}