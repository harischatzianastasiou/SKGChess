package com.chess.engine.board;

import java.util.Map;
import java.util.HashMap;

import com.chess.engine.Alliance;
import com.chess.engine.board.Tile.EmptyTile;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableMap;

//Factory design pattern to create instances of tiles
// :TODO --> 1. Note down everything as practices.

//immutable class
public abstract class Tile {                                                                       // we cannot instantiate abstract classes directly, but we can create instances of their subclasses.

	protected final int tileCoordinate;
	protected final Alliance tileAlliance;															// protected so that it can only be accessed by subclasses - final so that once the tileCoordinate is instantiated via the constructor its value cannot be changed! This is suggested in Effective Java for immutability objects.
	
	private Tile(int tileCoordinate,  final Alliance tileAlliance) {
		this.tileCoordinate = tileCoordinate;
		this.tileAlliance = tileAlliance;
		
	}
	
	// factory method, used for a single point of creation for tiles
	public static Tile createTile(final int tileCoordinate,final Alliance alliance,final Piece piece) {
		return piece != null? new OccupiedTile(tileCoordinate, alliance, piece) : new EmptyTile(tileCoordinate,alliance);
	}
	
	public abstract boolean isTileOccupied();
	
	public abstract int getTileCoordinate();
	
	public abstract Alliance getTileAlliance();
	
	public abstract Piece getPiece();
	
	public static final class EmptyTile extends Tile  { 
		
		private EmptyTile(final int coordinate, final Alliance alliance) {
			super(coordinate, alliance);
		}
		
		@Override
		public boolean isTileOccupied() {
            return false;
        }
		
		@Override
		public int getTileCoordinate() {
            return this.tileCoordinate;
        }
		
		@Override
		public Alliance getTileAlliance() {
            return this.tileAlliance;
        }	
		
		@Override
        public Piece getPiece() {
            return null;
        }
	}
	
	public static final class OccupiedTile extends Tile {
		
		private final Piece pieceOnTile;
		
        private OccupiedTile(int coordinate, Alliance alliance, Piece piece) {//private or public, since the constructor of Tile is private then it cannot be instatiated from outside the Tile class.
            super(coordinate, alliance);
            this.pieceOnTile = piece;
        }
        
        @Override
        public boolean isTileOccupied() {
            return true;
        }
        
		@Override
		public int getTileCoordinate() {
            return this.tileCoordinate;
        }
        
        @Override
        public Alliance getTileAlliance() {
            return this.tileAlliance;
        }
        
        @Override
        public Piece getPiece() {
            return this.pieceOnTile;
        }
	}
}
//A.Why Nested?? --> 

		/*
		 * Would it make difference if these 2 classes where on separate classes
		
		Yes, it would make a difference if the EmptyTile and OccupiedTile classes were placed in separate files or folders. Here are the key differences and considerations:
		
		1. **Encapsulation and Cohesion**
		Nested Classes: When EmptyTile and OccupiedTile are nested within the Tile class, they are tightly encapsulated within the context of Tile. This makes it clear that these classes are specific implementations of a Tile and are closely related to the Tile class.
		Separate Files: If these classes are moved to separate files, they become top-level classes. This can make the relationship between Tile, EmptyTile, and OccupiedTile less explicit. You would need to ensure that the relationship is well-documented and understood.
		
		
		2. **Access Control**
		Nested Classes: As nested classes, EmptyTile and OccupiedTile can access the private members of the outer Tile class if needed. This can be useful for tightly coupled logic.
		Separate Files: As top-level classes, they would not have direct access to the private members of Tile. You would need to use public or protected methods to expose any necessary functionality.
		
		
		3. **Code Organization and Readability**
		Nested Classes: Keeping EmptyTile and OccupiedTile within Tile can make the code more readable and easier to understand, as all related logic is in one place.
		Separate Files: Moving them to separate files can make the codebase more modular and easier to navigate, especially in larger projects. Each class would have its own file, making it easier to manage and modify independently.
		
		
		4. **Compilation and Dependency Management**
		Nested Classes: Nested classes are compiled into the same .class file as the outer class. This can simplify dependency management.
		Separate Files: Separate files mean separate .class files, which can be beneficial for incremental compilation and modular builds
		
/*B. DESIGN PATTERN HERE
 * 
		 * The design pattern used in this code is the Factory Method Pattern. Here's how it is applied:

				1.
				Factory Method: The createTile method in the Tile class acts as a factory method. It provides a single point of creation for Tile objects, determining whether to return an instance of EmptyTile or OccupiedTile based on the presence of a Piece.
				2.
				Encapsulation of Object Creation: The factory method encapsulates the logic for creating Tile objects, allowing the client code to request a Tile without needing to know the details of which specific subclass is being instantiated.
				3.
				Abstraction: The Tile class is abstract, and the specific implementations (EmptyTile and OccupiedTile) are hidden from the client. The client interacts with the Tile interface, and the factory method handles the instantiation of the appropriate subclass.
				4.
				Reuse and Caching: The EMPTY_TILES map is used to cache instances of EmptyTile, demonstrating a common practice in factory patterns to reuse objects and improve performance.
				
				
				Overall, the Factory Method Pattern is used here to provide a flexible and centralized way to create Tile objects, while hiding the complexity of the instantiation process from the client.
						 */

/*C. FINAL PARAMETER IN CONSTRUCTOR

In Java, marking a parameter as final in a constructor (or any method) means that the parameter cannot be reassigned within the body of that constructor or method. This is a common practice for several reasons:

1.
Immutability: By marking the parameter as final, you ensure that its value remains constant throughout the execution of the constructor. This can help prevent accidental reassignment and maintain the immutability of the object being constructed.
2.
Readability and Intent: Using final can make the code more readable and convey the intent that the parameter is not meant to be changed. It signals to other developers that the parameter should be treated as a constant within the method.
3.
Error Prevention: It helps prevent errors that might occur if the parameter is accidentally reassigned, which could lead to unexpected behavior or bugs.

*/