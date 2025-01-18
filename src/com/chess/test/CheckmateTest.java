package com.chess.test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Arrays;
import com.chess.model.Alliance;
import com.chess.model.board.IBoard;
import com.chess.model.pieces.*;
import com.chess.model.moves.Move;
import com.chess.model.tiles.Tile;
import java.util.List;

class CheckmateTest {
    
    private void assertNoLegalMoves(IBoard board, Alliance alliance) {
        for (Tile tile : board.getTiles()) {
            if (tile.isTileOccupied() && tile.getPiece().getPieceAlliance() == alliance) {
                Collection<Move> moves = tile.getPiece().calculateMoves(board.getTiles(), board.getOpponentPlayer());
                if (moves != null && !moves.isEmpty()) {
                    System.out.println("Piece at " + tile.getTileCoordinate() + " (" + 
                        tile.getPiece().getClass().getSimpleName() + ") has legal moves:");
                    for (Move move : moves) {
                        System.out.println("  Can move to: " + move.getTargetCoordinate());
                    }
                }
                assertTrue(moves == null || moves.isEmpty(), 
                    "Piece at " + tile.getTileCoordinate() + " should have no legal moves");
            }
        }
    }
    
    @Test
    void testKingInCheck() {
        King blackKing = new King(7, Alliance.BLACK);
        Rook blackRook = new Rook(52, Alliance.BLACK);
        King whiteKing = new King(60, Alliance.WHITE);

        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, blackRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> kingMoves = whiteKing.calculateMoves(tiles, board.getOpponentPlayer());
        Collection<Move> rookMoves = blackRook.calculateMoves(tiles, board.getCurrentPlayer());
        
        // King should be in check (rook controls the square)
        assertTrue(rookMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == whiteKing.getPieceCoordinate()));
        
        // King should have legal moves to escape
        assertTrue(kingMoves.stream().anyMatch(move -> 
            move.getTargetCoordinate() == 59 || // d1
            move.getTargetCoordinate() == 61)); // f1
    }
    
    @Test
    void testPinnedPiece() {
        King blackKing = new King(7, Alliance.BLACK);
        Rook blackRook = new Rook(44, Alliance.BLACK);
        Bishop whiteBishop = new Bishop(52, Alliance.WHITE);
        King whiteKing = new King(60, Alliance.WHITE);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteBishop, blackRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Bishop should be pinned and have no legal moves
        assertTrue(bishopMoves == null || bishopMoves.isEmpty());
    }

    @Test
    void testBackRankMate() {
        // Classic back rank mate with rook
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(4, Alliance.BLACK);
        Rook blackRook = new Rook(56, Alliance.BLACK);
        Pawn whitePawnF = new Pawn(51, Alliance.WHITE); 
        Pawn whitePawnG = new Pawn(52, Alliance.WHITE);
        Pawn whitePawnH = new Pawn(53, Alliance.WHITE);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, blackRook,
            whitePawnF, whitePawnG, whitePawnH));
            
        // Verify that White has no legal moves (is in checkmate)
        assertNoLegalMoves(board, Alliance.WHITE);
    }
    
    @Test
    void testSmotheredMate() {
        // Knight checkmate with king trapped by own pieces
        King whiteKing = new King(62, Alliance.WHITE); // g1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Knight blackKnight = new Knight(47, Alliance.BLACK); // f3
        Rook blackRook = new Rook(14, Alliance.BLACK);
        Pawn whitePawnF = new Pawn(53, Alliance.WHITE); // f2
        Pawn whitePawnG = new Pawn(54, Alliance.WHITE); // g2
        Pawn whitePawnH = new Pawn(55, Alliance.WHITE); // h2
        Bishop whiteBishop = new Bishop(61, Alliance.WHITE); // f2
        Rook whiteRook = new Rook(63, Alliance.WHITE); // g2
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, blackKnight, blackRook,
            whitePawnF, whitePawnG, whitePawnH, whiteBishop, whiteRook));
            
        // Verify that White has no legal moves (is in checkmate)
        assertNoLegalMoves(board, Alliance.WHITE);
    }
    
    @Test
    void testQueenAndPawnMate() {
        // Queen checkmate supported by pawn
        King whiteKing = new King(62, Alliance.WHITE); // g1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Queen blackQueen = new Queen(54, Alliance.BLACK); // f1
        Pawn blackPawn = new Pawn(45, Alliance.BLACK); // f2
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, blackQueen, blackPawn));
            
        // Verify that White has no legal moves (is in checkmate)
        assertNoLegalMoves(board, Alliance.WHITE);
    }
    
    @Test
    void testDoubleBishopMate() {
        // Checkmate with two bishops
        King whiteKing = new King(63, Alliance.WHITE); // h1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Bishop blackBishop1 = new Bishop(35, Alliance.BLACK); // f3
        Bishop blackBishop2 = new Bishop(36, Alliance.BLACK); // b3
        Pawn whitePawn = new Pawn(55, Alliance.WHITE);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, blackBishop1, blackBishop2, whitePawn));
            
        // Verify that White has no legal moves (is in checkmate)
        assertNoLegalMoves(board, Alliance.WHITE);
    }
    
    @Test
    void testQueenAndRookMate() {
        // Checkmate with queen and rook cooperation
        King whiteKing = new King(60, Alliance.WHITE); // e1
        King blackKing = new King(4, Alliance.BLACK); // e8
        Queen blackQueen = new Queen(50, Alliance.BLACK); // d1
        Rook blackRook = new Rook(58, Alliance.BLACK); // c1
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, blackQueen, blackRook));
            
        // Verify that White has no legal moves (is in checkmate)
        assertNoLegalMoves(board, Alliance.WHITE);
    }
    
    @Test
    void testKnightAndBishopMate() {
        // Checkmate with knight and bishop cooperation
        King whiteKing = new King(63, Alliance.WHITE); // h1
        Pawn whitePawn1 = new Pawn(55, Alliance.WHITE);
        Pawn whitePawn2 = new Pawn(62, Alliance.WHITE);
        King blackKing = new King(4, Alliance.BLACK); // e8
        Knight blackKnight = new Knight(46, Alliance.BLACK); // g3
        Bishop blackBishop = new Bishop(45, Alliance.BLACK); // f3
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(
            whiteKing, blackKing, blackKnight, blackBishop, whitePawn1, whitePawn2));
            
        // Verify that White has no legal moves (is in checkmate)
        assertNoLegalMoves(board, Alliance.WHITE);
    }
} 