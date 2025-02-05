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
import  com.chess.util.Sounduser;
import com.chess.util.PGNParser;
import java.util.Collection;

public class GameManager {

    private IBoard currentBoard;
    private GameStatus gameStatus;
    private DrawType drawType;
    private int moveCount;
    private String pgnMoves;
    private Move lastMove;

    public GameManager(String fen, int moveCount, String pgn, boolean isCastled) {
        this.lastMove = getLastMoveFromPgn();
        this.currentBoard = IBoard.createBoardFromFEN(fen,lastMove,isCastled);
        this.gameStatus = updateGameStatus();
        this.moveCount = moveCount;
        this.pgnMoves = pgn; 
    }

    public Move getLastMoveFromPgn() {
        if (pgnMoves == null || pgnMoves.trim().isEmpty()) {
            return null;
        }
        
        List<String> moves = PGNParser.parseMoves(pgnMoves);
        if (moves.isEmpty()) {
            return null;
        }
        
        String lastMoveNotation = moves.get(moves.size() - 1);
        // Convert the PGN move notation to a Move object using the current board state
        Collection<Move> legalMoves = currentBoard.getCurrentPlayer().getMoves();
        
        // Find the matching move from legal moves based on the PGN notation
        for (Move move : legalMoves) {
            if (move.toString().equals(lastMoveNotation)) {
                return move;
            }
        }
        return null;
    }

    public boolean isThreefoldRepetitionFromPgn() {
        if (pgnMoves == null || pgnMoves.trim().isEmpty()) {
            return false;
        }

        // Get the current position's FEN without the move counters
        String currentFen = currentBoard.getFEN();
        currentFen = currentFen.substring(0, currentFen.lastIndexOf(" ")); // Remove halfmove and fullmove counters

        // Count occurrences of the current position in the PGN history
        int repetitionCount = 1; // Current position counts as 1
        IBoard boardState = IBoard.createBoardFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", null, false);
        
        List<String> moves = PGNParser.parseMoves(pgnMoves);
        for (String moveNotation : moves) {
            Collection<Move> legalMoves = boardState.getCurrentPlayer().getMoves();
            for (Move move : legalMoves) {
                if (move.toString().equals(moveNotation)) {
                    boardState = move.execute();
                    String fenPosition = boardState.getFEN();
                    fenPosition = fenPosition.substring(0, fenPosition.lastIndexOf(" ")); // Remove move counters
                    
                    if (fenPosition.equals(currentFen)) {
                        repetitionCount++;
                        if (repetitionCount >= 3) {
                            return true;
                        }
                    }
                    break;
                }
            }
        }
        
        return false;
    }

    public boolean isFiftyMoveRuleFromPgn() {
        if (pgnMoves == null || pgnMoves.trim().isEmpty()) {
            return false;
        }

        List<String> moves = PGNParser.parseMoves(pgnMoves);
        if (moves.isEmpty()) {
            return false;
        }

        int movesWithoutPawnOrCapture = 0;
        IBoard currentPosition = IBoard.createBoardFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", null, false);

        for (String moveNotation : moves) {
            Collection<Move> legalMoves = currentPosition.getCurrentPlayer().getMoves();
            for (Move move : legalMoves) {
                if (move.toString().equals(moveNotation)) {
                    // Check if move is a pawn move or capture
                    if (move.getPieceToMove().getPieceSymbol() == PieceSymbol.PAWN || 
                        move instanceof CapturingMove) {
                        movesWithoutPawnOrCapture = 0;
                    } else {
                        movesWithoutPawnOrCapture++;
                    }
                    currentPosition = move.execute();
                    break;
                }
            }
        }

        return movesWithoutPawnOrCapture >= 100; // 50 moves by each user = 100 half-moves
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
        Collection<Move> currentPlayerMoves = currentBoard.getCurrentPlayer().getMoves();

        for (Move currentPlayerMove : currentPlayerMoves) {
            if (currentPlayerMove.getSourceCoordinate() == move.getSourceCoordinate() &&
                currentPlayerMove.getTargetCoordinate() == move.getTargetCoordinate()) {
                return executeSelectedMove(currentPlayerMove);
            }
        }
        return currentBoard;
    }

    public Collection<Move> getMoves(){
        return currentBoard.getCurrentPlayer().getMoves();
    }

    public IBoard getBoard() {
        return currentBoard;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }


    public boolean isCheckmate() {
        CurrentPlayer currentPlayer = (CurrentPlayer) currentBoard.getCurrentPlayer();
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
        CurrentPlayer currentPlayer = (CurrentPlayer) currentBoard.getCurrentPlayer();
        
        // Check all draw conditions
        if (!currentPlayer.isInCheck() && currentPlayer.getMoves().isEmpty()) {
            return DrawType.STALEMATE;
        } 
        if (isThreefoldRepetitionFromPgn()) {
            return DrawType.THREEFOLD_REPETITION;
        } 
        if (isFiftyMoveRuleFromPgn()) {
            return DrawType.FIFTY_MOVE_RULE;
        } 
        if (isInsufficientMaterial()) {
            return DrawType.INSUFFICIENT_MATERIAL;
        }
        
        return null;  // Not a draw
    }

    public boolean isInsufficientMaterial() {
        Collection<Piece> whitePieces = currentBoard.getCurrentPlayer().getPieces();
        Collection<Piece> blackPieces = currentBoard.getOpponentPlayer().getPieces();
        
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
            Sounduser.playCaptureSound();
        } else {
            Sounduser.playMoveSound();
        }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ///   With each move a new currentBoard is created to represent the new state of the tiles and the turn of the next users.     //           
            ///   At least one tile has now changed to occupied or empty (tiles hold pieces).                                         // 
            ///   Also Current and Opponent user have changed.                                                                      //
            ///   The new current user gets the opposite color of the previous current user. Same applies for the opponent.       //
            ///   Tiles, current user and opponent user are created with the currentBoard, and are immutable afterwards.                 //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(selectedMove.execute()!= null){
                this.currentBoard = selectedMove.execute(); 
            }
    
        CurrentPlayer currentPlayer = (CurrentPlayer) this.getBoard().getCurrentPlayer();

        if (currentPlayer.isCheckmate()) {
            Sounduser.playCheckmateSound();
        } else if(currentPlayer.isInCheck()) {
            Sounduser.playCheckSound();
        }
        return this.currentBoard;
    }

    public void setBoard(IBoard currentBoard) {
        this.currentBoard = currentBoard;
    }
}