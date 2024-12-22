package com.chess.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.Point;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.moves.capturing.CapturingMove;
import com.chess.model.moves.capturing.PawnEnPassantAttack;
import com.chess.model.moves.capturing.PawnPromotionCapturingMove;
import com.chess.model.moves.noncapturing.KingSideCastleMove;
import com.chess.model.moves.noncapturing.PawnJumpMove;
import com.chess.model.moves.noncapturing.PawnMove;
import com.chess.model.moves.noncapturing.PawnPromotionMove;
import com.chess.model.moves.noncapturing.QueenSideCastleMove;
import com.chess.model.pieces.CalculateMoveUtils;
import com.chess.model.pieces.Pawn;
import com.chess.model.tiles.Tile;
import com.chess.model.pieces.Piece;
import com.chess.model.board.IBoard;
import com.chess.model.player.Player;
import com.chess.model.player.CurrentPlayer;
import com.chess.util.GameHistory;
import com.chess.util.SoundPlayer;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.Component;
public class ChessBoardUI {
    
    private static ChessBoardUI instance;
    private final JFrame gameFrame;
    private final BoardPanel boardPanel;
    private final JPanel moveHistoryPanel;
    private final JTextArea moveHistoryTextArea;
    private IBoard chessboard;
	private Player currentPlayer;
    private Tile sourceTile;
    private Tile targetTile;
    private Piece sourceSelectedPiece;
	private Piece targetSelectedPiece;
	private Map<Integer, Set<Integer>> pieceLegalMoves;
    private boolean boardIsFlipped;

    private volatile boolean moveSelected = false;

    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(743, 655);
    private final static Dimension BOARD_PANEL_DIMENSION = new Dimension(580, 580);
    private final static Dimension TILE_PANEL_DIMENSION = new Dimension(64, 64);
    private final static Dimension ANNOTATION_PANEL_DIMENSION = new Dimension(20, 655);
    private final static Dimension HISTORY_PANEL_DIMENSION = new Dimension(130, 580);
    private final static String pieceImagesPath = "images/";
    private final Color lightTileColor = Color.decode("#eeeed2");
    private final Color darkTileColor = Color.decode("#003166");
    private final Color annotationBackgroundColor = Color.decode("#ffffff");
    private final Color annotationTextColor = Color.decode("#003166");
    private final Color historyPanelColor = Color.decode("#eeeed2");
    private final Color historyTitleBackgroundColor = new Color(51, 51, 51);
    private final Color historyTitleTextColor = Color.WHITE;
    private final Color moveTextColor = new Color(0, 0, 0);
    
    private int lastMoveSource = -1;
    private int lastMoveTarget = -1;
    private final Color lastMoveColor = new Color(170, 162, 58, 90); // Light smooth green with transparency
    
    private boolean isViewingHistory = false;
    private int currentHistoryIndex = -1;

    private final Color moveHighlightColor = new Color(255, 255, 0, 128); // Brighter yellow with more opacity

