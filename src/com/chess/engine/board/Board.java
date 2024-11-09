package com.chess.engine.board;

import java.util.HashMap;
import java.util.Map;

import com.chess.engine.Alliance;
import com.chess.engine.board.BoardUtils;
import com.google.common.collect.ImmutableMap;

public class Board {
	
	private Board(Builder builder) {
		
	}
	
	private Map<Integer, Tile> ALL_TILES = new HashMap<> (BoardUtils.createAllPossibleEmptyTiles());

	public Tile getTile(final int tileCoordinate) {
        return ALL_TILES.get(tileCoordinate);
    }
	
	public  Map<Integer, Tile> getAllTiles() {
		return this.ALL_TILES;
	}
	
	public static class Builder{//Set mutable fields in Builder and once we call build(), we get an immutable Board object.
		public Board build() {
			return new Board(this);
		}
	}
	
}


/*
The Builder class in this Board class is an implementation of the Builder pattern, which is discussed in detail in Joshua Bloch's "Effective Java" book (Item 2 in the 3rd edition: "Consider a builder when faced with many constructor parameters").

Here's how the Builder pattern helps in this context:

1.
Handling Complex Object Creation: The Board class likely has many components (tiles, pieces, game state, etc.). Using a Builder can simplify the process of creating a Board object with many parameters.
2.
Flexibility: It allows for creating different configurations of the Board without needing multiple constructors or a single constructor with many parameters.
3.
Readability: When using the Builder pattern, the code for creating a Board object becomes more readable, as each setter method in the Builder can be named descriptively.
4.
Immutability: The Builder pattern can help in creating immutable Board objects. Once the Board is built, its state cannot be changed.
5.
Step-by-step Construction: It allows for step-by-step construction of the Board, which can be useful if the board setup process is complex or involves multiple steps.
6.
Default Values: The Builder can set default values for certain parameters, making it easier to create a Board with only the necessary custom settings.
7.
Validation: The Builder can perform validation on the parameters before actually creating the Board object.
*/