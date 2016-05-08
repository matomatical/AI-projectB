package unimelb.farrugiulian.hexifence.agent;

import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.matomatical.util.QueueHashSet;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

public class AgentGreedy extends Agent{

	private Random rng = new Random(System.nanoTime());
	
	private QueueHashSet<Edge> freeScoring;
	private QueueHashSet<Edge> scoring;
	private QueueHashSet<Edge> safe;
	private HashMap<Edge, Integer> sacr;
	
	private int locked = 0;
	
	@Override
	public int init(int n, int p){
		int r = super.init(n, p);
	
		List<Edge> edges = Arrays.asList(board.getEdges());
		Collections.shuffle(edges);
		
		freeScoring = new QueueHashSet<Edge>();
		scoring = new QueueHashSet<Edge>();
		safe = new QueueHashSet<Edge>(edges);
		sacr = new HashMap<Edge, Integer>(); // um how is this gonna work
		
		return r;
	}
	
	@Override
	protected void notify(Edge edge) {
		
		// remove this edge from play
		if (!freeScoring.remove(edge)) {
			if(!scoring.remove(edge)) {
				if(!safe.remove(edge)) {
					sacr.remove(edge);
				}
			}
		}
		
		// upate all potentially-affected edges
		
		for(Cell cell : edge.getCells()){
			
			int n = cell.numFreeEdges();
			
			if(n == 2){
				for(Edge e : cell.getFreeEdges()){
					// these edges are no longer safe!
					if(safe.remove(e)){
						sacr.put(e, sacrificeSize(e));
					} else if (freeScoring.remove(e)) {
						// And if they were a free scoring edge, now they result in another
						// scoring edge
						scoring.add(e);
					}
				}
			
			} else if (n == 1){
				// these edges are no longer sacrifices, they're free!
				Edge e = cell.getFreeEdges()[0];
				if(sacr.remove(e) != null){
					// But what type of free? It depends on whether the other cell is
					// a sacrifice or not
					if (e.getOtherCell(cell) != null
							&& e.getOtherCell(cell).numFreeEdges() == 2) {
						scoring.add(e);
					} else {
						freeScoring.add(e);
					}
				} else if (scoring.remove(e)) {
					freeScoring.add(e);
				}
			}
		}
	}
	
	@Override
	public Edge getChoice(){
		// Free scoring cells are always safe to take
		String color = super.piece == Piece.BLUE ? "Blue" : "Red";
		System.out.println(color + " has " + numShortChains() + " short chains left");
		if (freeScoring.size() > 0) {
			/*System.out.println("Taking " + freeScoring.peek().i + "," + freeScoring.peek().j);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			return freeScoring.remove();
		}
		
		if(scoring.size() > 0){
			/*System.out.println("Taking " + scoring.peek().i + "," + scoring.peek().j);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			return scoring.remove();
		}
		
		// then select moves that are safe
		
		if(safe.size() > 0){
			return safe.remove();
		}
		
		/*if (locked == 0 && safe.size() == 0) {
			String color = super.piece == Piece.BLUE ? "Blue" : "Red";
			System.out.println(color + " has " + numShortChains() + " short chains left");
			locked = 1;
		}*/
		
			// then and only then, select a move that will lead to a small sacrifice
		
		Edge[] edges = board.getFreeEdges();
		
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
		//String color = super.piece == Piece.BLUE ? "Blue" : "Red";
		System.out.println(color + " sacrificing chain of size " + bestCost + ": " + bestEdge.i + "," + bestEdge.j);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bestEdge;
	}
	
	private int numShortChains() {
		int numShortChains = 0;
		if (safe.size() > 0){
			return -1; // Can't count short chains until board is locked
		}
		Stack<Edge> stack = new Stack<Edge>();
		// Keep taking short chains while keeping count
		while(takeShortChain(stack) != 0) {
			numShortChains++;
		}
		// Undo all moves made while testing
		while(!stack.isEmpty()){
			board.unplace(stack.pop());
		}
		return numShortChains;
	}
	
	private int takeShortChain(Stack<Edge> stack) {
		Edge[] edges = board.getFreeEdges();
		
		if (edges.length == 0) {
			return 0;
		}
		
		// Find the edge that sacrifices the least number of cells
		Edge bestEdge = null;
		int bestCost = 100000;
		
		for(int i = 0; i < edges.length; i++){
			Edge edge = edges[i];
			// Only consider edges that don't score
			if (edge.getCells()[0].numFreeEdges() > 1 && (edge.getCells().length == 1
					|| edge.getCells()[1].numFreeEdges() > 1)){
				int cost = sacrificeSize(edge);
				if(cost < bestCost){
					bestEdge = edge;
					bestCost = cost;
				}
			}
		}
		// If this chain is short, take it and return how many cells it had 
		if (bestCost < 3) {
			bestEdge.place(super.opponent);
			stack.push(bestEdge);
			System.out.println(bestEdge.i + "," + bestEdge.j);
			for(Cell cell : bestEdge.getCells()){
				sacrifice(cell, stack);
			}
			return bestCost;
		}
		return 0; // No short chains left
	}

	private int sacrificeSize(Edge edge) {
		int size = 0;
		Stack<Edge> stack = new Stack<Edge>();
		
		board.place(edge, super.opponent);
		stack.push(edge);
		
		for(Cell cell : edge.getCells()){
			if (cell.numFreeEdges() != 0){
				size += sacrifice(cell, stack);
			}
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


// old linear time move selection

//// select cells that are free!
//
//for(Edge edge : edges){
//	if(edge.numCapturableCells()>0){
//		return edge;
//	}
//}
//
//// then select moves that wont sacrifice a cell
//
//int offset = rng.nextInt(edges.length);
//
//for(int i = 0; i < edges.length; i++){
//	Edge edge = edges[(i + offset) % edges.length];
//	
//	boolean safe = true;
//	Cell[] cells = edge.getCells();
//	for(Cell cell : cells){
//		if(cell.numFreeEdges() == 2){
//			// this cell is not safe to capture around
//			safe = false;
//		}
//	}
//	if(safe){
//		return edge;
//	}
//}

//first select moves that will capture a cell

//		if(free.size() > 0){
//			return free.remove();
//		}
//		
//		// then select moves that are safe
//		
//		if(safe.size() > 0){
//			return safe.remove();
//		}