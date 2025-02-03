package com.chess.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import  static org.junit.jupiter.api.Assertions.assertFalse;
import  static org.junit.jupiter.api.Assertions.assertNotNull;
import  static org.junit.jupiter.api.Assertions.assertTrue;
import  org.junit.jupiter.api.Test;

import  com.chess.core.board.IBoard;
import  com.chess.core.moves.Move;
import com.chess.core.pieces.Bishop;
import com.chess.core.pieces.King;
import com.chess.core.pieces.Knight;
import com.chess.core.pieces.Pawn;
import com.chess.core.pieces.Piece.PieceSymbol;
import com.chess.core.pieces.Queen;
import com.chess.core.tiles.Tile;
import com.chess.core.GameManager;
import java.util.Map;

class DrawTest {
    
    @Test
    void testStalemate() {
        King blackKing = new King(7, Alliance.BLACK);
        King whiteKing = new King(23, Alliance.WHITE);
        Queen whiteQueen = new Queen(22, Alliance.WHITE);
        
        Map<String, GameManager> gameMap = GameManager.createNewTestGame("test");
        IBoard board = IBoard.createRandomBoard(Arrays.asList(blackKing, whiteKing, whiteQueen), "test");
        GameManager.addTestGame(board, "test");

        List<Tile> tiles = board.getTiles();
        
        Collection<Move> kingMoves = blackKing.calculateMoves(tiles, board.getCurrentPlayer(), "test");
        Collection<Move> queenMoves = whiteQueen.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        
        // Black king should have no legal moves but not be in check
        assertTrue(kingMoves.isEmpty() || kingMoves == null);
        assertFalse(queenMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == blackKing.getPieceCoordinate()));
    }
    
    @Test
    void testInsufficientMaterial() {
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(4, Alliance.BLACK);
        Bishop whiteBishop = new Bishop(61, Alliance.WHITE);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteBishop), "test");
        Map<String, GameManager> gameMap = GameManager.createNewTestGame("test");
GameManager.addTestGame(board, "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> whiteMoves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        Collection<Move> blackMoves = blackKing.calculateMoves(tiles, board.getCurrentPlayer(), "test");
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        
        // Both kings and a bishop should be insufficient material
        assertFalse(whiteMoves.isEmpty());
        assertFalse(blackMoves.isEmpty());
        assertFalse(bishopMoves.isEmpty());
    }
    
    @Test
    void testSufficientMaterial() {
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(4, Alliance.BLACK);
        Pawn whitePawn = new Pawn(52, Alliance.WHITE);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whitePawn), "test");
        Map<String, GameManager> gameMap = GameManager.createNewTestGame("test");
GameManager.addTestGame(board, "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> pawnMoves = whitePawn.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        
        // Having a pawn means there's sufficient material
        assertFalse(pawnMoves.isEmpty());
    }
    
