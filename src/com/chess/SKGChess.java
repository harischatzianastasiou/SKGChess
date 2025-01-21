package com.chess;

import com.chess.ai.AIPlayer;
import com.chess.ai.evaluation.StandardBoardEvaluator;
import com.chess.ai.strategy.MinimaxStrategy;
import com.chess.controller.GameController;
import com.chess.model.Alliance;
import com.chess.model.board.IBoard;
import com.chess.model.moves.Move;
import com.chess.view.ChessBoardUI;
import com.chess.view.GameModeSelector;

public class SKGChess {
    public static void main(String[] args) {
        // Show game mode selection dialog
        GameModeSelector modeSelector = new GameModeSelector();
        if (!modeSelector.isSelectionMade()) {
            return; // User closed the dialog
        }

        boolean isHumanVsHuman = modeSelector.isHumanVsHuman();
        Alliance playerAlliance = modeSelector.getPlayerAlliance();
        
        // Create AI player with opposite alliance of the human player
        AIPlayer aiPlayer = null;
        if (!isHumanVsHuman) {
            Alliance aiAlliance = playerAlliance == Alliance.WHITE ? Alliance.BLACK : Alliance.WHITE;
            MinimaxStrategy strategy = new MinimaxStrategy(new StandardBoardEvaluator(), 3); // depth of 3
            aiPlayer = new AIPlayer(strategy, aiAlliance);
        }
        
        IBoard board = IBoard.createStandardBoard();

        while (true) {
            try {
                GameController controller = new GameController(board);
                ChessBoardUI chessBoardUI = ChessBoardUI.getInstance(board);

                // If it's AI's turn in human vs AI mode
                if (!isHumanVsHuman && board.getCurrentPlayer().getAlliance() == aiPlayer.getAlliance()) {
                    Move aiMove = aiPlayer.makeMove(board);
                    board = aiMove.execute();
                    chessBoardUI = ChessBoardUI.getInstance(board);
                } else {
                    board = controller.executeMove(chessBoardUI);
                }
                
                if (controller.isCheckmate()) {
                    chessBoardUI = ChessBoardUI.getInstance(board);
                    chessBoardUI.displayCheckmateMessage(board.getOpponentPlayer().getAlliance());
                    break;
                } else if (controller.isDraw()) {
                    chessBoardUI = ChessBoardUI.getInstance(board);
                    chessBoardUI.displayDrawMessage();
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions gracefully
            }
        }
    }
}