package com.chess.model;

public enum Alliance {
	WHITE{
		@Override
        public int getMovingDirection() {
            return -1; // make clear that this is for pieces
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
            return 1; // make clear that this is for pieces
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


