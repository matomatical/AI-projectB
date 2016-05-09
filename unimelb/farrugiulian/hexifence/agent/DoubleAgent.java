package unimelb.farrugiulian.hexifence.agent;

import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.matomatical.util.QueueHashSet;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

public class DoubleAgent extends Agent{

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
		
		// update all potentially-affected edges
		
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
		// System.out.println(color + " has " + numShortChains() + " short chains left");
		if (freeScoring.size() > 0) {
			//System.out.println("Taking " + freeScoring.peek().i + "," + freeScoring.peek().j);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return freeScoring.remove();
		}
		
		// then select moves that are safe
		
		if(safe.size() > 0){
			return safe.remove();
		}
		
		if (locked == 0 && safe.size() == 0) {
			Stack<Edge> stack = new Stack<Edge>();
			consumeAll(stack);
			int numShortChains = numShortChains();
			while(!stack.isEmpty()){
				board.unplace(stack.pop());
			}
			if (numShortChains % 2 == 0) {
				System.out.println(color + " has " + numShortChains + " short chains left and should lose");
			} else {
				System.out.println(color + " has " + numShortChains + " short chains left and should win");
			}
			locked = 1;
		}
		
		if(scoring.size() > 0){
			Stack<Edge> stack = new Stack<Edge>();
			int capturable = consumeAll(stack);
			while(!stack.isEmpty()){
				board.unplace(stack.pop());
			}
			//System.out.println("Capturable cells: " + capturable);
			//System.out.println("Taking " + scoring.peek().i + "," + scoring.peek().j);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Double box
			if (capturable == 2) {
				Cell[] cells = scoring.peek().getCells();
				Cell cell;
				// Get the cell that has the edge that can double box
				if (cells[0].numFreeEdges() == 2) {
					cell = cells[0];
				} else {
					cell = cells[1];
				}
				Edge paritySwitch;
				Edge parityKeep;
				// Figure out which edge can double box
				if (cell.getFreeEdges()[0] == scoring.peek()){
					paritySwitch = cell.getFreeEdges()[1];
					parityKeep = cell.getFreeEdges()[0];
				} else {
					paritySwitch = cell.getFreeEdges()[0];
					parityKeep = cell.getFreeEdges()[1];
				}
				// If we need to switch parity then double box
				consumeAll(stack);
				Edge choice;
				int numShortChains = numShortChains();
				System.out.print(color + " has " + numShortChains + " short chains so " + color);
				if (numShortChains % 2 == 0) {
					System.out.println(" is double boxing");
					choice = paritySwitch;
				} else {
					System.out.println(" is not double boxing");
					choice = parityKeep;
				}
				while(!stack.isEmpty()){
					board.unplace(stack.pop());
				}
				return choice;
			}
			return scoring.remove();
		}
		
		// then and only then, select a move that will lead to a small sacrifice
		Edge[] edges = board.getFreeEdges();
		
		// all remaining edges represent possible sacrifices,
		// just find the best option (least damage)
		
		Edge bestEdge = edges[0];
		int bestCost = sacrificeSize(edges[0]);
		
		for(int i = 1; i < edges.length; i++){
			Edge edge = edges[i];
			int cost = sacrificeSize(edge);
			if(cost < bestCost || cost == 3 && isLoop(edge) && bestCost >= 3){
				bestEdge = edge;
				bestCost = cost;
			}
		}
		
		// If we are sacrificing a chain of length 2 we need to either secure or bait
		if (bestCost == 2) {
			bestEdge.place(super.piece);
			Edge secureEdge = bestEdge;
			Edge baitEdge = bestEdge;
			// If the current bestEdge is a secure edge
			if (bestEdge.getCells()[0].numFreeEdges() == 1 &&
					bestEdge.getCells().length == 2 &&
					bestEdge.getCells()[1].numFreeEdges() == 1) {
				// Fix up the bait edge
				baitEdge = bestEdge.getCells()[0].getFreeEdges()[0];
			// Otherwise the current bestEdge is a bait edge
			} else {
				// Fix up the secure edge
				if (bestEdge.getCells()[0].numFreeEdges() == 1) {
					secureEdge = bestEdge.getCells()[0].getFreeEdges()[0];
				} else {
					secureEdge = bestEdge.getCells()[1].getFreeEdges()[0];
				}
			}
			board.unplace(bestEdge);
			if (numShortChains() % 2 == 0) {
				bestEdge = baitEdge;
			} else {
				bestEdge = secureEdge;
			}
		}
		//String color = super.piece == Piece.BLUE ? "Blue" : "Red";
		System.out.println(color + " sacrificing chain of size " + bestCost);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return bestEdge;
	}
	
	// Works ONLY if the scoring QueueHashSet is accurate
	private int consumeAll(Stack<Edge> stack){
		int capturable = 0;
		for (Edge edge : scoring){
			if (edge.isEmpty()){
				edge.place(super.piece);
				stack.push(edge);
				capturable++;
				if (edge.getCells()[0].numFreeEdges() > 0){
					capturable += sacrifice(edge.getCells()[0], stack);
				}
				if (edge.getCells().length == 2 && edge.getCells()[1].numFreeEdges() > 0){
					capturable += sacrifice(edge.getCells()[1], stack);
				}
			}
		}
		return capturable;
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
		int bestCost = 10000;
		
		for(int i = 0; i < edges.length; i++){
			Edge edge = edges[i];
			// Only consider edges that don't score
			if (edge.getCells()[0].numFreeEdges() > 1 && (edge.getCells().length == 1
					|| edge.getCells()[1].numFreeEdges() > 1)){
				int cost = sacrificeSize(edge);
				if(cost < bestCost || cost == 3 && isLoop(edge) && bestCost >= 3){
					bestEdge = edge;
					bestCost = cost;
				}
			}
		}
		// If this chain is short, take it and return how many cells it had 
		if (bestCost < 3 || bestCost == 3 && isLoop(bestEdge)) {
			bestEdge.place(super.opponent);
			stack.push(bestEdge);
			//System.out.println(bestEdge.i + "," + bestEdge.j);
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
	
	private boolean isLoop(Edge edge) {
		Stack<Edge> stack = new Stack<Edge>();
		boolean isLoop = false;
		
		board.place(edge, super.opponent);
		stack.push(edge);
		
		for(Cell cell : edge.getCells()){
			if (cell.numFreeEdges() != 0){
				sacrifice(cell, stack);
			} else {
				isLoop = true;
			}
		}
		
		while(!stack.isEmpty()){
			board.unplace(stack.pop());
		}
		return isLoop;
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