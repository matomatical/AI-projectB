package unimelb.farrugiulian.hexifence.board.features;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.matomatical.util.QueueHashSet;

import unimelb.farrugiulian.hexifence.board.*;

public class EdgeSet {

	private QueueHashSet<Edge> free; // (capturing with no consequences)
	private QueueHashSet<Edge> safe; // (non-capturing with no consequences)
	
	private QueueHashSet<Edge> capturing;
	private QueueHashSet<Edge> sacrificing;
	
	/** Create a new EdgeSet from an <b>empty</b> board
	 * If the board privided is not empty, this constructor will fail to
	 * produce a correct EdgeSet
	 * @param an <b>empty</b> board to use to make this EdgeSet
	 **/
	public EdgeSet(Board board){
		
		// shuffle the list of edges to result in random removal order
		long seed = System.nanoTime();
		List<Edge> edges = Arrays.asList(board.getEdges());
		Collections.shuffle(edges, new Random(seed));
		
		// on an empty board, all edges are safe
		safe = new QueueHashSet<Edge>(edges);
		free = new QueueHashSet<Edge>();
		capturing = new QueueHashSet<Edge>();
		sacrificing = new QueueHashSet<Edge>();
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

	private void remove(Edge edge){
		safe.remove(edge);
		free.remove(edge);
		capturing.remove(edge);
		sacrificing.remove(edge);	
	}
	
	private void reclassify(Edge edge){
		
		// remove this edge from any set it was in, so that we can find the
		// right place for it
		this.remove(edge);
		
		if (! edge.isEmpty()){
			// this edge is no longer free! don't put it in any collection
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
			
			// otherwise, it could be either, let's check out 
			
			for(Cell cell : edge.getCells()){
				if(cell.numEmptyEdges() == 1){
					// this is the cell we are looking for
					
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public boolean hasSafeEdges(){
		return safe.size() > 0;
	}
	public int numSafeEdges(){
		return safe.size();
	}
	public Edge getSafeEdge(){
		return safe.element();
	}
	public QueueHashSet<Edge> getSafeEdges(){
		return safe;
	}
	
	public boolean hasFreeEdges(){
		return free.size() > 0;
	}
	public int numFreeEdges(){
		return free.size();
	}
	public Edge getFreeEdge(){
		return free.element();
	}
	public QueueHashSet<Edge> getFreeEdges(){
		return free;
	}
	
	public boolean hasSacrificingEdges(){
		return sacrificing.size() > 0;
	}
	public int numSacrificingEdges(){
		return sacrificing.size();
	}
	public Edge getSacrificingEdge(){
		return sacrificing.element();
	}
	public QueueHashSet<Edge> getSacrificingEdges(){
		return sacrificing;
	}
	
	public boolean hasCapturingEdges(){
		return capturing.size() > 0;
	}
	public int numCapturingEdges(){
		return capturing.size();
	}
	public Edge getCapturingEdge(){
		return capturing.element();
	}
	public QueueHashSet<Edge> getCapturingEdges(){
		return capturing;
	}
}
