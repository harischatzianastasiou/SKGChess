package com.chess.model;

public enum Alliance {
	WHITE{
		@Override
        public int getMovingDirection() {
            return -1; // White pieces are on the down side of the board and they move upwards 
        }
        @Override
        public boolean isWhite() {
            return true;
        }
        @Override
        public boolean isBlack() {
            return false;
        }
        @Override
        public Alliance getOpposite() {
            return BLACK;
        }
	},
	BLACK{
		@Override
        public int getMovingDirection() {
            return 1; // Black pieces are on the upper side of the board and they move downwards
        }
        @Override
        public boolean isWhite() {
            return false;
        }
        @Override
        public boolean isBlack() {
            return true;
        }
        @Override
        public Alliance getOpposite() {
            return WHITE;
        }
    };
	
	public abstract int getMovingDirection();
	public abstract boolean isWhite();
	public abstract boolean isBlack();
	public abstract Alliance getOpposite();

}


