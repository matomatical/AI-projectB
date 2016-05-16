/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.board;

import aiproj.hexifence.Piece;

/** This class captures the commonality between grid cells and edges which
 *  both have a color, an i and a j and both make up the board grid
 *  <br>
 *  Also privides useful helper functions toString, compareTo, and hashCode
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran   [juliant1]
 */
public abstract class Index implements Comparable<Edge> {
	
	public final int i, j;	// public immutable
	int color;				// package access
	
	/** Create a new empty Index **/
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
