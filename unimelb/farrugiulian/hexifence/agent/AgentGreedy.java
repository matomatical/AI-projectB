package unimelb.farrugiulian.hexifence.agent;

import java.util.Random;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Arrays;

import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

public class AgentGreedy extends Agent{

	private Random rng;

	private TreeSet<Edge> free;
	private TreeSet<Edge> safe;
	private TreeSet<Edge> sacr;
	
	public AgentGreedy(){
		long seed = System.nanoTime();
		rng = new Random(seed);
		System.err.println("AgentGreedy seed: "+seed);
	}
	
	@Override
	public int init(int n, int p){
		int r = super.init(n, p);
	
		free = new TreeSet<Edge>();
		safe = new TreeSet<Edge>(Arrays.asList(board.getEdges()));
		sacr = new TreeSet<Edge>();
		
		return r;
	}
	
	@Override
	protected void notify(Edge edge) {
		
		boolean b = free.remove(edge) || safe.remove(edge) || sacr.remove(edge);
		
		for(Cell cell : edge.getCells()){
			int n = cell.numFreeEdges();
			if(n == 2){
				// these edges are no longer safe!
				for(Edge e : cell.getFreeEdges()){
					if(safe.remove(e)){
						sacr.add(e);
					}
				}
			} else if (n == 1){
				// these edges are no longer sacrifices, they're free!
				for(Edge e : cell.getFreeEdges()){
					if(sacr.remove(e)){
						free.add(e);
					}
				}
			}
		}
	}
	
	@Override
	public Edge getChoice(){
		
		// first select moves that will capture a cell
		
		if(free.size() > 0){
			Edge e = free.pollFirst();
			return e;
		}
		
		// then select moves that are safe
		
		if(safe.size() > 0){
			Edge e = safe.pollFirst();
			return e;
		}

		// then and only then, select a move that will lead to a small sacrifice
		
//		
		Edge[] edges = board.getFreeEdges();
//		
//		// select cells that are free!
//		
//		for(Edge edge : edges){
//			if(edge.numCapturableCells()>0){
//				return edge;
//			}
//		}
//		
//		// then select moves that wont sacrifice a cell
//		
//		int offset = rng.nextInt(edges.length);
//		
//		for(int i = 0; i < edges.length; i++){
//			Edge edge = edges[(i + offset) % edges.length];
//			
//			boolean safe = true;
//			Cell[] cells = edge.getCells();
//			for(Cell cell : cells){
//				if(cell.numFreeEdges() == 2){
//					// this cell is not safe to capture around
//					safe = false;
//				}
//			}
//			if(safe){
//				return edge;
//			}
//		}
		
		// all remaining edges represent possible sacrifices,
		// just find the best option (least damage)
		
		Edge bestEdge = edges[0];
		int bestCost = sacrificeSize(edges[0]);
		
		for(int i = 1; i < edges.length; i++){
			Edge edge = edges[i];
			int cost = sacrificeSize(edge);
			if(cost < bestCost){
				bestEdge = edge;
				bestCost = cost;
			}
		}
		
		return bestEdge;
	}

	private int sacrificeSize(Edge edge) {
		int size = 0;
		Stack<Edge> stack = new Stack<Edge>();
		
		board.place(edge, super.opponent);
		stack.push(edge);
		
		for(Cell cell : edge.getCells()){
			size += sacrifice(cell, stack);
		}
		
		while(!stack.isEmpty()){
			board.unplace(stack.pop());
		}
		
		return size;
	}

	private int sacrifice(Cell cell, Stack<Edge> stack){
		
		int n = cell.numFreeEdges();
		
		if(n > 1){
			// this cell is not available for capture
			return 0;
			
		} else if (n == 1){
			for(Edge edge : cell.getEdges()){
				if(edge.isEmpty()){
					
					// claim this piece
					edge.place(super.opponent);
					stack.push(edge);
					
					// follow opposite cell
					Cell other = edge.getOtherCell(cell);
					
					if(other == null){
						return 1;
					}
					
					return 1 + sacrifice(other, stack);
				}	
			}
		}
		
		// n == 0, which means this cell is a dead end for the taking
		return 1;
	}
}