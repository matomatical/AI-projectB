package unimelb.farrugiulian.hexifence.agent.farrugiulian;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.matomatical.util.QueueHashSet;

import unimelb.farrugiulian.hexifence.board.*;

/** This agent will return moves using its move() method
 *  based on the following rules:
 * 
 *  Select any move that captures a cell, if one exists.
 *  Otherwise, select a random, safe move (one that will
 *  not let the opponent capture a cell), if one exists.
 *  Otherwise, return null.
 *  
 *  Constructing the expert is a linear time operation,
 *  while updating it and retreiving moves are O(1)
 **/
public class OpeningExpert implements Expert {
	
	private QueueHashSet<Edge> free;
	private QueueHashSet<Edge> safe;
	private QueueHashSet<Edge> dont;

	private final int transitionThreshold;
	
	/** Makes a new {@link OpeningExpert}
	 * (This constructor assumes the board is still empty,
	 *  that is, all edges are available and safe to pick)
	 *  
	 * @param board An <b>empty</b> board
	 **/
	public OpeningExpert(Board board, int n){
		
		this.transitionThreshold = n;
		
		long seed = System.nanoTime();
		
		// shuffle the list of edges to result in random
		// removal order (linear time)
		List<Edge> edges = Arrays.asList(board.getEdges());
		Collections.shuffle(edges, new Random(seed));
		
		// the expert's collections
		free = new QueueHashSet<Edge>();
		safe = new QueueHashSet<Edge>(edges);
		dont = new QueueHashSet<Edge>();
	}
	
	@Override
	public void update(Edge edge) {
		
		// remove this edge from any set it is in
		if(!safe.remove(edge)) {
			if(!free.remove(edge)) {
				dont.remove(edge);
			}
		}
		
		// track all potentially-affected edges
		for(Cell cell : edge.getCells()){
			
			int n = cell.numFreeEdges();
			
			if(n == 2){
				// these edges are no longer safe!
				for(Edge e : cell.getFreeEdges()){
					if(safe.remove(e)){
						dont.add(e);
					}
				}
			
			} else if (n == 1){
				// these edges are no longer sacrifices, they're free!
				for(Edge e : cell.getFreeEdges()){
					if(dont.remove(e)){
						free.add(e);
					}
				}
			}
		}
	}

	@Override
	public Edge move() {

		// select moves that capture a cell, if they exist

		if(free.size() > 0){
			return free.remove();
		}
		
		// then, select moves that are safe
		
		if(safe.size() > 0){
			return safe.remove();
		}
		
		// if there are no such moves, we cannot make a decision,
		// it's time to hand over to the next expert
		
		return null;
	}

	@Override
	public boolean transition() {
		
		return (safe.size() < this.transitionThreshold);
	}
}
