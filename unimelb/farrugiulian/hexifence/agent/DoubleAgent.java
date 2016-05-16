/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                   Last Modified 24/04/16 by Julian Tran                   *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.agent;

import java.util.List;
import java.util.Stack;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import com.matomatical.util.QueueHashSet;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

/** This agent plays the Game of Hexifence with random capturing or safe moves
 *  up to the point where sacrifices must be made, where it begins to attempt
 *  to remain in control using a double-boxing strategy using a simple parity
 *  based evaluation function.
 *  
 *  This strategy works well if there are big chains and loops sacrificing on
 *  the board which make sacrificing short chains worth it to get the long
 *  chains at the end (more suitable for large board sizes) and also the parity
 *  evaluation is not perfect as there are situations where the order in which
 *  chains are sacrificed can effect the overall parity count and change who
 *  ends up in control.
 * 
 * @author Julain Tran   [juliant1]
 * @author Matt Farrugia [farrugiam]
 **/
public class DoubleAgent extends Agent {
	
	/** HashSet for storing Edges **/
	private QueueHashSet<Edge> freeScoring, scoring, safe, sacr;
	
	private int locked = 0;
	
	@Override
	public int init(int n, int p){
		int r = super.init(n, p);
	
		List<Edge> edges = Arrays.asList(board.getEdges());
		Collections.shuffle(edges);
		
		freeScoring = new QueueHashSet<Edge>();
		scoring = new QueueHashSet<Edge>();
		safe = new QueueHashSet<Edge>(edges);
		sacr = new QueueHashSet<Edge>(); // um how is this gonna work
		
		return r;
	}
	
