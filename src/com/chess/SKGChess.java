package com.chess;

import java.awt.Point;

import com.chess.core.Alliance;
import com.chess.core.board.IBoard;
import com.chess.core.game.GameController;
import com.chess.core.moves.Move;
import com.chess.core.player.ai.engine.AIPlayer;
import com.chess.core.player.ai.engine.evaluation.StandardBoardEvaluator;
import com.chess.core.player.ai.engine.strategy.MinimaxStrategy;
import com.chess.core.player.ai.engine.strategy.OpeningBookStrategy;
import com.chess.ui.board.ChessBoardUI;
import com.chess.ui.dialogs.GameIntro;
import com.chess.ui.dialogs.GameModeSelector;

public class SKGChess {
    public static void main(String[] args) {
        Point windowLocation = null;

        // Show intro animation
        GameIntro intro = new GameIntro();
        while (!intro.isIntroFinished()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        windowLocation = intro.getLocation();

        // Show game mode selection dialog
        IBoard board = IBoard.createStandardBoard();
        GameModeSelector modeSelector = new GameModeSelector(windowLocation);
        if (!modeSelector.isSelectionMade()) {
            return; // User closed the dialog
        }
        windowLocation = modeSelector.getLocation();

        boolean isHumanVsHuman = modeSelector.isHumanVsHuman();
        Alliance playerAlliance = modeSelector.getPlayerAlliance();
        
        // Create AI player with opposite alliance of the human player
        AIPlayer aiPlayer = null;
        if (!isHumanVsHuman) {
            Alliance aiAlliance = playerAlliance == Alliance.WHITE ? Alliance.BLACK : Alliance.WHITE;
            MinimaxStrategy strategy = new MinimaxStrategy(new StandardBoardEvaluator(), 3);
            OpeningBookStrategy openingBookStrategy = new OpeningBookStrategy(strategy);
            aiPlayer = new AIPlayer(openingBookStrategy, aiAlliance);
        }

        // Initialize the game UI at the same location
        ChessBoardUI chessBoardUI = ChessBoardUI.getInstance(board);
        chessBoardUI.getGameFrame().setLocation(windowLocation);
        chessBoardUI.getGameFrame().setVisible(true);
        
        while (true) {
            try {
                GameController controller = new GameController(board);

                // If it's AI's turn in human vs AI mode
                if (!isHumanVsHuman && board.getCurrentPlayer().getAlliance() == aiPlayer.getAlliance()) {
                    Move aiMove = aiPlayer.makeMove(board);
                    board = controller.executeAIMove(aiMove, chessBoardUI);
                } else {
                    board = controller.executeMove(chessBoardUI);
                }
                
                // Update UI after each move
                chessBoardUI = ChessBoardUI.getInstance(board);
                
                if (controller.isCheckmate()) {
                    chessBoardUI.displayCheckmateMessage(board.getOpponentPlayer().getAlliance());
                    break;
                } else if (controller.isDraw()) {
                    chessBoardUI.displayDrawMessage();
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}