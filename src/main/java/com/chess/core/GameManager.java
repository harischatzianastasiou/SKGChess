package com.chess.core;

import java.util.Collection;
import java.util.List;

import com.chess.core.board.IBoard;
import com.chess.core.moves.Move;
import  com.chess.core.moves.capturing.CapturingMove;
import  com.chess.core.pieces.Bishop;
import  com.chess.core.pieces.Piece;
import  com.chess.core.pieces.Piece.PieceSymbol;
import  com.chess.core.player.CurrentPlayer;
import  com.chess.util.PGNParser;
import  com.chess.util.Sounduser;

public class GameManager {

    private IBoard currentBoard;
    private GameStatus gameStatus;
    private DrawType drawType;
    private int moveCount;
    private Move lastMove;

    public GameManager(String fen, String lastMovePgn, boolean isCastled, int moveCount) {
        // First create the current board from FEN
        this.currentBoard = IBoard.createBoardFromFEN(fen, null, isCastled);
        
        // Then construct the last move by working backward from the current position
        this.lastMove = getLastMoveFromPgn(lastMovePgn);
        
        // Update game status based on the PGN
        this.gameStatus = updateGameStatus(lastMovePgn);
        
        // Update the board with the last move
        this.currentBoard = IBoard.createBoardFromFEN(fen, this.lastMove, isCastled);
        this.moveCount = moveCount;
    }

    public Move getLastMoveFromPgn(String pgn) {
        if (pgn == null || pgn.trim().isEmpty()) {
            return null;
        }
        
        List<String> moves = PGNParser.parseMoves(pgn);
        if (moves.isEmpty()) {
            return null;
        }
        
        return null;
    }

    public boolean isThreefoldRepetitionFromPgn(String pgn) {
        if (pgn == null || pgn.trim().isEmpty()) {
            return false;
        }

        // Get the current position's FEN without the move counters
        String currentFen = currentBoard.getFEN();
        currentFen = currentFen.substring(0, currentFen.lastIndexOf(" ")); // Remove halfmove and fullmove counters

        // Count occurrences of the current position in the PGN history
        int repetitionCount = 1; // Current position counts as 1
        IBoard boardState = IBoard.createBoardFromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", null, false);
        
        List<String> moves = PGNParser.parseMoves(pgn);
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

    public boolean isFiftyMoveRuleFromPgn(String pgn) {
        if (pgn == null || pgn.trim().isEmpty()) {
            return false;
        }

        List<String> moves = PGNParser.parseMoves(pgn);
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


    public GameStatus updateGameStatus(String pgn) {
        if (isCheckmate()) {
            return GameStatus.CHECKMATE;
        }else if(getDrawType(pgn) != null) {
            this.drawType = getDrawType(pgn);
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

    private DrawType getDrawType(String pgn) {
        CurrentPlayer currentPlayer = (CurrentPlayer) currentBoard.getCurrentPlayer();
        
        // Check all draw conditions
        if (!currentPlayer.isInCheck() && currentPlayer.getMoves().isEmpty()) {
            return DrawType.STALEMATE;
        } 
        if (isThreefoldRepetitionFromPgn(pgn)) {
            return DrawType.THREEFOLD_REPETITION;
        } 
        if (isFiftyMoveRuleFromPgn(pgn)) {
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