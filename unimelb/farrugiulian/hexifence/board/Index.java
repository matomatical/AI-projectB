package unimelb.farrugiulian.hexifence.board;

import aiproj.hexifence.Piece;

public abstract class Index implements Comparable{
	
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
	public int compareTo(Object o) {
		Index that = (Index)o;
		
		if(that.i != this.i){
			return that.i - this.i;
		} else {
			return that.j - this.j;
		}
	}
}
