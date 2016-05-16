/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                   Last Modified 16/05/16 by Julian Tran                   *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.board.features;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.matomatical.util.QueueHashSet;

import unimelb.farrugiulian.hexifence.board.*;

/** Collection of Edges sorted based on edge classifications:
 * <ul>
 * <li> free -	edges that capture a cell with no consequences to the
 * 				capturability of neighbouring cells
 * </li>
 * <li> capturing -	edges that capture a cell and in doing so, also make
 * 				a neighbouring cell captureable
 * </li>
 * <li> safe -	edges that do not capture a cell, or make any of their
 * 				neighbouring cells captureable
 * </li>
 * <li> sacrificing -	edges that do not capture a cell but do make
 * 				neighbouring cells captureable (sacrificing those cells)
 * </li>
 * </ul>
 * 
 * The set supports linear-time creation and constant-time updating, rewinding
 * peeking/removing. Enjoy!
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran   [juliant1]
 **/
public class EdgeSet {
	
	/** Set of edges that capture a cell with no consequences to the
	 *  capturability of neighbouring cells
	 **/
	private QueueHashSet<Edge> free = new QueueHashSet<Edge>();

	/** Set of edges that capture a cell and in doing so, also make
	 *  a neighbouring cell captureable
	 **/
	private QueueHashSet<Edge> capturing = new QueueHashSet<Edge>();
	
	/** Set of edges that do not capture a cell, or make any of their
	 *  neighbouring cells captureable
	 **/
	private QueueHashSet<Edge> safe = new QueueHashSet<Edge>();
	
	/** Set of edges that do not capture a cell but do make neighbouring cells
	 *  captureable (sacrificing those cells)
	 **/
	private QueueHashSet<Edge> sacrificing = new QueueHashSet<Edge>();
	
	/** Create a new EdgeSet from a board. Randomises the order edges are
	 *  inserted, causing removal order to be effectively randomised
	 *  (because removal order is actually FIFO)
	 * @param a board to use to make this EdgeSet
	 **/
	public EdgeSet(Board board, boolean shuffling){
		
		List<Edge> edges = Arrays.asList(board.getEdges());
		
		if(shuffling){
			// shuffle the list of edges to result in random removal order
			long seed = System.nanoTime();
			
			System.err.println("Edgeset seed:" + seed);
			Collections.shuffle(edges, new Random(seed));			
		}
		
		if(board.numEmptyEdges() == board.numEdges()){
			// the board is empty!
			// on an empty board, all edges are safe
			safe = new QueueHashSet<Edge>(edges);
		
		} else {
			// the board is not empty!
			// now we have to classify every edge ourselves...
			for(Edge edge : edges){
				// these edges may already be taken but reclassify will just
				// skip them if that's the case!
				reclassify(edge);
			}
		}
	}
	
	/** Update the feature set in response to the placing of an edge
	 * @param edge The edge that has been placed
	 **/
	public void update(Edge edge) {
		
		// remove this edge from any set it was in, it is no longer empty
		
		remove(edge);
		
		// track all potentially-affected edges, ensuring they are in the
		// right collection
		
		for(Cell cell : edge.getCells()){
			for(Edge neighbour : cell.getEmptyEdges()){
				reclassify(neighbour);
			}
		}
	}
	
	/** Update the feature set in response to the un-placing of an edge
	 * @param edge The edge that has been un-placed
	 **/
	public void rewind(Edge edge) {

		// re-add the edge and reclassify potentially-affected edges, ensuring
		// they are in the right collection
		
		for(Cell cell : edge.getCells()){
			for(Edge e : cell.getEmptyEdges()){
				reclassify(e);	
			}
		}
	}

	/** Remove this edge from all collections it may or may not be in
	 * @param edge the edge to remove
	 **/
	private void remove(Edge edge){
		safe.remove(edge);
		free.remove(edge);
		capturing.remove(edge);
		sacrificing.remove(edge);	
	}
	
	/** Remove this edge from all collections it may or may not be in
	 * @param edge the edge to remove
	 **/
	private void reclassify(Edge edge){
		
		// remove this edge from any set it was in, so that we can find the
		// right place for it
		this.remove(edge);
		
		if (! edge.isEmpty()){
			// this edge is no longer free! don't put it in ANY collection
			return;
			
		} else if (edge.numCapturableCells() == 0){
			// decide between safe and sacrificing
			
			for(Cell cell : edge.getCells()){
				if(cell.numEmptyEdges() == 2){
					// this edge is NOT safe, it's sacrificing!
					sacrificing.add(edge);
					return;
				}
			}
			
			// if we make it through that loop, it's not sacrificial on
			// either side, we can add it to the safe set!
			safe.add(edge);
		
		} else {
			// decide between capturing and free
			
			// is it a double freebie?
			if(edge.numCapturableCells() == 2){
				// definitely free!
				free.add(edge);
				return;
			}
			
			// otherwise, it could be either, let's check out the
			// non-captureable cell
			
			for(Cell cell : edge.getCells()){
				if(cell.numEmptyEdges() == 1){
					// this is the cell we are looking for (captureable)
					
					Cell other = edge.getOtherCell(cell);
					
					if(other != null && other.numEmptyEdges() == 2){
						// part of a chain, this capture has consequences!
						capturing.add(edge);
					} else {
						// side of the baord or not part of the chain, freee!
						free.add(edge);
					}
					
					// either way, we're done here
					return;
				}
			}
		}
	}
	
	/** @return true iff this edge set contains safe edges **/
	public boolean hasSafeEdges(){
		return safe.size() > 0;
	}
	/** @return the number of remaining safe edges in this edge set **/
	public int numSafeEdges(){
		return safe.size();
	}
	/** @return the first safe edge in the collection (doesn't remove) **/
	public Edge getSafeEdge(){
		return safe.element();
	}
	/** @return THE QueueHashSet storing safe edges (NOT a copy) **/
	public QueueHashSet<Edge> getSafeEdges(){
		return safe;
	}
	
	/** @return true iff this edge set contains free edges **/
	public boolean hasFreeEdges(){
		return free.size() > 0;
	}
	/** @return the number of remaining free edges in this edge set **/
	public int numFreeEdges(){
		return free.size();
	}
	/** @return the first free edge in the collection (doesn't remove) **/
	public Edge getFreeEdge(){
		return free.element();
	}
	/** @return THE QueueHashSet storing free edges (NOT a copy) **/
	public QueueHashSet<Edge> getFreeEdges(){
		return free;
	}
	
	/** @return true iff this edge set contains sacrificing edges **/
	public boolean hasSacrificingEdges(){
		return sacrificing.size() > 0;
	}
	/** @return the number of remaining sacrificing edges in this edge set **/
	public int numSacrificingEdges(){
		return sacrificing.size();
	}
	/** @return the first sacrificing edge in the collection (won't remove) **/
	public Edge getSacrificingEdge(){
		return sacrificing.element();
	}
	/** @return THE QueueHashSet storing sacrificing edges (NOT a copy) **/
	public QueueHashSet<Edge> getSacrificingEdges(){
		return sacrificing;
	}
	
	/** @return true iff this edge set contains capturing edges **/
	public boolean hasCapturingEdges(){
		return capturing.size() > 0;
	}
	/** @return the number of remaining capturing edges in this edge set **/
	public int numCapturingEdges(){
		return capturing.size();
	}
	/** @return the first capturing edge in the collection (doesn't remove) **/
	public Edge getCapturingEdge(){
		return capturing.element();
	}
	/** @return THE QueueHashSet storing capturing edges (NOT a copy) **/
	public QueueHashSet<Edge> getCapturingEdges(){
		return capturing;
	}
}
