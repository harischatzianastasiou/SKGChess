package com.chess.gui;

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

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.GameHistory;
import com.chess.engine.board.Move;
import com.chess.engine.board.MoveResult;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Bishop;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Knight;
import com.chess.engine.pieces.Pawn;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Queen;
import com.chess.engine.pieces.Rook;
import com.chess.engine.pieces.test.BishopTest;
import com.chess.engine.pieces.test.KingTest;
import com.chess.engine.pieces.test.KnightTest;
import com.chess.engine.pieces.test.PawnTest;
import com.chess.engine.pieces.test.QueenTest;
import com.chess.engine.pieces.test.RookTest;

public class ChessTable {
    
    private final JFrame gameFrame;
    private final BoardPanel boardPanel;
    private Board chessboard;
    
    private Tile sourceTile;
    private Tile targetTile;
    private Piece selectedPiece;
    
    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private final static Dimension BOARD_PANEL_DIMENSION = new Dimension (400, 350);
    private final static Dimension TILE_PANEL_DIMENSION = new Dimension (70, 70); // Increased size
    private final static String pieceImagesPath = "images/";
    private final Color lightTileColor = Color.decode("#eeeed2");
    private final Color darkTileColor = Color.decode("#769656");
    
    public ChessTable() {
        this.gameFrame = new JFrame("SKGChess");
        this.gameFrame.setLayout(new BorderLayout());
        this.gameFrame.setJMenuBar(createChessTableMenuBar());
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.chessboard = Board.createStandardBoard();
        this.boardPanel = new BoardPanel();
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        this.gameFrame.setVisible(true);
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
	                        }
	                    	                    //second click
	                	}else {
	                        highlightColor = Color.YELLOW; // Set highlight color for left-click
	                        targetTile = chessboard.getTile(tileId);
	                        if(sourceTile!= null && targetTile!= null) {
                               Collection<Move> modifiedCollection = new ArrayList<>(chessboard.getCurrentPlayer().getLegalMoves());
                               Collection<Move> checkingMoves = new ArrayList<>();
                               MoveResult moveResult = null;
	                           for(Move move : chessboard.getCurrentPlayer().getLegalMoves()) {  
	                        	   moveResult = move.simulate();
	                        	   if(moveResult.getMoveStatus() == MoveResult.MoveStatus.ILLEGAL) {
                                       modifiedCollection.remove(move); // remove illegal move from current player's legal moves list.
                                   }
                                   if(moveResult.getMoveStatus() == MoveResult.MoveStatus.CHECKMATE) {
                                    checkingMoves.add(move); // add check move to current player's legal moves list.
                                }
	                           }
  
	                           if(selectedPiece.getPieceAlliance() == chessboard.getCurrentPlayer().getAlliance()) {
	                        		if(selectedPiece instanceof Rook) {
		                            System.out.println("\nTesting Rook Moves:");
		                            RookTest.testRookMovesWithStandardBoard(modifiedCollection,selectedPiece.getPieceCoordinate());
	                        		}else if(selectedPiece instanceof Knight) {
		                            System.out.println("\nTesting Knight Moves:");
		                            KnightTest.testKnightMovesWithStandardBoard(modifiedCollection,selectedPiece.getPieceCoordinate());
	                        		}else if(selectedPiece instanceof Bishop) {
		                            System.out.println("\nTesting Bishop Moves:");
		                            BishopTest.testBishopMovesWithStandardBoard(modifiedCollection,selectedPiece.getPieceCoordinate());
	                        		}else if(selectedPiece instanceof Queen) {
		                            System.out.println("\nTesting Queen Moves:");
		                            QueenTest.testQueenMovesWithStandardBoard(modifiedCollection,selectedPiece.getPieceCoordinate());
	                        		}else if(selectedPiece instanceof King) {
		                            System.out.println("\nTesting King Moves:");
		                            KingTest.testKingMovesWithStandardBoard(modifiedCollection,selectedPiece.getPieceCoordinate());
	                        		}else if(selectedPiece instanceof Pawn) {
		                            System.out.println("\nTesting Pawn Moves:");
		                            PawnTest.testPawnMovesWithStandardBoard(modifiedCollection,selectedPiece.getPieceCoordinate());
	                        		}
	                        	}
	                           for(Move move : modifiedCollection) {  
	                        	   if(move.getSourceCoordinate() == sourceTile.getTileCoordinate() && 
	                        	      move.getTargetCoordinate() == targetTile.getTileCoordinate()) {
                                       GameHistory.getInstance().addBoardState(chessboard);
                                       GameHistory.getInstance().addMove(move);
	                        		   chessboard = move.execute();
                                       if(checkingMoves.contains(move)) {
                                           JOptionPane.showMessageDialog(null, "CHECKMATE!");
                                       }
                                       break;
	                        	   }
                                }
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

}


//Optimization Strategies:
//1.
//Pruning Techniques: Implement techniques like alpha-beta pruning to reduce the number of moves that need to be evaluated, focusing only on the most promising ones.
//2.
//Incremental Updates: Instead of recalculating the entire board state from scratch, use incremental updates to adjust only the parts of the board affected by the move.
//3.
//Caching: Use caching mechanisms to store and reuse previously calculated board states and move evaluations, reducing redundant calculations.
//4.
//Parallel Processing: Leverage parallel processing to evaluate multiple moves simultaneously, taking advantage of multi-core processors.



/*
how to use the observer pattern
Tabnine
GPT-4o
To use the Observer pattern in your chess game, you can follow these steps. The Observer pattern is a behavioral design pattern that allows an object (the subject) to notify other objects (observers) about changes in its state. This pattern is useful for decoupling the state management from the objects that need to react to state changes.

Steps to Implement the Observer Pattern
1.
Define the Subject Interface: This interface will declare methods for attaching, detaching, and notifying observers.
2.
Create the Concrete Subject: This class will implement the subject interface and maintain a list of observers. It will notify observers of any state changes.
3.
Define the Observer Interface: This interface will declare the update method that observers must implement.
4.
Create Concrete Observers: These classes will implement the observer interface and define how they should react to updates from the subject.


Example Implementation
Let's implement a simple version of the Observer pattern for your chess game:

Step 1: Define the Subject Interface
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/observer/Subject.java
package com.chess.engine.observer;

public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers();
}
Insert
Copy
New file

Step 2: Create the Concrete Subject
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/observer/GameState.java
package com.chess.engine.observer;

import java.util.ArrayList;
import java.util.List;

public class GameState implements Subject {
    private final List<Observer> observers;
    private PiecesStatus piecesStatus;

    public GameState() {
        this.observers = new ArrayList<>();
        this.piecesStatus = new PiecesStatus();
    }

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(piecesStatus);
        }
    }

    public void updatePiecesStatus(PiecesStatus newStatus) {
        this.piecesStatus = newStatus;
        notifyObservers();
    }

    public PiecesStatus getPiecesStatus() {
        return piecesStatus;
    }
}
Insert
Copy
New file

Step 3: Define the Observer Interface
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/observer/Observer.java
package com.chess.engine.observer;

import com.chess.engine.board.PiecesStatus;

public interface Observer {
    void update(PiecesStatus piecesStatus);
}
Insert
Copy
New file

Step 4: Create Concrete Observers
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/pieces/King.java
package com.chess.engine.pieces;

import com.chess.engine.observer.Observer;
import com.chess.engine.board.PiecesStatus;

public class King extends Piece implements Observer {
    // Existing code...

    @Override
    public void update(PiecesStatus piecesStatus) {
        // React to changes in the PiecesStatus
        // For example, recalculate legal moves if necessary
    }
}
Insert
Copy
New file

Usage
Initialize GameState: Create an instance of GameState at the start of the game.
Attach Observers: Attach pieces like King to the GameState so they can be notified of changes.
Update State: When the game state changes (e.g., a move is made), update the PiecesStatus in GameState and notify observers.


This implementation allows your pieces to react to changes in the game state without being tightly coupled to the state management logic.
References (1)
1. 
King.java - 40-40
Copy

harischatzi
okay my example is that when for example a piece is pinning an opposite piece, the opposite piece should be aware of that.
Tabnine
GPT-4o
To implement a system where a piece is aware of being pinned by an opponent's piece, you can use the Observer pattern to notify pieces of changes in their status, such as being pinned. Here's how you can set this up:

Steps to Implement Awareness of Being Pinned
1.
Define the Subject Interface: This interface will declare methods for attaching, detaching, and notifying observers.
2.
Create the Concrete Subject: This class will implement the subject interface and maintain a list of observers. It will notify observers of any state changes, such as pinning.
3.
Define the Observer Interface: This interface will declare the update method that observers must implement.
4.
Create Concrete Observers: These classes will implement the observer interface and define how they should react to updates, such as being pinned.


Example Implementation
Step 1: Define the Subject Interface
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/observer/Subject.java
package com.chess.engine.observer;

public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers();
}
Insert
Copy
New file

Step 2: Create the Concrete Subject
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/observer/PiecesStatus.java
package com.chess.engine.observer;

import java.util.ArrayList;
import java.util.List;
import com.chess.engine.pieces.Piece;

public class PiecesStatus implements Subject {
    private final List<Observer> observers;
    private final List<Piece> pinnedPieces;

    public PiecesStatus() {
        this.observers = new ArrayList<>();
        this.pinnedPieces = new ArrayList<>();
    }

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(pinnedPieces);
        }
    }

    public void addPinnedPiece(Piece piece) {
        pinnedPieces.add(piece);
        notifyObservers();
    }

    public void removePinnedPiece(Piece piece) {
        pinnedPieces.remove(piece);
        notifyObservers();
    }
}
Insert
Copy
New file

Step 3: Define the Observer Interface
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/observer/Observer.java
package com.chess.engine.observer;

import java.util.List;
import com.chess.engine.pieces.Piece;

public interface Observer {
    void update(List<Piece> pinnedPieces);
}
Insert
Copy
New file

Step 4: Create Concrete Observers
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/pieces/Piece.java
package com.chess.engine.pieces;

import com.chess.engine.observer.Observer;
import java.util.List;

public abstract class Piece implements Observer {
    // Existing code...

    private boolean isPinned;

    @Override
    public void update(List<Piece> pinnedPieces) {
        this.isPinned = pinnedPieces.contains(this);
        // React to being pinned, e.g., restrict movement
    }

    public boolean isPinned() {
        return isPinned;
    }
}
Insert
Copy
New file

Usage
Initialize PiecesStatus: Create an instance of PiecesStatus at the start of the game.
Attach Observers: Attach pieces to the PiecesStatus so they can be notified of changes.
Update State: When a piece pins another, update the PiecesStatus to reflect this, and notify observers.


This setup allows pieces to be aware of their pinned status and react accordingly, such as restricting their movement. The Observer pattern helps decouple the pinning logic from the individual piece behavior, promoting a cleaner design.
References (7)
1. 
King.java - 40-40
2. 
BishopTest.java - 15-50
3. 
RookTest.java - 19-54
4. 
Rook.java - 30-40
Show more
Copy

harischatzi
my idea is to use piecesstatus as singletton and store there everytime the pinned , pinning, checking etc pieces. is there a pattern to better do this? 
Tabnine
GPT-4o
Using a singleton pattern for managing the PiecesStatus can be a viable approach if you want to ensure that there is only one instance of PiecesStatus throughout the application. However, there are some considerations and alternative patterns you might want to explore:

Considerations for Singleton Pattern
1.
Global State:
Singletons can introduce global state into your application, which can make testing and debugging more challenging. It can also lead to tight coupling between components.
2.
Thread Safety:
If your application is multi-threaded, you need to ensure that the singleton instance is thread-safe.
3.
Flexibility:
Singletons can reduce flexibility, as they enforce a single instance. This can be problematic if you later decide you need multiple instances (e.g., for different game sessions).


Alternative Patterns
1.
Service Locator:
This pattern provides a centralized registry for services (like PiecesStatus). It allows for more flexibility than a singleton, as you can swap out implementations if needed.
2.
Dependency Injection:
Use a dependency injection framework to manage the lifecycle of PiecesStatus. This approach provides more flexibility and testability, as you can easily mock or replace dependencies.
3.
Event-Driven Architecture:
Instead of a singleton, use an event-driven approach where changes in piece status are published as events. Other components can subscribe to these events and react accordingly.


Implementing Singleton for PiecesStatus
If you decide to proceed with the singleton pattern, here's how you can implement it:

File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/board/PiecesStatus.java
package com.chess.engine.board;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.chess.engine.pieces.Piece;

public class PiecesStatus {
    private static PiecesStatus instance;
    private List<Piece> checkingPieces;
    private Map<Integer, Piece> pinningPieces;
    private Map<Integer, Piece> pinnedPieces;
    private Map<Integer, Piece> protectorPieces;
    private Map<Integer, Piece> protectedPieces;
    private Map<Integer, Piece> blockingQueenSideCastlingPieces;
    private Map<Integer, Piece> blockingKingSideCastlingPieces;

    private PiecesStatus() {
        this.checkingPieces = new ArrayList<>();
        this.pinningPieces = new HashMap<>();
        this.pinnedPieces = new HashMap<>();
        this.protectorPieces = new HashMap<>();
        this.protectedPieces = new HashMap<>();
        this.blockingQueenSideCastlingPieces = new HashMap<>();
        this.blockingKingSideCastlingPieces = new HashMap<>();
    }

    public static synchronized PiecesStatus getInstance() {
        if (instance == null) {
            instance = new PiecesStatus();
        }
        return instance;
    }

    // Methods to manage pieces status
    public void addCheckingPiece(Piece piece) {
        this.checkingPieces.add(piece);
    }

    public List<Piece> getCheckingPieces() {
        return checkingPieces;
    }

    // Add similar methods for pinning, pinned, protector, etc.
}
Insert
Copy
New file

Conclusion
While the singleton pattern can be useful for ensuring a single instance of PiecesStatus, consider the trade-offs in terms of flexibility and testability. Depending on your application's complexity and requirements, alternative patterns like dependency injection or event-driven architecture might offer more benefits.
References (11)
1. 
King.java - 40-40
2. 
PiecesStatus.java - 18-26
3. 
PiecesStatus.java - 35-37
4. 
PiecesStatus.java - 75-77
Show more
Copy

harischatzi
present a demo for this 
Event-Driven Architecture: 
in detail
Tabnine
GPT-4o
To implement an Event-Driven Architecture (EDA) in your chess game, you can follow these steps. EDA is a design pattern in which components communicate by emitting and listening to events. This approach decouples the components, making the system more flexible and scalable.

Steps to Implement Event-Driven Architecture
1.
Define Event Classes: Create classes for different types of events, such as PiecePinnedEvent, PieceUnpinnedEvent, PieceCheckedEvent, etc.
2.
Create an Event Bus: Implement a central event bus that allows components to publish and subscribe to events.
3.
Define Event Listeners: Create interfaces or abstract classes for event listeners that define how components should react to specific events.
4.
Implement Event Publishers: Modify components to publish events when certain actions occur, such as a piece being pinned.
5.
Implement Event Subscribers: Modify components to subscribe to relevant events and react accordingly.


Example Implementation
Step 1: Define Event Classes
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/events/PiecePinnedEvent.java
package com.chess.engine.events;

import com.chess.engine.pieces.Piece;

public class PiecePinnedEvent {
    private final Piece pinnedPiece;

    public PiecePinnedEvent(Piece pinnedPiece) {
        this.pinnedPiece = pinnedPiece;
    }

    public Piece getPinnedPiece() {
        return pinnedPiece;
    }
}
Insert
Copy
New file

Step 2: Create an Event Bus
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/events/EventBus.java
package com.chess.engine.events;

import java.util.ArrayList;
import java.util.List;

public class EventBus {
    private final List<EventListener> listeners = new ArrayList<>();

    public void register(EventListener listener) {
        listeners.add(listener);
    }

    public void unregister(EventListener listener) {
        listeners.remove(listener);
    }

    public void publish(Object event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
Insert
Copy
New file

Step 3: Define Event Listeners
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/events/EventListener.java
package com.chess.engine.events;

public interface EventListener {
    void onEvent(Object event);
}
Insert
Copy
New file

Step 4: Implement Event Publishers
Modify the component that detects pinning to publish a PiecePinnedEvent.

File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/board/Board.java
package com.chess.engine.board;

import com.chess.engine.events.EventBus;
import com.chess.engine.events.PiecePinnedEvent;
import com.chess.engine.pieces.Piece;

public class Board {
    private final EventBus eventBus;

    public Board(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void detectPinning(Piece piece) {
        // Logic to detect pinning
        boolean isPinned = true; // Example condition
        if (isPinned) {
            eventBus.publish(new PiecePinnedEvent(piece));
        }
    }
}
Insert
Copy
New file

Step 5: Implement Event Subscribers
File: C:/Users/tchatzia/OneDrive - Netcompany/Files/Notes/Languages/backend/Java/eclipse-workspace/SKGChess/src/com/chess/engine/pieces/Piece.java
package com.chess.engine.pieces;

import com.chess.engine.events.EventListener;
import com.chess.engine.events.PiecePinnedEvent;

public abstract class Piece implements EventListener {
    private boolean isPinned;

    @Override
    public void onEvent(Object event) {
        if (event instanceof PiecePinnedEvent) {
            PiecePinnedEvent pinnedEvent = (PiecePinnedEvent) event;
            if (pinnedEvent.getPinnedPiece() == this) {
                this.isPinned = true;
                // React to being pinned, e.g., restrict movement
            }
        }
    }

    public boolean isPinned() {
        return isPinned;
    }
}
Insert
Copy
New file

Usage
Initialize EventBus: Create an instance of EventBus at the start of the game.
Register Listeners: Register pieces or other components as listeners to the EventBus.
Publish Events: When a piece is pinned, publish a PiecePinnedEvent using the EventBus.
React to Events: Pieces react to events by implementing the onEvent method.


This setup allows your chess game to use an event-driven approach, where components are loosely coupled and communicate through events. This makes the system more flexible and easier to maintain.
References (3)
1. 
King.java - 40-40
2. 
SKGChessGUI.java - 112-114
3. 
SKGChessGUI.java - 12-115
Copy

*/

/*1. Approach 1: Each current player must take as instance variable the opponent moves so that it can calculate which of those make some of his moves illegal.
2. Approach 2: As I did, each player calculates his moves, independently of the opponent moves. Then on the list of those moves I can simulate each move which will give me a way to identify the moves that are not legal due to some rules( for example as far as King is taken into consideration, King knows that he can go to square a5. But because opponent targets a5, I calculate it in the simulated boad and disqualify this move.

Which approach is better?
Tabnine
GPT-4o
Both approaches have their pros and cons, and the choice between them depends on your specific requirements and constraints. Here's a breakdown of each approach:

Approach 1: Pre-compute Legal Moves Considering Opponent's Moves
Pros:
Immediate Feedback: Players can see all legal moves upfront, which improves the user experience by providing immediate feedback.
Efficiency in Gameplay: By pre-computing legal moves, you avoid the need to check legality during each move attempt, which can make the game feel more responsive.
Simplified Move Execution: Since illegal moves are filtered out beforehand, the move execution logic can be simpler and more straightforward.


Cons:
Complexity in Calculation: Calculating legal moves while considering opponent moves can be complex, especially for advanced rules like pins and checks.
Performance Overhead: Pre-computing all possible moves and their legality can be computationally expensive, especially in complex board states.


Approach 2: Calculate Moves Independently and Validate on Execution
Pros:
Simplicity in Initial Calculation: Calculating moves without considering the opponent's moves can be simpler and faster initially.
Flexibility: This approach can be more flexible, as it allows for dynamic checks and adjustments based on the current game state.


Cons:
Delayed Feedback: Players might attempt moves that are later found to be illegal, which can disrupt the flow of the game.
Increased Complexity in Execution: The move execution logic needs to handle legality checks dynamically, which can lead to more complex and error-prone code.


Recommendation
If your primary goal is to provide a smooth and responsive user experience, Approach 1 is generally preferable. It allows players to see all legal moves upfront and avoids the frustration of having moves rejected after they are attempted. However, it requires careful implementation to efficiently calculate and update legal moves.

If you are more concerned with flexibility and simplicity in initial calculations, and you can manage the complexity of dynamic legality checks, Approach 2 might be suitable. This approach can be beneficial if you plan to implement complex rules or if performance is not a critical concern.

Ultimately, the best approach depends on your specific requirements, such as performance constraints, user experience priorities, and the complexity of the rules you need to implement.
References (1)
1. 
ChessTable.java - 177-189
Copy

harischatzi
New idea! By having the approach 2 I think I can just read all current player legal moves and all opponent player legal moves. Then I can call the simulate() function and check which of those are not allowed due to opponent legal moves. For those that are not allowed I remove them from the current player legal moves. Only then I give control to the user to click a square. What about this?
Tabnine
GPT-4o
Your new idea is a hybrid approach that combines elements of both approaches you previously described. Here's how it works and its potential benefits and drawbacks:

Hybrid Approach: Pre-filter Legal Moves Using Simulation
Process:
1.
Calculate Initial Legal Moves: Calculate the current player's legal moves without considering the opponent's moves.
2.
Simulate and Filter: For each move in the current player's legal moves, simulate the move and check against the opponent's legal moves to determine if it results in an illegal state (e.g., putting the king in check).
3.
Update Legal Moves: Remove any moves that are not allowed due to the opponent's legal moves.
4.
User Interaction: Allow the player to select from the filtered list of legal moves.


Pros:
Balanced Complexity: This approach balances the complexity of pre-computing all legal moves with the flexibility of dynamic checks.
Improved User Experience: By filtering out illegal moves before user interaction, you provide a smoother experience with immediate feedback on available moves.
Efficiency: You avoid the need to check legality during each move attempt, which can improve performance during gameplay.


Cons:
Initial Overhead: There is still some computational overhead in simulating and filtering moves before user interaction, which might be noticeable in complex board states.
Complexity in Implementation: Implementing the simulation and filtering logic requires careful handling to ensure accuracy and efficiency.


Recommendation
This hybrid approach can be effective if you want to provide a responsive user experience while managing the complexity of move legality checks. It allows you to pre-filter moves based on the current game state, reducing the likelihood of user frustration from attempting illegal moves.

To implement this approach, ensure that your simulation logic accurately reflects the game rules and efficiently handles the filtering process. This will help maintain performance and provide a seamless experience for the player.
*/