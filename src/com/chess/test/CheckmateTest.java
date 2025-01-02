package com.chess.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Arrays;
import com.chess.model.Alliance;
import com.chess.model.board.IBoard;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Rook;
import com.chess.model.pieces.Bishop;
import com.chess.model.moves.Move;
import com.chess.model.tiles.Tile;
import java.util.List;

class CheckmateTest {
    
    @Test
    void testKingInCheck() {
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(7, Alliance.BLACK);
        Rook blackRook = new Rook(52, Alliance.BLACK);
        
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
        King whiteKing = new King(60, Alliance.WHITE);
        King blackKing = new King(7, Alliance.BLACK);
        Bishop whiteBishop = new Bishop(52, Alliance.WHITE);
        Rook blackRook = new Rook(44, Alliance.BLACK);
        
        IBoard board = IBoard.createRandomBoard(Arrays.asList(whiteKing, blackKing, whiteBishop, blackRook));
        List<Tile> tiles = board.getTiles();
        
        Collection<Move> bishopMoves = whiteBishop.calculateMoves(tiles, board.getOpponentPlayer());
        
        // Bishop should be pinned and have no legal moves
        assertTrue(bishopMoves == null || bishopMoves.isEmpty());
    }
} 