package unimelb.farrugiulian.hexifence.board;

import aiproj.hexifence.Piece;

public abstract class Index {
	
	public final int i, j;	// public immutable
	int color;				// package access
	
	protected Index(int i, int j){
		this.i = i;
		this.j = j;
		
		color = Piece.EMPTY;
	}
	
	public int getColor(){
		return color;
	}
	
	public boolean isEmpty(){
		return color == Piece.EMPTY;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + i;
		result = prime * result + j;
		return result;
	}
}
