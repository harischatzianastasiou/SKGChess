package com.chess.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
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
import java.util.Set;
import java.awt.Font;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.chess.model.Alliance;
import com.chess.model.moves.Move;
import com.chess.model.pieces.CalculateMoveUtils;
import com.chess.model.tiles.Tile;
import com.chess.model.pieces.Piece;
import com.chess.model.board.IBoard;
import com.chess.model.player.Player;
import com.chess.util.GameHistory;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.Component;
public class ChessBoardUI {
    
    private static ChessBoardUI instance;
    private final JFrame gameFrame;
    private final BoardPanel boardPanel;
    private IBoard chessboard;
	private Player currentPlayer;
    private Tile sourceTile;
    private Tile targetTile;
    private Piece sourceSelectedPiece;
	private Piece targetSelectedPiece;
	private Map<Integer, Set<Integer>> pieceLegalMoves;

    private volatile boolean moveSelected = false;

    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(613, 655);
    private final static Dimension BOARD_PANEL_DIMENSION = new Dimension(580, 580);
    private final static Dimension TILE_PANEL_DIMENSION = new Dimension(64, 64);
    private final static Dimension ANNOTATION_PANEL_DIMENSION = new Dimension(20, 655);
    private final static String pieceImagesPath = "images/";
    private final Color lightTileColor = Color.decode("#eeeed2");
    private final Color darkTileColor = Color.decode("#003166");
    private final Color annotationBackgroundColor = Color.decode("#ffffff");
    private final Color annotationTextColor = Color.decode("#003166");
    
    private int lastMoveSource = -1;
    private int lastMoveTarget = -1;
    private final Color lastMoveColor = new Color(170, 162, 58, 90); // Light smooth green with transparency
    
    
    
    private ChessBoardUI(IBoard board) {
        this.gameFrame = new JFrame("SKGChess");
        this.gameFrame.setLayout(new BorderLayout());
        this.gameFrame.setJMenuBar(createChessTableMenuBar());
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.chessboard = board;
        this.currentPlayer = chessboard.getCurrentPlayer();
		this.pieceLegalMoves = getPieceLegalCoordinates(this.currentPlayer);
        this.boardPanel = new BoardPanel();
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
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
    
    private class BoardPanel extends JPanel{
        final List<TilePanel> boardTiles;
        private final JLayeredPane layeredPane;
        private final JPanel fileAnnotationPanel;
        private final JPanel rankAnnotationPanel;
        
        BoardPanel(){
            super(new BorderLayout(0, 0));
            this.layeredPane = new JLayeredPane();
            JPanel tilesPanel = new JPanel(new GridLayout(8, 8, 0, 0));
            this.boardTiles = new ArrayList<>();
            
            // Create file annotation panel (A-H)
            this.fileAnnotationPanel = new JPanel(new GridLayout(1, 8, 0, 0));
            fileAnnotationPanel.setPreferredSize(new Dimension(BOARD_PANEL_DIMENSION.width, 20));
            fileAnnotationPanel.setBackground(annotationBackgroundColor);
            for (char file = 'a'; file <= 'h'; file++) {
                JPanel cellPanel = new JPanel(new GridBagLayout());
                cellPanel.setBackground(annotationBackgroundColor);
                JLabel fileLabel = new JLabel(String.valueOf(file));
                fileLabel.setForeground(annotationTextColor);
                fileLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
                fileLabel.setHorizontalAlignment(SwingConstants.CENTER);
                fileLabel.setVerticalAlignment(SwingConstants.CENTER);
                // Add right padding to the text
                fileLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
                cellPanel.add(fileLabel);
                fileAnnotationPanel.add(cellPanel);
            }
            
            // Create rank annotation panel (1-8)
            this.rankAnnotationPanel = new JPanel(new GridLayout(8, 1, 0, 0));
            rankAnnotationPanel.setPreferredSize(ANNOTATION_PANEL_DIMENSION);
            rankAnnotationPanel.setBackground(annotationBackgroundColor);
            for (int rank = 8; rank >= 1; rank--) {
                JLabel rankLabel = new JLabel(String.valueOf(rank), SwingConstants.CENTER);
                rankLabel.setForeground(annotationTextColor);
                rankLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
                rankAnnotationPanel.add(rankLabel);
            }
            
            // Create a container panel for the board and rank annotation
            JPanel boardContainer = new JPanel(new BorderLayout(0, 0));
            boardContainer.add(rankAnnotationPanel, BorderLayout.WEST);
            boardContainer.add(tilesPanel, BorderLayout.CENTER);
            
            setPreferredSize(OUTER_FRAME_DIMENSION);
            layeredPane.setPreferredSize(OUTER_FRAME_DIMENSION);
            tilesPanel.setPreferredSize(BOARD_PANEL_DIMENSION);
            
            // Set bounds for the container panel
            boardContainer.setBounds(0, 0, 
                BOARD_PANEL_DIMENSION.width + ANNOTATION_PANEL_DIMENSION.width,
                BOARD_PANEL_DIMENSION.height);
            
            for (int i = 0; i < CalculateMoveUtils.NUM_TILES; i++) {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                tilesPanel.add(tilePanel);
            }
            
            // Add components to the layered pane
            layeredPane.add(boardContainer, JLayeredPane.DEFAULT_LAYER);
            
            // Add the main components to the panel
            add(layeredPane, BorderLayout.CENTER);
            add(fileAnnotationPanel, BorderLayout.SOUTH);
            
            validate();
        }
        
        public void drawBoard(final IBoard board) {
        	// Clear drag layer
        	Component[] dragLayerComps = layeredPane.getComponentsInLayer(JLayeredPane.DRAG_LAYER);
        	for (Component comp : dragLayerComps) {
        		layeredPane.remove(comp);
        	}
        	
        	for(final TilePanel tilePanel : boardTiles) {
        		tilePanel.drawTile(board);
        	}
        	validate();
        	repaint();
        }
    }
    
    private class TilePanel extends JPanel{
        
        private final int tileId;
        private Color highlightColor; // Store the current highlight color
        private DraggablePieceLabel pieceLabel;  // New field to hold the draggable piece

        
        TilePanel(final BoardPanel boardPanel, final int tileId){
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
	                }else if(isLeftMouseButtonClicked(e)) {
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
										sourceSelectedPiece= targetSelectedPiece;
										targetTile = null;
										targetSelectedPiece = null;
										highlightColor = Color.GREEN;
										highlightLegalMoves(sourceSelectedPiece);
									}else {
										highlightColor = Color.RED;
										moveSelected = true;
									}
								}
							}else{
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
	            }
            );
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
	                }catch(IOException e) {
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
                    }
					else{
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
            int tileId = rank * 8 + file;
            return boardPanel.boardTiles.get(tileId);
        }
        return null;
    }

}