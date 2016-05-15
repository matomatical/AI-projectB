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
	
	public void update(Edge edge) {
		
		// remove this edge from any set it was in, it is no longer empty
		safe.remove(edge);
		free.remove(edge);
		capturing.remove(edge);
		sacrificing.remove(edge);
		
		// track all potentially-affected edges
		for(Cell cell : edge.getCells()){
			
			int n = cell.numEmptyEdges();
			
			if(n == 2){
				// these edges are no longer safe!
				for(Edge e : cell.getEmptyEdges()){
					safe.remove(e);
				}
			
			} else if (n == 1){
				// these edges are no longer sacrifices, they're free!
				for(Edge e : cell.getEmptyEdges()){
					capturing.add(e);
				}
			}
		}
	}
	
	public void rewind(Edge edge) {
		
		// this piece has been removed from the board,
		// we better update our collections!

		// but wait! this could affect other nearby edges too!
		
		for(Cell cell : edge.getCells()){
			
			int n = cell.numEmptyEdges();
			
			if(n == 1){
				// this was the only edge, this cell's a freebie now
				capturing.add(edge);
				
			} else if(n == 2){
				// these edges are no longer free! they're
				// sacrifices now
				for(Edge e : cell.getEmptyEdges()){
					capturing.remove(e);
				}
				
			} else if (n > 2){
				// these edges are now no longer sacrifices, they're all safe! TODO 79 characters
				for(Edge e : cell.getEmptyEdges()){
					if (e.getOtherCell(cell) == null || e.getOtherCell(cell) != null && e.getOtherCell(cell).numEmptyEdges() > 2) {
						safe.add(e);
					}
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