    private ChessBoardUI(IBoard board) {
        this.gameFrame = new JFrame("SKGChess");
        this.gameFrame.setLayout(new BorderLayout());
        this.gameFrame.setJMenuBar(createChessTableMenuBar());
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.chessboard = board;
        this.currentPlayer = chessboard.getCurrentPlayer();
		this.pieceLegalMoves = getPieceLegalCoordinates(this.currentPlayer);
        
        // Initialize board panel first
        this.boardPanel = new BoardPanel();
        
        // Initialize move history panel
        this.moveHistoryPanel = new JPanel(new BorderLayout(0, 0));
        moveHistoryPanel.setPreferredSize(HISTORY_PANEL_DIMENSION);
        moveHistoryPanel.setBackground(historyPanelColor);
        moveHistoryPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(200, 200, 200)));
        
        // Create title panel
        JPanel titlePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(historyTitleBackgroundColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        titlePanel.setPreferredSize(new Dimension(HISTORY_PANEL_DIMENSION.width, 40));
        
        // Add title with custom font and styling
        JLabel historyTitle = new JLabel("Move History", SwingConstants.CENTER);
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyTitle.setForeground(historyTitleTextColor);
        historyTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        titlePanel.add(historyTitle, BorderLayout.CENTER);
        
        // Initialize move history text area with custom styling
        this.moveHistoryTextArea = new JTextArea() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                                   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                super.paint(g);
            }
        };
        moveHistoryTextArea.setEditable(false);
        moveHistoryTextArea.setFont(new Font("Consolas", Font.BOLD, 16));
        moveHistoryTextArea.setBackground(historyPanelColor);
        moveHistoryTextArea.setForeground(moveTextColor);
        moveHistoryTextArea.setMargin(new Insets(10, 10, 10, 10));
        moveHistoryTextArea.setLineWrap(true);
        moveHistoryTextArea.setWrapStyleWord(true);
        moveHistoryTextArea.setCaret(new DefaultCaret() {
            @Override
            public boolean isVisible() {
                return false;
            }
        });
        
        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(moveHistoryTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(historyPanelColor);
        
        // Customize scroll bar
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setOpaque(false);
        verticalBar.setPreferredSize(new Dimension(8, 0));
        verticalBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(150, 150, 150, 100);
            }
            
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                // Don't paint the track
            }
            
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if(thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                    return;
                }
                
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1,
                               thumbBounds.width - 2, thumbBounds.height - 2,
                               5, 5);
                g2.dispose();
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
        
        // Add components to the history panel
        moveHistoryPanel.add(titlePanel, BorderLayout.NORTH);
        moveHistoryPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create navigation buttons panel
        JPanel navigationPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        navigationPanel.setBackground(historyPanelColor);
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create left (previous) button
        JButton previousButton = new JButton("\u2190"); // Left arrow unicode
        previousButton.setFont(new Font("Dialog", Font.BOLD, 20));
        previousButton.setFocusPainted(false);

        // Create right (next) button
        JButton nextButton = new JButton("\u2192"); // Right arrow unicode
        nextButton.setFont(new Font("Dialog", Font.BOLD, 20));
        nextButton.setFocusPainted(false);
        nextButton.setEnabled(false); // Initially disabled since we start at latest position

        previousButton.addActionListener(e -> {
            List<IBoard> boardHistory = GameHistory.getInstance().getBoards();
            List<Move> moveHistory = GameHistory.getInstance().getMoveHistory();
            if (currentHistoryIndex == -1) {
                // If at current position, go to the last board in history
                currentHistoryIndex = boardHistory.size() - 1;
            } else if (currentHistoryIndex > 0) {
                currentHistoryIndex--;
            }
            if (currentHistoryIndex >= 0) {
                isViewingHistory = true;
                boardPanel.drawBoard(boardHistory.get(currentHistoryIndex));
                nextButton.setEnabled(true); // Enable next button since we can go forward
                previousButton.setEnabled(currentHistoryIndex > 0);
                
                // Update move highlighting
                highlightMoveInHistory(currentHistoryIndex);
                // Update last move squares highlighting
                updateLastMoveHighlighting(currentHistoryIndex > 0 ? moveHistory.get(currentHistoryIndex - 1) : null);
                
                // Play the sound of the next move that will be made from this position
                if (currentHistoryIndex < moveHistory.size()) {
                    Move nextMove = moveHistory.get(currentHistoryIndex);
                    if (nextMove instanceof CapturingMove) {
                        SoundPlayer.playCaptureSound();
                    } else {
                        SoundPlayer.playMoveSound();
                    }
                    
                    // Then check for check/checkmate on the next position
                    if (currentHistoryIndex + 1 < boardHistory.size()) {
                        IBoard nextBoard = boardHistory.get(currentHistoryIndex + 1);
                        if (nextBoard.getCurrentPlayer() instanceof CurrentPlayer) {
                            CurrentPlayer currentPlayer = (CurrentPlayer) nextBoard.getCurrentPlayer();
                            if (currentPlayer.isCheckmate()) {
                                SoundPlayer.playCheckmateSound();
                            } else if (currentPlayer.isInCheck()) {
                                SoundPlayer.playCheckSound();
                            }
                        }
                    }
                }
            }
        });

        nextButton.addActionListener(e -> {
            List<IBoard> boardHistory = GameHistory.getInstance().getBoards();
            List<Move> moveHistory = GameHistory.getInstance().getMoveHistory();
            if (currentHistoryIndex < boardHistory.size() - 1) {
                currentHistoryIndex++;
                boardPanel.drawBoard(boardHistory.get(currentHistoryIndex));
                previousButton.setEnabled(true);
                
                // Update move highlighting
                highlightMoveInHistory(currentHistoryIndex - 1);
                // Update last move squares highlighting
                updateLastMoveHighlighting(moveHistory.get(currentHistoryIndex - 1));
                
                // Play the sound of the move that got us to this position
                Move moveToShow = moveHistory.get(currentHistoryIndex - 1);
                if (moveToShow instanceof CapturingMove) {
                    SoundPlayer.playCaptureSound();
                } else {
                    SoundPlayer.playMoveSound();
                }
                
                // Then check for check/checkmate on the current position
                IBoard currentBoard = boardHistory.get(currentHistoryIndex);
                if (currentBoard.getCurrentPlayer() instanceof CurrentPlayer) {
                    CurrentPlayer currentPlayer = (CurrentPlayer) currentBoard.getCurrentPlayer();
                    if (currentPlayer.isCheckmate()) {
                        SoundPlayer.playCheckmateSound();
                    } else if (currentPlayer.isInCheck()) {
                        SoundPlayer.playCheckSound();
                    }
                }
                
                if (currentHistoryIndex == boardHistory.size() - 1) {
                    nextButton.setEnabled(true);
                }
            } else if (currentHistoryIndex == boardHistory.size() - 1) {
                // We're at the last history board and clicked next, go to current
                isViewingHistory = false;
                currentHistoryIndex = -1;
                boardPanel.drawBoard(chessboard);
                nextButton.setEnabled(false);
                
                // Update move highlighting for last move
                highlightMoveInHistory(moveHistory.size() - 1);
                // Update last move squares highlighting for last move
                updateLastMoveHighlighting(moveHistory.get(moveHistory.size() - 1));
                
                // Play the sound of the last move
                Move lastMove = moveHistory.get(moveHistory.size() - 1);
                if (lastMove instanceof CapturingMove) {
                    SoundPlayer.playCaptureSound();
                } else {
                    SoundPlayer.playMoveSound();
                }
                
                // Check for check/checkmate on current position
                if (chessboard.getCurrentPlayer() instanceof CurrentPlayer) {
                    CurrentPlayer currentPlayer = (CurrentPlayer) chessboard.getCurrentPlayer();
                    if (currentPlayer.isCheckmate()) {
                        SoundPlayer.playCheckmateSound();
                    } else if (currentPlayer.isInCheck()) {
                        SoundPlayer.playCheckSound();
                    }
                }
            }
        });

        navigationPanel.add(previousButton);
        navigationPanel.add(nextButton);

        moveHistoryPanel.add(navigationPanel, BorderLayout.SOUTH);
        
        // Create a container panel for board and history
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(boardPanel, BorderLayout.CENTER);
        containerPanel.add(moveHistoryPanel, BorderLayout.EAST);
        
        this.gameFrame.add(containerPanel, BorderLayout.CENTER);
        this.gameFrame.setVisible(true);
    }

    public static ChessBoardUI getInstance(IBoard board) {
        if (instance == null) {
            instance = new ChessBoardUI(board);
        } else {
            instance.sourceTile = null;
            instance.targetTile = null;
            instance.sourceSelectedPiece = null;
			instance.targetSelectedPiece = null;
			instance.chessboard = board;
			instance.currentPlayer = board.getCurrentPlayer();
			instance.pieceLegalMoves = getPieceLegalCoordinates(instance.currentPlayer);
            // Store last move before updating board
            if (GameHistory.getInstance().getLastMove() != null) {
                Move lastMove = GameHistory.getInstance().getLastMove();
                instance.lastMoveSource = lastMove.getSourceCoordinate();
                instance.lastMoveTarget = lastMove.getTargetCoordinate();
            }
            instance.boardPanel.drawBoard(board);
        }
        return instance;
    }

	private static Map<Integer, Set<Integer>> getPieceLegalCoordinates(Player player) {
		Map<Integer, Set<Integer>> pieceMoves = new HashMap<>();
		Collection<Move> legalMoves = player.getMoves();
		
		for(Move legalMove : legalMoves) {
			int sourceCoord = legalMove.getSourceCoordinate();
			pieceMoves.computeIfAbsent(sourceCoord, k -> new HashSet<>())
				.add(legalMove.getTargetCoordinate());
		}
		return pieceMoves;
	}

    private JMenuBar createChessTableMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createViewMenu());
        return menuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");
        
        final JMenuItem openPGN = new JMenuItem("Load PGN File");
        openPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("open up that pgn file!");
            }
        });
        fileMenu.add(openPGN);
        return fileMenu;
    }

    private JMenu createViewMenu() {
        final JMenu viewMenu = new JMenu("View");
        
        final JMenuItem flipBoardItem = new JMenuItem("Flip Board");
        flipBoardItem.setAccelerator(KeyStroke.getKeyStroke('F', java.awt.event.InputEvent.CTRL_DOWN_MASK));
        flipBoardItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flipBoard();
            }
        });
        viewMenu.add(flipBoardItem);
        return viewMenu;
    }
    
    private class BoardPanel extends JPanel{
        final List<TilePanel> boardTiles;
        private final JLayeredPane layeredPane;
        private final JPanel fileAnnotationPanel;
        private final JPanel rankAnnotationPanel;
        private JPanel tilesPanel;
        
        BoardPanel(){
            super(new BorderLayout(0, 0));
            this.layeredPane = new JLayeredPane();
            this.tilesPanel = new JPanel(new GridLayout(8, 8, 0, 0));
            this.boardTiles = new ArrayList<>();
            
            // Create file annotation panel (A-H)
            this.fileAnnotationPanel = new JPanel(new GridLayout(1, 8, 0, 0));
            fileAnnotationPanel.setPreferredSize(new Dimension(BOARD_PANEL_DIMENSION.width, 20));
            fileAnnotationPanel.setBackground(annotationBackgroundColor);
            updateFileAnnotations();
            
            // Create rank annotation panel (1-8)
            this.rankAnnotationPanel = new JPanel(new GridLayout(8, 1, 0, 0));
            rankAnnotationPanel.setPreferredSize(ANNOTATION_PANEL_DIMENSION);
            rankAnnotationPanel.setBackground(annotationBackgroundColor);
            updateRankAnnotations();
            
            setPreferredSize(OUTER_FRAME_DIMENSION);
            layeredPane.setPreferredSize(OUTER_FRAME_DIMENSION);
            tilesPanel.setPreferredSize(BOARD_PANEL_DIMENSION);
            
            // Initialize tiles
            for (int i = 0; i < CalculateMoveUtils.NUM_TILES; i++) {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
            }
            
            // Initial board setup
            setupBoard();
            
            validate();
        }

        private void setupBoard() {
            tilesPanel.removeAll();
            
            // Create a container panel for the board and rank annotation
            JPanel boardContainer = new JPanel(new BorderLayout(0, 0));
            boardContainer.add(rankAnnotationPanel, BorderLayout.WEST);
            boardContainer.add(tilesPanel, BorderLayout.CENTER);
            
            // Set bounds for the container panel
            boardContainer.setBounds(0, 0, 
                BOARD_PANEL_DIMENSION.width + ANNOTATION_PANEL_DIMENSION.width,
                BOARD_PANEL_DIMENSION.height);
            
            // Add tiles in the correct order
            if (boardIsFlipped) {
                for (int rank = 0; rank < 8; rank++) {
                    for (int file = 0; file < 8; file++) {
                        int index = (7 - rank) * 8 + (7 - file);
                        tilesPanel.add(boardTiles.get(index));
                    }
                }
            } else {
                for (int i = 0; i < 64; i++) {
                    tilesPanel.add(boardTiles.get(i));
                }
            }
            
            // Add components to the layered pane
            layeredPane.removeAll();
            layeredPane.add(boardContainer, JLayeredPane.DEFAULT_LAYER);
            
            // Add the main components to the panel
            removeAll();
            add(layeredPane, BorderLayout.CENTER);
            add(fileAnnotationPanel, BorderLayout.SOUTH);
            
            validate();
            repaint();
        }

        private void updateFileAnnotations() {
            fileAnnotationPanel.removeAll();
            char[] files = boardIsFlipped ? 
                new char[]{'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'} :
                new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
            
            for (char file : files) {
                JPanel cellPanel = new JPanel(new GridBagLayout());
                cellPanel.setBackground(annotationBackgroundColor);
                JLabel fileLabel = new JLabel(String.valueOf(file));
                fileLabel.setForeground(annotationTextColor);
                fileLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
                fileLabel.setHorizontalAlignment(SwingConstants.CENTER);
                fileLabel.setVerticalAlignment(SwingConstants.CENTER);
                fileLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
                cellPanel.add(fileLabel);
                fileAnnotationPanel.add(cellPanel);
            }
            fileAnnotationPanel.revalidate();
            fileAnnotationPanel.repaint();
        }

        private void updateRankAnnotations() {
            rankAnnotationPanel.removeAll();
            int startRank = boardIsFlipped ? 1 : 8;
            int endRank = boardIsFlipped ? 8 : 1;
            int step = boardIsFlipped ? 1 : -1;
            
            for (int rank = startRank; rank != endRank + step; rank += step) {
                JLabel rankLabel = new JLabel(String.valueOf(rank), SwingConstants.CENTER);
                rankLabel.setForeground(annotationTextColor);
                rankLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
                rankAnnotationPanel.add(rankLabel);
            }
            rankAnnotationPanel.revalidate();
            rankAnnotationPanel.repaint();
        }

        public void drawBoard(final IBoard board) {
            // Clear drag layer
            Component[] dragLayerComps = layeredPane.getComponentsInLayer(JLayeredPane.DRAG_LAYER);
            for (Component comp : dragLayerComps) {
                layeredPane.remove(comp);
            }
            
            // Update all tiles
            for (TilePanel tilePanel : boardTiles) {
                tilePanel.drawTile(board);
            }
            
            // Rebuild the board layout
            setupBoard();
        }
    }

    public void flipBoard() {
        boardIsFlipped = !boardIsFlipped;
        boardPanel.updateFileAnnotations();
        boardPanel.updateRankAnnotations();
        boardPanel.drawBoard(chessboard);
    }

    public void updateBoard(IBoard board) {
        boardPanel.drawBoard(board);
    }

    public void displayCheckmateMessage(Alliance winningAlliance) {
        JOptionPane.showMessageDialog(null, "Checkmate! " + winningAlliance.toString() + " wins.");
        System.exit(0);
    }

	public Tile getSourceTile() {
		return sourceTile;
	}

	public Tile getTargetTile() {
		return targetTile;
	}

	public void resetTileSelections() {
		this.sourceTile = null;
		this.targetTile = null;
	}

	public void waitForPlayerMove() {
        moveSelected = false;
        while (!moveSelected) {
            try {
                if (isViewingHistory) {
                    Thread.sleep(100); // Don't consume CPU while viewing history
                    continue; // Skip move processing while viewing history
                }
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        moveSelected = false; // Reset for next move
    }

    private Color blendColors(Color base, Color overlay) {
        float alpha = overlay.getAlpha() / 255f;
        int red = (int) ((1 - alpha) * base.getRed() + alpha * overlay.getRed());
        int green = (int) ((1 - alpha) * base.getGreen() + alpha * overlay.getGreen());
        int blue = (int) ((1 - alpha) * base.getBlue() + alpha * overlay.getBlue());
        return new Color(red, green, blue);
    }

    private class DraggablePieceLabel extends JLabel {
        private Point initialPosition;

        public DraggablePieceLabel(ImageIcon icon) {
            super(icon);
            setOpaque(false);
        }

        public void setInitialPosition(Point p) {
            initialPosition = p;
        }

        public void returnToInitialPosition() {
            if (initialPosition != null) {
                setLocation(initialPosition);
                getParent().repaint();
            }
        }
    }

    private TilePanel findTargetTile(Point dropPoint, BoardPanel boardPanel) {
        // Calculate which tile we're over based on coordinates
        int tileSize = BOARD_PANEL_DIMENSION.width / 8;
        int x = dropPoint.x;
        int y = dropPoint.y;
        
        // Convert to tile coordinates
        int file = x / tileSize;
        int rank = y / tileSize;
        
        // Check if within board bounds
        if (file >= 0 && file < 8 && rank >= 0 && rank < 8) {
            // Adjust coordinates if board is flipped
            if (boardIsFlipped) {
                file = 7 - file;
                rank = 7 - rank;
            }
            int tileId = rank * 8 + file;
            return boardPanel.boardTiles.get(tileId);
        }
        return null;
    }

    private class TilePanel extends JPanel {
        private final int tileId;
        private Color highlightColor; // Store the current highlight color
        private DraggablePieceLabel pieceLabel;  // New field to hold the draggable piece

        TilePanel(final BoardPanel boardPanel, final int tileId) {
            super(new GridBagLayout());
            this.tileId = tileId;
            // Calculate tile size based on board size
            int tileSize = BOARD_PANEL_DIMENSION.width / 8;
            setPreferredSize(new Dimension(tileSize, tileSize));
            assignTileColor(tileId);
            if(chessboard!=null) {
                assignTilePieceIcon(chessboard);
            }
            setupDragListeners(boardPanel);
            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (isViewingHistory) {
                        return; // Ignore clicks while viewing history
                    }
                    if (chessboard == null) {
                        JOptionPane.showMessageDialog(null, "Chessboard is not initialized. Please restart the game.");
                        return;
                    }
                    if(isRightMouseButtonClicked(e)) {
                        highlightColor = Color.BLUE;
                        sourceTile = null;
                        targetTile = null;
                        sourceSelectedPiece = null;
                        targetSelectedPiece = null;
                        moveSelected = false;
                        assignTileColor(tileId);
                        repaint();
                        for (TilePanel tile : boardPanel.boardTiles) {
                            tile.drawTile(chessboard);
                        }
                    } else if(isLeftMouseButtonClicked(e)) {
                        //Select source piece
                        if(sourceTile == null) {
                            sourceTile = chessboard.getTile(tileId);
                            sourceSelectedPiece = sourceTile.getPiece();
                            if(sourceSelectedPiece == null || sourceSelectedPiece.getPieceAlliance() != chessboard.getCurrentPlayer().getAlliance()) {
                                sourceTile = null;
                                sourceSelectedPiece = null;
                                assignTileColor(tileId);
                                repaint();
                                return; // Exit the click handler
                            } else {
                                highlightColor = Color.GREEN;
                                setBackground(highlightColor);
                                highlightLegalMoves(sourceSelectedPiece);
                            }
                        }

                        //Select target piece
                        if(sourceSelectedPiece != null) {
                            targetTile = chessboard.getTile(tileId);
                            if(targetTile.isTileOccupied()) {
                                targetSelectedPiece = targetTile.getPiece();
                                if(targetSelectedPiece != null) {
                                    if(targetSelectedPiece.getPieceAlliance() == chessboard.getCurrentPlayer().getAlliance()) {
                                        if (sourceTile != null) {
                                            boardPanel.boardTiles.get(sourceTile.getTileCoordinate()).assignTileColor(sourceTile.getTileCoordinate());
                                            boardPanel.boardTiles.get(sourceTile.getTileCoordinate()).repaint();
                                        }
                                        sourceTile = targetTile;
                                        sourceSelectedPiece = targetSelectedPiece;
                                        targetTile = null;
                                        targetSelectedPiece = null;
                                        highlightColor = Color.GREEN;
                                        highlightLegalMoves(sourceSelectedPiece);
                                    } else {
                                        highlightColor = Color.RED;
                                        moveSelected = true;
                                    }
                                }
                            } else {
                                highlightColor = Color.YELLOW;
                                moveSelected = true;
                            }
                            if(sourceTile != null && targetTile != null) {
                               moveSelected = true;
                            }
                        }
                        setBackground(highlightColor);
                        repaint();
                    }
                }
                
                private boolean isRightMouseButtonClicked(MouseEvent e) {
                    return SwingUtilities.isRightMouseButton(e);
                }
                
                private boolean isLeftMouseButtonClicked(MouseEvent e) {
                    return SwingUtilities.isLeftMouseButton(e);
                }
                @Override
                public void mousePressed(MouseEvent e) {}
                @Override
                public void mouseReleased(MouseEvent e) {}
                @Override
                public void mouseEntered(MouseEvent e) {}
                @Override
                public void mouseExited(MouseEvent e) {}
            });
        }

        public void drawTile(final IBoard board) {
            if (board != null) {
                assignTileColor(tileId);
                assignTilePieceIcon(board);
                validate();
                repaint();
            }
        }

        private void assignTileColor(final int tileId) {
            Color baseColor = CalculateMoveUtils.getCoordinateAlliance(tileId) == Alliance.WHITE ? lightTileColor : darkTileColor;
            
            // If this tile was part of the last move, blend with lastMoveColor
            if (tileId == lastMoveSource || tileId == lastMoveTarget) {
                setBackground(blendColors(baseColor, lastMoveColor));
            } else {
                setBackground(baseColor);
            }
        }
        
        private void assignTilePieceIcon(final IBoard board) {
            if (board != null) {
                this.removeAll();
                Tile tile = board.getTile(tileId);
                if(tile.isTileOccupied()) {
                    Piece piece = tile.getPiece();
                    String pieceSymbol = piece.getPieceSymbol().toString().toLowerCase();
                    String colorPrefix = piece.getPieceAlliance().isWhite() ? "white" : "black";
                    String pieceIconPath = pieceImagesPath + colorPrefix + "_" + pieceSymbol + ".png";
                    try {
                        final BufferedImage image = ImageIO.read(new File(pieceIconPath));
                        ImageIcon icon = new ImageIcon(image.getScaledInstance(
                           BOARD_PANEL_DIMENSION.width / 8,
                           BOARD_PANEL_DIMENSION.height / 8,
                            Image.SCALE_AREA_AVERAGING));
                        pieceLabel = new DraggablePieceLabel(icon);
                        add(pieceLabel);
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    pieceLabel = null;
                }
            }
        }

        private void drawLegalMoveIndicator() {
            if (chessboard != null) {
                removeAll();
                assignTilePieceIcon(chessboard); // Keep the piece icon
                
                // Draw the move indicator dot
                JLabel dot = new JLabel("\u2022"); // Unicode for dot
                dot.setFont(new Font("Dialog", Font.BOLD, 50));
                dot.setForeground(new Color(0, 0, 0, 0.7f)); // More visible black
                dot.setHorizontalAlignment(SwingConstants.CENTER);
                add(dot);
                revalidate(); // Use revalidate instead of validate
                repaint();
            }
        }

        private void highlightLegalMoves(Piece selectedPiece) {
            if (selectedPiece != null) {
                int selectedPieceCoordinate = selectedPiece.getPieceCoordinate();
                Set<Integer> legalTargets = pieceLegalMoves.getOrDefault(selectedPieceCoordinate, new HashSet<>());

                // Only update tiles that need to change
                for (TilePanel tile : boardPanel.boardTiles) {
                    if (legalTargets.contains(tile.tileId)) {
                        tile.drawLegalMoveIndicator();
                    } else {
                        tile.drawTile(chessboard);
                    }
                }
                boardPanel.revalidate();
                boardPanel.repaint();
            }
        }

        private void setupDragListeners(final BoardPanel boardPanel) {
            MouseAdapter dragListener = new MouseAdapter() {
                private Point dragOffset;
                private int sourceTileId;

                @Override
                public void mousePressed(MouseEvent e) {
                    if (pieceLabel != null && 
                        chessboard.getTile(tileId).getPiece().getPieceAlliance() == chessboard.getCurrentPlayer().getAlliance()) {
                        dragOffset = e.getPoint();
                        sourceTileId = tileId;
                        
                        // Create floating piece
                        Point loc = SwingUtilities.convertPoint(pieceLabel, 0, 0, boardPanel.layeredPane);
                        DraggablePieceLabel floatingPiece = new DraggablePieceLabel(
                            (ImageIcon)pieceLabel.getIcon());
                        floatingPiece.setBounds(loc.x, loc.y, 
                            TILE_PANEL_DIMENSION.width, TILE_PANEL_DIMENSION.height);
                        boardPanel.layeredPane.add(floatingPiece, JLayeredPane.DRAG_LAYER);
                        
                        // Hide original piece
                        pieceLabel.setVisible(false);
                        boardPanel.repaint();
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragOffset != null) {
                        Component draggedPiece = boardPanel.layeredPane.getComponentsInLayer(
                            JLayeredPane.DRAG_LAYER)[0];
                        Point newLocation = SwingUtilities.convertPoint(
                            e.getComponent(), e.getPoint(), boardPanel.layeredPane);
                        newLocation.x -= dragOffset.x;
                        newLocation.y -= dragOffset.y;
                        draggedPiece.setLocation(newLocation);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragOffset != null) {
                        Point dropPoint = SwingUtilities.convertPoint(
                            e.getComponent(), e.getPoint(), boardPanel);
                        TilePanel targetTilePanel = findTargetTile(dropPoint, boardPanel);
                        
                        // Remove floating piece
                        Component draggedPiece = boardPanel.layeredPane.getComponentsInLayer(
                            JLayeredPane.DRAG_LAYER)[0];
                        boardPanel.layeredPane.remove(draggedPiece);

                        if (targetTilePanel != null && 
                            pieceLegalMoves.getOrDefault(sourceTileId, new HashSet<>())
                                .contains(targetTilePanel.tileId)) {
                            // Valid move
                            sourceTile = chessboard.getTile(sourceTileId);
                            sourceSelectedPiece = sourceTile.getPiece();
                            targetTile = chessboard.getTile(targetTilePanel.tileId);
                            moveSelected = true;
                            // Hide original piece since move will be executed
                            pieceLabel.setVisible(false);
                        } else {
                            // Invalid move - show original piece
                            pieceLabel.setVisible(true);
                        }
                        
                        dragOffset = null;
                        boardPanel.layeredPane.repaint();
                    }
                }
            };

            addMouseListener(dragListener);
            addMouseMotionListener(dragListener);
        }
    }

    public void addMoveToHistory(Move move) {
        String moveText = formatMove(move);
        if (moveHistoryTextArea.getText().isEmpty()) {
            // First move (White)
            moveHistoryTextArea.append(String.format("1. %-5s", moveText));
        } else {
            String currentText = moveHistoryTextArea.getText();
            if (currentPlayer.getAlliance().isWhite()) {
                // Start new move number with consistent spacing
                moveHistoryTextArea.append(String.format("\n%d. %-5s",
                    currentText.split("\n").length + 1, moveText));
            } else {
                // Add black's move with consistent spacing
                moveHistoryTextArea.append(moveText);
            }
        }
        moveHistoryTextArea.setCaretPosition(moveHistoryTextArea.getDocument().getLength());
        
        // Highlight the latest move in the history
        List<Move> moveHistory = GameHistory.getInstance().getMoveHistory();
        highlightMoveInHistory(moveHistory.size() - 1);
    }

    private String formatMove(Move move) {
        StringBuilder notation = new StringBuilder();
        Piece piece = move.getPieceToMove();
        
        // Get file and rank for coordinates
        int sourceFile = move.getSourceCoordinate() % 8;
        int sourceRank = 7 - (move.getSourceCoordinate() / 8); // Invert rank for correct notation
        int targetFile = move.getTargetCoordinate() % 8;
        int targetRank = 7 - (move.getTargetCoordinate() / 8); // Invert rank for correct notation
        
        // Handle castling first
        if (move instanceof KingSideCastleMove) {
            notation.append("O-O");
        } else if (move instanceof QueenSideCastleMove) {
            notation.append("O-O-O");
        } else {
            // Add piece symbol (except for pawns)
            if (!(piece instanceof Pawn)) {
                notation.append(piece.getPieceSymbol().toString());
            }
            
            // Add capture symbol
            if (move.getCapturedPiece() != null) {
                // For pawn captures, add the source file
                if (piece instanceof Pawn) {
                    notation.append((char)('a' + sourceFile));
                }
                notation.append("x");
            }
            
            // Add destination square
            notation.append((char)('a' + targetFile));
            notation.append(targetRank + 1); // Add 1 since ranks are 1-based
            
            // Add promotion piece
            if (move instanceof PawnPromotionMove || move instanceof PawnPromotionCapturingMove) {
                notation.append("=Q"); // Assuming queen promotion for now
            }
        }
        
        // Execute the move temporarily to check the resulting position
        IBoard futureBoard = move.execute();
        Player futurePlayer = futureBoard.getCurrentPlayer();
        
        // Check if the move results in check or checkmate
        if (futurePlayer instanceof CurrentPlayer) {
            CurrentPlayer currentPlayer = (CurrentPlayer) futurePlayer;
            if (currentPlayer.isCheckmate()) {
                notation.append("#");
            } else if (currentPlayer.isInCheck()) {
                notation.append("+");
            }
        }
        
        return notation.toString();
    }

    private void highlightMoveInHistory(int moveIndex) {
        try {
            // Clear any existing highlights
            moveHistoryTextArea.getHighlighter().removeAllHighlights();
            
            if (moveIndex < 0) {
                return;
            }
            
            String text = moveHistoryTextArea.getText();
            if (text.isEmpty()) {
                return;
            }
            
            // Split into lines and process each line
            String[] lines = text.split("\n");
            int currentMove = 0;
            int currentPosition = 0;
            
            for (String line : lines) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    currentPosition += line.length() + 1; // +1 for newline
                    continue;
                }
                
                // Split the line into move number and moves
                String[] parts = line.split("\\.");
                if (parts.length < 2) {
                    currentPosition += line.length() + 1;
                    continue;
                }
                
                // Get the moves part (after the number and dot)
                String movesText = parts[1].trim();
                String[] moves = movesText.split("\\s+");
                
                // Process each move in the line
                for (String move : moves) {
                    if (!move.trim().isEmpty()) {
                        if (currentMove == moveIndex) {
                            // Find the exact position of this move in the text
                            int startPos = text.indexOf(move, currentPosition);
                            if (startPos >= 0) {
                                int endPos = startPos + move.length();
                                moveHistoryTextArea.getHighlighter().addHighlight(
                                    startPos, 
                                    endPos,
                                    new DefaultHighlighter.DefaultHighlightPainter(moveHighlightColor)
                                );
                                // Ensure the highlighted text is visible
                                moveHistoryTextArea.setCaretPosition(startPos);
                            }
                            return;
                        }
                        currentMove++;
                    }
                }
                currentPosition += line.length() + 1; // +1 for newline
            }
        } catch (Exception e) {
            // Silently handle any highlighting errors
            moveHistoryTextArea.getHighlighter().removeAllHighlights();
        }
    }

    private void updateLastMoveHighlighting(Move move) {
        if (move != null) {
            lastMoveSource = move.getSourceCoordinate();
            lastMoveTarget = move.getTargetCoordinate();
        } else {
            lastMoveSource = -1;
            lastMoveTarget = -1;
        }
        boardPanel.drawBoard(currentHistoryIndex == -1 ? chessboard : GameHistory.getInstance().getBoards().get(currentHistoryIndex));
    }

}