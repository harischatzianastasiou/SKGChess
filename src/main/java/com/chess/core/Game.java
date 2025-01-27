package com.chess.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.chess.core.board.IBoard;
import  com.chess.core.moves.Move;
import  com.chess.core.moves.capturing.CapturingMove;
import  com.chess.core.pieces.Bishop;
import  com.chess.core.pieces.Piece;
import  com.chess.core.pieces.Piece.PieceSymbol;
import  com.chess.core.player.CurrentPlayer;
import  com.chess.core.tiles.Tile;
import  com.chess.util.SoundPlayer;

public class Game {

    private IBoard board;
    private List<IBoard> boardHistory = new ArrayList<>();
    private List<Move> moveHistory = new ArrayList<>();
    private int moveCount = 0;
    private GameStatus gameStatus = GameStatus.ACTIVE;
    private DrawType drawType;


    public Game updateGame(Move move) {
        this.board = executeMove(move);
        this.moveHistory.add(move);
        this.boardHistory.add(board);
        this.moveCount++;
        this.gameStatus = updateGameStatus();
        return this;
    }

    public GameStatus updateGameStatus() {
        if (isCheckmate()) {
            return GameStatus.CHECKMATE;
        }else if(getDrawType() != null) {
            this.drawType = getDrawType();
            return GameStatus.DRAW;
        }else{
            return GameStatus.ACTIVE;
        }
    }

    public IBoard executeMove(Move move) {
        Collection<Move> currentPlayerMoves = board.getCurrentPlayer().getMoves();

        for (Move currentPlayerMove : currentPlayerMoves) {
            if (currentPlayerMove.getSourceCoordinate() == move.getSourceCoordinate() &&
                currentPlayerMove.getTargetCoordinate() == move.getTargetCoordinate()) {
                return executeSelectedMove(currentPlayerMove);
            }
        }
        return board;
    }

    public Collection<Move> getMoves(){
        return board.getCurrentPlayer().getMoves();
    }

    public IBoard getBoard() {
        return board;
    }

    public Move getLastMove() {
        return moveHistory.get(moveHistory.size() - 1);
    }

    public List<IBoard> getBoardHistory() {
        return boardHistory;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public boolean isCheckmate() {
        CurrentPlayer currentPlayer = (CurrentPlayer) board.getCurrentPlayer();
        return currentPlayer.isCheckmate();
    }

    public enum GameStatus {
        ACTIVE,
        CHECKMATE,
        DRAW
    }
    
    public enum DrawType {
        STALEMATE("Draw by Stalemate"),
        THREEFOLD_REPETITION("Draw by Threefold Repetition"),
        FIFTY_MOVE_RULE("Draw by Fifty Move Rule"),
        INSUFFICIENT_MATERIAL("Draw by Insufficient Material");

        private final String description;

        DrawType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private DrawType getDrawType() {
        CurrentPlayer currentPlayer = (CurrentPlayer) board.getCurrentPlayer();
        
        // Check all draw conditions
        if (!currentPlayer.isInCheck() && currentPlayer.getMoves().isEmpty()) {
            return DrawType.STALEMATE;
        } 
        if (isThreefoldRepetition()) {
            return DrawType.THREEFOLD_REPETITION;
        } 
        if (isFiftyMoveRule()) {
            return DrawType.FIFTY_MOVE_RULE;
        } 
        if (isInsufficientMaterial()) {
            return DrawType.INSUFFICIENT_MATERIAL;
        }
        
        return null;  // Not a draw
    }

    public boolean isThreefoldRepetition() {
        int repetitionCount = 1; // Current position counts as 1
        
        for (int i = boardHistory.size() - 1; i >= 0; i--) {
            if (isSamePosition(board, boardHistory.get(i))) {
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
        if (moveHistory.get(moveHistory.size() - 1) == null) return false;
        
        for (int i = moveHistory.size() - 1; i >= 0 && moveCount < 100; i--, moveCount++) {
            Move move = moveHistory.get(i);
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

    public IBoard executeAIMove(Move aiMove) {
        return executeSelectedMove(aiMove);
    }

    private IBoard executeSelectedMove(Move selectedMove) {
        
        // Play appropriate sound based on move type
        if(selectedMove instanceof CapturingMove) {
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
            if(selectedMove.execute()!= null){
                this.board = selectedMove.execute(); 
            }
    
        CurrentPlayer currentPlayer = (CurrentPlayer) this.getBoard().getCurrentPlayer();

        if (currentPlayer.isCheckmate()) {
            SoundPlayer.playCheckmateSound();
        } else if(currentPlayer.isInCheck()) {
            SoundPlayer.playCheckSound();
        }
        return this.board;
    }

    public void setBoard(IBoard board) {
        this.board = board;
    }
}