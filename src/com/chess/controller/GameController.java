package com.chess.controller;

import java.util.ArrayList;
import java.util.Collection;

import com.chess.model.board.Board;
import com.chess.model.moves.Move;
import com.chess.model.moves.MoveResult;
import com.chess.model.moves.noncapturing.KingSideCastleMove;
import com.chess.model.moves.noncapturing.QueenSideCastleMove;
import com.chess.model.pieces.Bishop;
import com.chess.model.pieces.King;
import com.chess.model.pieces.Knight;
import com.chess.model.pieces.Pawn;
import com.chess.model.pieces.Piece;
import com.chess.model.pieces.Queen;
import com.chess.model.pieces.Rook;
import com.chess.model.tiles.Tile;
import com.chess.test.BishopTest;
import com.chess.test.KingTest;
import com.chess.test.KnightTest;
import com.chess.test.PawnTest;
import com.chess.test.QueenTest;
import com.chess.test.RookTest;
import com.chess.util.GameHistory;
import com.chess.view.ChessBoardUI;

public class GameController {

    private boolean isCheckmate = false;
    private static GameController instance;

    private GameController() {}

    public static GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    public Board executeMove(Board chessboard, ChessBoardUI chessBoardUI) {
        MoveValidator moveValidator = new MoveValidator(chessboard,chessBoardUI);
        Board updatedBoard = moveValidator.validateMove();
        this.isCheckmate = moveValidator.isCheckmate();
        return updatedBoard;
    }

    public boolean isCheckmate() {
        return this.isCheckmate;
    }
}
