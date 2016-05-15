package unimelb.farrugiulian.hexifence.agent;

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
	
	/** edges that represent cells free for the taking */
	private QueueHashSet<Edge> free;
	private QueueHashSet<Edge> safe;

	private final int transitionThreshold;
	
	/** Makes a new {@link OpeningExpert}
	 * @param board The board for this agent to watch
	 **/
	public OpeningExpert(Board board, int transitionThreshold){
		
		this.transitionThreshold = transitionThreshold;
		
		long seed = System.nanoTime();
		
		// shuffle the list of edges to result in random
		// removal order (linear time)
		List<Edge> edges = Arrays.asList(board.getEdges());
		Collections.shuffle(edges, new Random(seed));
		
		// the expert's collections
		
		if(board.numEmptyEdges() == board.numEdges()){
			
			// all edges are safe
			free = new QueueHashSet<Edge>();
			safe = new QueueHashSet<Edge>(edges);
			
		} else {
			
			free = new QueueHashSet<Edge>();
			safe = new QueueHashSet<Edge>();
			
			// must check each edge for safety / freeness
			
			for(Edge edge : edges){
				
				// flag to check safety of both sides
				boolean safeEdge = true;
				
				for(Cell cell : edge.getCells()){
					
					int n = cell.numEmptyEdges();
					
					if(n == 1){
						// this edge gives a free cell!
						free.add(edge);
						
						// nothing more to consider
						break;
						
					} else if(n == 2){
						// this edge is not safe on this side!
						safeEdge = false;
					}
				}
				
				if(safeEdge){
					// safe on both sides!
					safe.add(edge);
				}
				
			}
		}
	}
	
	@Override
	public void update(Edge edge) {
		
		// remove this edge from any set it was in
		// (take advantage of lazy evaluation here)
		if(safe.remove(edge) || free.remove(edge)){}
		
		
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
					free.add(e);
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
