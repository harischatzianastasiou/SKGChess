package com.chess.engine;

import javax.swing.*;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.board.Tile;
import com.chess.engine.Alliance;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
public class SKGChessGUI {

    private JFrame frame;
    private JPanel boardPanel;
    private JButton[][] boardButtons;
    private Board board;
    private Tile selectedTile;
    private Piece selectedPiece;
    private Alliance currentPlayer;

    public SKGChessGUI() {
        this.frame = new JFrame("SKG Chess Engine");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.setSize(600, 600);
        this.frame.setLayout(new BorderLayout());

        this.boardPanel = new JPanel(new GridLayout(8, 8));
        this.boardButtons = new JButton[8][8];
        this.board = Board.createStandardBoard();
        this.currentPlayer = Alliance.WHITE; // Start with white

        initializeBoard();
        this.frame.add(boardPanel, BorderLayout.CENTER);
        this.frame.setVisible(true);
    }

    private void initializeBoard() {
        for (int row = 7; row >= 0; row--) { // Start from 7 and go to 0
            for (int col = 0; col < 8; col++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(75, 75));
                button.setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                button.addActionListener(new PieceMoveListener(row, col));
                boardButtons[row][col] = button;
                boardPanel.add(button);
            }
        }
        updateBoard();
    }

    private void updateBoard() {
        for (int row = 7; row >= 0; row--) { // Start from 7 and go to 0
            for (int col = 0; col < 8; col++) {
                Tile tile = board.getTile(row * 8 + col);
                if(tile.isTileOccupied()) {
	                Piece piece = tile.getPiece();
	                String pieceSymbol = piece.getPieceSymbol().toString().toLowerCase();
	                String colorPrefix = piece.getPieceAlliance().isWhite() ? "white" : "black";
	                String imagePath = "/images/" + colorPrefix + "_" + pieceSymbol + ".png";
	                java.net.URL imageUrl = this.getClass().getResource(imagePath);
	                if (imageUrl != null) {
	                    ImageIcon icon = new ImageIcon(new ImageIcon(imageUrl).getImage().getScaledInstance(75, 75, Image.SCALE_SMOOTH));
	                    boardButtons[row][col].setIcon(icon);
	                } else {
	                    System.err.println("Image not found: " + imagePath);
	                    boardButtons[row][col].setIcon(null);
	                }
                }
            }
        }
        boardPanel.repaint(); // Ensure the board is repainted
    }

    private class PieceMoveListener implements ActionListener {
        private int row;
        private int col;

        public PieceMoveListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedTile == null) {
                selectedTile = board.getTile(row * 8 + col);
                if (selectedTile.isTileOccupied() && selectedTile.getPiece().getPieceAlliance() == currentPlayer) {
                    selectedPiece = selectedTile.getPiece();
                } else {
                    selectedTile = null; // Reset if the selected piece is not the current player's
                }
            } else {
                Tile destinationTile = board.getTile(row * 8 + col);
//                if (selectedPiece != null) {
////                    Collection<Move> legalMoves = selectedPiece.calculateMoves(tiles, isInCheck, oppositeKingCoordinate, oppositeKingSideCastlePath, oppositeQueenSideCastlePath));
////                    for (Move move : legalMoves) {
////                        if (move.getTargetCoordinate() == destinationTile.getTileCoordinate()) {
////                            board = move.execute();
////                            currentPlayer = currentPlayer.isWhite() ? Alliance.BLACK : Alliance.WHITE; // Switch turns
////                            break;
////                        }
////                    }
//                }
                selectedTile = null;
                selectedPiece = null;
                updateBoard();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SKGChessGUI::new);
    }
}