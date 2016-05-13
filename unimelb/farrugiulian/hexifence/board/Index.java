package unimelb.farrugiulian.hexifence.board;

import aiproj.hexifence.Piece;

public abstract class Index implements Comparable<Edge> {
	
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
	
	@Override
	public String toString(){
		return "(" + i + ", " + j + ")";
	}
	
	@Override
	public int compareTo(Edge that) {
		
		if(this.i < that.i){
			// this is less
			return -1;
		
		} else if(this.i > that.i){
			// this is more
			return 1;
			
		} else {
			// i's ar equal, it's down to the j's
			// return negative if this is less, etc
			return this.j - that.j; 
		}
		
	}
}