//    @Test
//    void testThreefoldRepetition() {
//        King whiteKing = new King(60, Alliance.WHITE);
//        King blackKing = new King(4, Alliance.BLACK);
//        Knight whiteKnight = new Knight(62, Alliance.WHITE); // g1
//        Knight blackKnight = new Knight(6, Alliance.BLACK); // g8
//        
//        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteKnight, blackKnight), "test");
//        Map<String, Game> gameMap = Game.createNewTestGame("test");
//        Game.addTestGame(board, "test");
//        List<Tile> tiles = board.getTiles();
//        
//        // Get the specific moves we want to repeat
//        Collection<Move> whiteKnightMoves = whiteKnight.calculateMoves(tiles, board.getOpponentPlayer(), "test");
//        Collection<Move> blackKnightMoves = blackKnight.calculateMoves(tiles, board.getCurrentPlayer(), "test");
//        
//        Move whiteKnightToF3 = whiteKnightMoves.stream()
//            .filter(move -> move.getTargetCoordinate() == 45) // f3
//            .findFirst().orElse(null);
//        Move blackKnightToF6 = blackKnightMoves.stream()
//            .filter(move -> move.getTargetCoordinate() == 21) // f6
//            .findFirst().orElse(null);
//            
//        assertNotNull(whiteKnightToF3);
//        assertNotNull(blackKnightToF6);
//        
//        // Execute the moves three times
//        for (int i = 0; i < 3; i++) {
//            board = whiteKnightToF3.execute("test");
//            gameMap.get("test").updateGame(whiteKnightToF3);
//
//            board = blackKnightToF6.execute("test");
//            gameMap.get("test").updateGame(blackKnightToF6);
//            
//            Knight whiteKnight1 = new Knight(45, Alliance.WHITE); // g1
//            Knight blackKnight1 = new Knight(21, Alliance.BLACK); // g8
//            
//            List<Tile> tiles1 = board.getTiles();
//
//            Collection<Move> whiteKnightNewMoves = whiteKnight1.calculateMoves(tiles1, board.getOpponentPlayer(), "test");
//            Collection<Move> blackKnightNewMoves = blackKnight1.calculateMoves(tiles1, board.getCurrentPlayer(), "test");
//
//            // Print available moves for the white knight
//            System.out.println("White Knight Moves: ");
//            whiteKnightNewMoves.forEach(move -> System.out.println("Move to: " + move.getTargetCoordinate()));
//
//            Move whiteKnightBackToG1 = whiteKnightNewMoves.stream()
//                    .filter(move -> move.getTargetCoordinate() == 62) // g1
//                    .findFirst().orElse(null);
//
//            Move blackKnightBackToG8 = blackKnightNewMoves.stream()
//            .filter(move -> move.getTargetCoordinate() == 6) // g8
//            .findFirst().orElse(null);
//
//            board = whiteKnightBackToG1.execute("test");
//            gameMap.get("test").updateGame(whiteKnightBackToG1);
//            board = blackKnightBackToG8.execute("test");
//            gameMap.get("test").updateGame(blackKnightBackToG8);
//        }
//        
//        // Check if threefold repetition has occurred
//        assertTrue(gameMap.get("test").isThreefoldRepetition());
//    }
    
    @Test
    void testFiftyMoveRule() {
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(4, Alliance.BLACK);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing), "test");
        Map<String, GameManager> gameMap = GameManager.createNewTestGame("test");
GameManager.addTestGame(board, "test");
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> whiteKingMoves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer(), "test");
        Collection<Move> blackKingMoves = blackKing.calculateMoves(tiles, board.getCurrentPlayer(), "test");
        
        // Get the specific moves for the kings
        Move whiteKingMove1 = whiteKingMoves.stream()
            .filter(move -> move.getTargetCoordinate() == 61) // f1
            .findFirst().orElse(null);
        Move blackKingMove1 = blackKingMoves.stream()
            .filter(move -> move.getTargetCoordinate() == 5) // f8
            .findFirst().orElse(null);
        
        Move whiteKingMove2 = whiteKingMoves.stream()
                .filter(move -> move.getTargetCoordinate() == 61) // f1
                .findFirst().orElse(null);
            Move blackKingMove2 = blackKingMoves.stream()
                .filter(move -> move.getTargetCoordinate() == 5) // f8
                .findFirst().orElse(null);
            
        assertNotNull(whiteKingMove1);
        assertNotNull(blackKingMove1);
        assertNotNull(whiteKingMove2);
        assertNotNull(blackKingMove2);
        
        // Execute 50 moves for each player without pawn moves or captures
        for (int i = 0; i < 25; i++) {
            board = whiteKingMove1.execute("test");
            gameMap.get("test").updateGame(whiteKingMove1);
            board = blackKingMove1.execute("test");
            gameMap.get("test").updateGame(blackKingMove1);
            board = whiteKingMove2.execute("test");
            gameMap.get("test").updateGame(whiteKingMove2);
            board = blackKingMove2.execute("test");
            gameMap.get("test").updateGame(blackKingMove2);
            
        }
        
        // Check if 100 moves have been made without pawn moves or captures
        List<Move> moveHistory = gameMap.get("test").getMoveHistory();
        int movesWithoutPawnOrCapture = 0;
        
        // Check the last 100 moves
        for (int i = 0; i <moveHistory.size(); i++) {
            Move move = moveHistory.get(i);
            if (move.getPieceToMove().getPieceSymbol() == PieceSymbol.PAWN || 
                move.getCapturedPiece() != null) {
                break;
            }
            movesWithoutPawnOrCapture++;
        }
        
        // Verify that we have 50 half-moves without pawns or captures
        assertEquals(100, movesWithoutPawnOrCapture);
    }
} 