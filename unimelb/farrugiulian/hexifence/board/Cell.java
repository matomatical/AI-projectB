/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.board;

/** This Cell class represents a hexifence board cell, surrounded by 6 edges
 *  within a game grid. It contains methods for accessing its information and
 *  that of its neighbouring edges and cells
 * 
 *  @author Matt Farrugia [farrugiam]
 *  @author Julian Tran   [juliant1]
 **/
public class Cell extends Index {
	
	/** Nearby edges **/
	private Edge[] edges = new Edge[6];
	private int nedges = 0;

	/** Create a new, unlinked, empty cell at this position on the grid **/
	public Cell(int i, int j){
		super(i,j);
	}
	
	/** Link this cell to an edge (part of setting up a cell properly) **/
	public void link(Edge edge) {
		edges[nedges++] = edge;
	}

	/** Count and return the number of bordering edges still set to empty **/
	public int numEmptyEdges(){
		int n = 0;
		
		// loop and count edges that are still empty
		for(Edge edge : edges){
			if(edge.isEmpty()){
				n++;
			}
		}
		return n;
	}
	
	/** Get THE array used to store edges in this cell (not a copy) **/
	public Edge[] getEdges() {
		return edges;
	}
	
	/** Get an array of bordering edges which are still empty **/
	public Edge[] getEmptyEdges(){
		
		// make an array with the correct size
		Edge[] edges = new Edge[this.numEmptyEdges()];
		int n = 0;
		
		// now populate the array with any empty edges
		for(Edge edge : this.edges){
			if(edge.isEmpty()){
				edges[n++] = edge;
			}
		}
		
		// all done!
		return edges;
	}
}
