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
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.chess.model.Alliance;
import com.chess.model.board.Board;
import com.chess.model.board.BoardUtils;
import com.chess.model.moves.Move;
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
import com.chess.controller.GameController;

public class ChessBoardUI {
    
    private final JFrame gameFrame;
    private final BoardPanel boardPanel;
    private Board chessboard;
    private GameController gameController;
    private Tile sourceTile;
    private Tile targetTile;
    private Piece selectedPiece;
    boolean isCheckmate;

    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private final static Dimension BOARD_PANEL_DIMENSION = new Dimension (400, 350);
    private final static Dimension TILE_PANEL_DIMENSION = new Dimension (70, 70); // Increased size
    private final static String pieceImagesPath = "images/";
    private final Color lightTileColor = Color.decode("#eeeed2");
    private final Color darkTileColor = Color.decode("#769656");
    
    
    
    public ChessBoardUI(GameController gameController) {
        this.gameController = gameController;
        this.gameFrame = new JFrame("SKGChess");
        this.gameFrame.setLayout(new BorderLayout());
        this.gameFrame.setJMenuBar(createChessTableMenuBar());
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.chessboard = Board.createStandardBoard();
        this.boardPanel = new BoardPanel();
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        this.gameFrame.setVisible(true);
        this.isCheckmate = false;
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
        
        BoardPanel(){
            super(new GridLayout(8,8));
            this.boardTiles = new ArrayList<>();
            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel); // Add each TilePanel to the BoardPanel
            }
            setPreferredSize(BOARD_PANEL_DIMENSION);
            validate();
        }
        
        public void drawBoard(final Board board) {
        	removeAll();
        	for(final TilePanel tilePanel : boardTiles) {
        		tilePanel.drawTile(board);
        		add(tilePanel);
        	}
        	validate();
        	repaint();
        }
    }
    
    private class TilePanel extends JPanel{
        
        private final int tileId;
        private Color highlightColor; // Store the current highlight color

        
        TilePanel(final BoardPanel boardPanel, final int tileId){
            super(new GridBagLayout());
            this.tileId = tileId;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor(tileId);
            if(chessboard!=null) {
            	assignTilePieceIcon(chessboard);
            }
            addMouseListener(new MouseListener() {
	        	@Override
	            public void mouseClicked(MouseEvent e) {
                    if (chessboard == null) {
                        JOptionPane.showMessageDialog(null, "Chessboard is not initialized. Please restart the game.");
                        return;
                    }
	                if(isRightMouseButtonClicked(e)) {
	                    highlightColor = Color.BLUE; // Set highlight color for right-clicK
	                    sourceTile = null;
	                	targetTile = null;
	                	selectedPiece = null;
	                }else if(isLeftMouseButtonClicked(e)) {
	                	//first click
	                	if(sourceTile == null) {
	                        highlightColor = Color.GREEN; // Set highlight color for left-click
	                        sourceTile = chessboard.getTile(tileId);
	                        selectedPiece = sourceTile.getPiece();
	                        if(selectedPiece== null) {
	                        	sourceTile = null;
	                        }else{
                                Collection<Move> testValidLegalMoves = chessboard.getCurrentPlayer().getPotentialLegalMoves();
                                if(selectedPiece.getPieceAlliance() == chessboard.getCurrentPlayer().getAlliance()) {
                                    if(selectedPiece instanceof Rook) {
                                    System.out.println("\nTesting Rook Moves:");
                                    RookTest.testRookMovesWithStandardBoard(testValidLegalMoves,selectedPiece.getPieceCoordinate());
                                    }else if(selectedPiece instanceof Knight) {
                                    System.out.println("\nTesting Knight Moves:");
                                    KnightTest.testKnightMovesWithStandardBoard(testValidLegalMoves,selectedPiece.getPieceCoordinate());
                                    }else if(selectedPiece instanceof Bishop) {
                                    System.out.println("\nTesting Bishop Moves:");
                                    BishopTest.testBishopMovesWithStandardBoard(testValidLegalMoves,selectedPiece.getPieceCoordinate());
                                    }else if(selectedPiece instanceof Queen) {
                                    System.out.println("\nTesting Queen Moves:");
                                    QueenTest.testQueenMovesWithStandardBoard(testValidLegalMoves,selectedPiece.getPieceCoordinate());
                                    }else if(selectedPiece instanceof King) {
                                    System.out.println("\nTesting King Moves:");
                                    KingTest.testKingMovesWithStandardBoard(testValidLegalMoves,selectedPiece.getPieceCoordinate());
                                    }else if(selectedPiece instanceof Pawn) {
                                    System.out.println("\nTesting Pawn Moves:");
                                    PawnTest.testPawnMovesWithStandardBoard(testValidLegalMoves,selectedPiece.getPieceCoordinate());
                                    }
                                }
                            }
	                    //second click
	                	}else {
	                        highlightColor = Color.YELLOW; // Set highlight color for left-click
	                        targetTile = chessboard.getTile(tileId);
	                        if(sourceTile!= null && targetTile!= null) {
	                           chessboard = gameController.executeMove(chessboard, ChessBoardUI.this);
	                        }
	                        sourceTile = null;
	                        targetTile = null;
	                        selectedPiece = null;
	                	}
	                    setBackground(highlightColor);
	                    repaint();
	                	
	                	SwingUtilities.invokeLater(new Runnable() {
	                		@Override
	                		public void run() {
	                			if(chessboard!= null) {
	                				boardPanel.drawBoard(chessboard);
                                    if(gameController.isCheckmate()) {
                                        JOptionPane.showMessageDialog(null, "Checkmate! " + chessboard.getOpponentPlayer().getAlliance().toString() + " wins.");
                                        System.exit(0);
                                    }
	                			}
                            }
	                	});
	                		
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

        public void drawTile(final Board board) {
        	if (board != null) {
	        	assignTileColor(tileId);
	        	assignTilePieceIcon(board);
	        	validate();
	        	repaint();
        	}
		}

		private void assignTileColor(final int tileId) {
            setBackground(BoardUtils.getCoordinateAlliance(tileId) == Alliance.WHITE? lightTileColor : darkTileColor );	
        }
        
        private void assignTilePieceIcon(final Board board) {
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
	                    ImageIcon icon = new ImageIcon(image.getScaledInstance(TILE_PANEL_DIMENSION.width, TILE_PANEL_DIMENSION.height, Image.SCALE_AREA_AVERAGING));
	                    add(new JLabel(icon));
	                }catch(IOException e) {
	                    e.printStackTrace();
	                }
	            }
        	}
        }
    }

    public void updateBoard(Board board) {
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

}