	@Override
	public void update(Edge edge) {
		
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
			
			int n = cell.numEmptyEdges();
			
			if(n == 2){
				for(Edge e : cell.getEmptyEdges()){
					// these edges are no longer safe!
					if(safe.remove(e)){
						sacr.add(e);
					} else if (freeScoring.remove(e)) {
						// And if they were a free scoring edge, now they result in another
						// scoring edge
						scoring.add(e);
					}
				}
			} else if (n == 1){
				// these edges are no longer sacrifices, they're free!
				Edge e = cell.getEmptyEdges()[0];
				if(sacr.remove(e)){
					// But what type of free? It depends on whether the other cell is
					// a sacrifice or not
					if (e.getOtherCell(cell) != null
							&& e.getOtherCell(cell).numEmptyEdges() == 2) {
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
	
	private float midgameMinimax(QueueHashSet<Edge> safe, boolean max) {
		if (safe.size() == 0) {
			// Need to replace this with another minimax that works on dfs results
			return max ^ ((numShortChains() % 2) == 0) ? 1 : 0;
		}
		
		float bestValue;
		Iterator<Edge> iterator = safe.iterator();
		if (max) {
			bestValue = Float.NEGATIVE_INFINITY;
			while (iterator.hasNext()) {
				QueueHashSet<Edge> safeTmp = new QueueHashSet<Edge>();
				safeTmp.addAll(safe);
				Edge edge = iterator.next();
				edge.place(super.piece);
				safeTmp.remove(edge);
				for(Cell cell : edge.getCells()){
					if(cell.numEmptyEdges() == 2){
						for(Edge e : cell.getEmptyEdges()){
							// these edges are no longer safe!
							safeTmp.remove(e);
						}
					}
				}
				bestValue = Math.max(bestValue, midgameMinimax(safeTmp, false));
				board.unplace(edge);
				if (bestValue == 1) {
					return 1;
				}
			}
		} else {
			bestValue = Float.POSITIVE_INFINITY;
			while (iterator.hasNext()) {
				QueueHashSet<Edge> safeTmp = new QueueHashSet<Edge>();
				safeTmp.addAll(safe);
				Edge edge = iterator.next();
				edge.place(super.piece);
				safeTmp.remove(edge);
				for(Cell cell : edge.getCells()){
					if(cell.numEmptyEdges() == 2){
						for(Edge e : cell.getEmptyEdges()){
							// these edges are no longer safe!
							safeTmp.remove(e);
						}
					}
				}
				bestValue = Math.min(bestValue, midgameMinimax(safeTmp, true));
				board.unplace(edge);
				if (bestValue == 0) {
					return 0;
				}
			}
		}
		
		return bestValue;
	}
	
	@Override
	public Edge getChoice(){
		// Free scoring cells are always safe to take
		String color = super.piece == Piece.BLUE ? "Blue" : "Red";
		// System.out.println(color + " has " + numShortChains() + " short chains left");
		if (freeScoring.size() > 0) {
			/*System.out.println("Taking " + freeScoring.peek().i + "," + freeScoring.peek().j);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			return freeScoring.remove();
		}
		
		// then select moves that are safe
		
		if(safe.size() > 0){
			if (safe.size() < 20) {
				Edge bestEdge = null;
				float bestValue;
				float testValue;
				Iterator<Edge> iterator = safe.iterator();
				bestValue = Float.NEGATIVE_INFINITY;
				while (iterator.hasNext()) {
					QueueHashSet<Edge> safeTmp = new QueueHashSet<Edge>();
					safeTmp.addAll(safe);
					Edge edge = iterator.next();
					edge.place(super.piece);
					safeTmp.remove(edge);
					for(Cell cell : edge.getCells()){
						if(cell.numEmptyEdges() == 2){
							for(Edge e : cell.getEmptyEdges()){
								// these edges are no longer safe!
								safeTmp.remove(e);
							}
						}
					}
					testValue = midgameMinimax(safeTmp, false);
					board.unplace(edge);
					if (testValue > bestValue) {
						bestValue = testValue;
						bestEdge = edge;
					}
					if (bestValue == 1) {
						System.out.println("Expected value: " + bestValue);
						return bestEdge;
					}
				}
				System.out.println("Expected value: " + bestValue);
				return bestEdge;
			}
			return safe.remove();
		}
		
		if (locked == 0) {
			Stack<Edge> stack = new Stack<Edge>();
			consumeAll(stack);
			int numShortChains = numShortChains();
			while(!stack.isEmpty()){
				board.unplace(stack.pop());
			}
			if (numShortChains % 2 == 0) {
				System.out.println(color + " has " + numShortChains
						+ " short chains left and should lose");
			} else {
				System.out.println(color + " has " + numShortChains
						+ " short chains left and should win");
			}
			locked = 1;
		}
		
		if(scoring.size() > 0){
			Stack<Edge> stack = new Stack<Edge>();
			int capturable = consumeAll(stack);
			while(!stack.isEmpty()){
				board.unplace(stack.pop());
			}
			/*System.out.println("Capturable cells: " + capturable);
			System.out.println("Taking "+scoring.peek());
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
			// Double box
			boolean isLoop = isLoop(scoring.peek());
			if (capturable == 2 || capturable == 4 && isLoop(scoring.peek())) {
				Cell[] cells = scoring.peek().getCells();
				Cell cell;
				// Get the cell that has the edge that can double box
				if (cells[0].numEmptyEdges() == 2) {
					cell = cells[0];
				} else {
					cell = cells[1];
				}
				Edge paritySwitch;
				Edge parityKeep;
				// Figure out which edge can double box
				if (cell.getEmptyEdges()[0] == scoring.peek()){
					paritySwitch = cell.getEmptyEdges()[1];
					parityKeep = cell.getEmptyEdges()[0];
				} else {
					paritySwitch = cell.getEmptyEdges()[0];
					parityKeep = cell.getEmptyEdges()[1];
				}
				// If we need to switch parity then double box
				consumeAll(stack);
				Edge choice;
				if (numShortChains() % 2 == 0) {
					System.out.print(color + " is double boxing a ");
					choice = paritySwitch;
				} else {
					System.out.print(color + " is not double boxing a ");
					choice = parityKeep;
				}
				if (isLoop) {
					System.out.println("loop");
				} else {
					System.out.println("chain");
				}
				while(!stack.isEmpty()){
					board.unplace(stack.pop());
				}
				return choice;
			}
			
			// Logic for in case the enemy does not take a big sacrifice, and 
			// offers it back at us. It just prioritises taking the smallest
			// dead end.
			Edge bestEdge = null;
			for (Edge edge : scoring) {
				isLoop = false;
				int captureSize = 10000;
				if ((isLoop(edge) || !isLoop ) && sacrificeSize(edge) < captureSize) {
					bestEdge = edge;
					captureSize = sacrificeSize(edge);
					isLoop = isLoop(edge);
				}
			}
			return bestEdge;
		}
		
		// then and only then, select a move that will lead to a small
		// sacrifice
		Edge[] edges = board.getEmptyEdges();
		
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
		
		// If we are sacrificing a chain of length 2 we need to either secure
		// or bait
		if (bestCost == 2) {
			bestEdge.place(super.piece);
			Edge secureEdge = bestEdge;
			Edge baitEdge = bestEdge;
			// If the current bestEdge is a secure edge
			if (bestEdge.getCells()[0].numEmptyEdges() == 1 &&
					bestEdge.getCells().length == 2 &&
					bestEdge.getCells()[1].numEmptyEdges() == 1) {
				// Fix up the bait edge
				baitEdge = bestEdge.getCells()[0].getEmptyEdges()[0];
			// Otherwise the current bestEdge is a bait edge
			} else {
				// Fix up the secure edge
				if (bestEdge.getCells()[0].numEmptyEdges() == 1) {
					secureEdge = bestEdge.getCells()[0].getEmptyEdges()[0];
				} else {
					secureEdge = bestEdge.getCells()[1].getEmptyEdges()[0];
				}
			}
			board.unplace(bestEdge);
			if (numShortChains() % 2 == 0) {
				bestEdge = baitEdge;
			} else {
				bestEdge = secureEdge;
			}
		}
		System.out.println(color + " sacrificing chain of size " + bestCost);
		/*try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		return bestEdge;
	}
	
	// Works ONLY if the scoring QueueHashSet is accurate
	private int consumeAll(Stack<Edge> stack){
		int capturable = 0;
		for (Edge edge : scoring){
			if (edge.isEmpty()){
				edge.place(super.opponent);
				stack.push(edge);
				capturable++;
				if (edge.getCells()[0].numEmptyEdges() > 0){
					capturable += sacrifice(edge.getCells()[0], stack);
				}
				if (edge.getCells().length == 2
						&& edge.getCells()[1].numEmptyEdges() > 0){
					capturable += sacrifice(edge.getCells()[1], stack);
				}
			}
		}
		return capturable;
	}
	
	private int numShortChains() {
		int numShortChains = 0;
		Stack<Edge> stack = new Stack<Edge>();
		// Keep taking short chains while keeping count
		while(takeShortChain(stack) != 0) {
			numShortChains++;
		}
		if (board.getEmptyEdges().length == 0) {
			numShortChains++;
		}
		// Undo all moves made while testing
		while(!stack.isEmpty()){
			board.unplace(stack.pop());
		}
		return numShortChains;
	}
	
	private int takeShortChain(Stack<Edge> stack) {
		Edge[] edges = board.getEmptyEdges();
		
		if (edges.length == 0) {
			return 0;
		}
		
		// Find the edge that sacrifices the least number of cells
		Edge bestEdge = null;
		int bestCost = 10000;
		
		for(int i = 0; i < edges.length; i++){
			Edge edge = edges[i];
			// Only consider edges that don't score
			if (edge.getCells()[0].numEmptyEdges() > 1
					&& (edge.getCells().length == 1
					|| edge.getCells()[1].numEmptyEdges() > 1)){
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
			if (cell.numEmptyEdges() != 0){
				size += sacrifice(cell, stack);
			}
		}
		
		while(!stack.isEmpty()){
			board.unplace(stack.pop());
		}
		
		return size;
	}

	private int sacrifice(Cell cell, Stack<Edge> stack){
		
		int n = cell.numEmptyEdges();
		
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
	
	private boolean isLoop(Edge edge) {
		Stack<Edge> stack = new Stack<Edge>();
		boolean isLoop = false;
		
		board.place(edge, super.opponent);
		stack.push(edge);
		
		for(Cell cell : edge.getCells()){
			if (cell.numEmptyEdges() != 0){
				isLoop |= hasDeadEnd(cell, stack);
			}
		}
		
		while(!stack.isEmpty()){
			board.unplace(stack.pop());
		}
		return isLoop;
	}
	
	private boolean hasDeadEnd(Cell cell, Stack<Edge> stack){
		
		int n = cell.numEmptyEdges();
		
		if(n > 1){
			// this cell is not available for capture
			return false;
		} else if (n == 1){
			for(Edge edge : cell.getEdges()){
				if(edge.isEmpty()){
					
					// claim this piece
					edge.place(super.opponent);
					stack.push(edge);
					
					// follow opposite cell
					Cell other = edge.getOtherCell(cell);
					
					if(other == null){
						return false;
					}
					
					return hasDeadEnd(other, stack);
				}	
			}
		}
		
		// n == 0, which means this cell is a dead end for the taking
		return true;
	}
}