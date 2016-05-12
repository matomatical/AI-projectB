package unimelb.farrugiulian.hexifence.agent.farrugiulian;

import java.util.Stack;

import com.matomatical.util.QueueHashSet;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.*;
import unimelb.farrugiulian.hexifence.board.features.FeatureSet;

public class MidgameExpert implements Expert {

	/** edges that represent cells free for the taking */
	private QueueHashSet<Edge> scoring;
	private QueueHashSet<Edge> safe;

	private Board board;
	private int piece;
	
	private long startTime;
	
	public MidgameExpert(Board board, int piece) {
	
		this.board = board;
		this.piece = piece;
		
		// TODO it'd be nice if we could use the same sets as the opening expert,
		// as they are already up to date! perhaps merge these two agents?
		scoring = new QueueHashSet<Edge>();
		safe = new QueueHashSet<Edge>();
		
		// must check each edge for safety / freeness
		
		for(Edge edge : board.getFreeEdges()){
			store(edge);
		}
	}
	
	private void store(Edge edge){
		
		// flag to check safety of both sides
		boolean safeEdge = true;
		
		for(Cell cell : edge.getCells()){
			
			int n = cell.numFreeEdges();
			
			if(n == 1){
				// this edge gives a free cell!
				scoring.add(edge);
				
				// nothing more to consider
				return;
				
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
	
	@Override
	public void update(Edge edge) {
		
		// remove this edge from any set it was in
		// (take advantage of short-circuit evaluation and empty block)
		if(  safe.remove(edge) || scoring.remove(edge)  ){}
		
		
		// track all potentially-affected edges
		for(Cell cell : edge.getCells()){
			
			int n = cell.numFreeEdges();
			
			if(n == 2){
				// these edges are no longer safe!
				for(Edge e : cell.getFreeEdges()){
					safe.remove(e);
				}
			
			} else if (n == 1){
				// these edges are no longer sacrifices, they're free!
				for(Edge e : cell.getFreeEdges()){
					scoring.add(e);
				}
			}
		}
	}
	
	private void rewind(Edge edge) {
		
		// this piece has been removed from the board,
		// we better update our collections!

		// but wait! this could affect other nearby edges too!
		
		for(Cell cell : edge.getCells()){
			
			int n = cell.numFreeEdges();
			
			if(n == 1){
				// this was the only edge, this cell's a freebie now
				scoring.add(edge);
				
			} else if(n == 2){
				// these edges are no longer free! they're
				// sacrifices now
				for(Edge e : cell.getFreeEdges()){
					scoring.remove(e);
				}
				
			} else if (n > 2){
				// these edges are now no longer sacrifices, they're all safe!
				for(Edge e : cell.getFreeEdges()){
					if (e.getOtherCell(cell) == null || e.getOtherCell(cell) != null && e.getOtherCell(cell).numFreeEdges() > 2) {
						safe.add(e);
					}
				}
			}
		}
	}


	@Override
	public Edge move() {
		
		// select moves that capture a cell, if they exist

		if(scoring.size() > 0){
			return scoring.remove();
		}
		
		startTime = System.currentTimeMillis();
		// if not, there are only safe edges, start a search for the best move
		SearchPair sp = minimax(piece);
		System.out.println("Expected value: " + sp.value);
		return sp.edge;
		
		// TODO: PERHAPS CONSIDER SACRIFICING TO SWITCH PARITY,
		//       IF ALL EVALUATIONS TURN OUT BAD?

		// if(sp.value >= 0){
		//	return sp.edge;
		// } else {
		//	// search sacrifices? 
		// }
		
		// TODO: TRY MONTE-CARLO TOO
	}

	private SearchPair minimax(int piece){
		
		// base case, no more searching
		if(cutoff()){
			return new SearchPair(null, evaluation((piece == this.piece)));
		}
		
		// otherwise, we have some searching to do!
		
		// try to maximise if we're paying OUR piece,
		// otherwise try to minimise
		
		boolean maxing = (piece == this.piece);
		
		// try all of the safe edges
		
		SearchPair best = new SearchPair(null, 0);
		
		// copy for safe iterating
		Edge[] edges = safe.toArray(new Edge[safe.size()]);
		
		for(Edge edge : edges){
			if (System.currentTimeMillis() - startTime > 5000) {
				System.out.println("Taking too long, returning");
				return new SearchPair(edge, maxing ? 0 : 1);
			}
			
			
			safe.remove(edge);
			
			int nextPiece = piece;
			if(edge.numCapturableCells() == 0){
				// swap pieces unless a cell is captured
				nextPiece = (piece == Piece.RED) ? Piece.BLUE : Piece.RED;	
			}
			
			// make this move
			edge.place(piece);
			this.update(edge);
			
			// recursive part
			SearchPair sp = minimax(nextPiece);
			sp.edge = edge;
			
			// compare this choice
			if(best.edge == null){
				best = sp;
			}
			if(maxing) {
				if(sp.value > best.value){
					best = sp;
				}
				if (best.value == 1) {
					edge.unplace();
					this.rewind(edge);
					return best;
				}
			} else {
				if(sp.value < best.value){
					best = sp;
				}
				if (best.value == 0) {
					edge.unplace();
					this.rewind(edge);
					return best;
				}
			}
				
			// unmake this move
			edge.unplace();
			this.rewind(edge);
		}
		
		// return the best option
		return best;
	}

//	private class Path {
//		public Stack<Edge> edges = new Stack<Edge>();
//		public final int value;
//		public Path(int value){
//			this.value = value;
//		}
//	}
	
	private class SearchPair {
		public Edge edge;
		public int value;
		SearchPair(Edge edge, int value){
			this.edge = edge;
			this.value = value;
		}
	}
	
	private int evaluation(){
		// try to approximate the winning / losing margin from this position
		int eval = 0;
		
		FeatureSet fs = new FeatureSet(this.board, this.piece);
		
		eval += fs.getMyScore();
		eval -= fs.getYourScore();
		
		// try to statically figure out how many pieces we can win from lockdown
		// (feel free to use something linear time here, creating the featureset
		// is already linear time)
		
		return eval;
	}
	
	private int evaluation(boolean max){
		return max ^ ((numShortChains() % 2) == 0) ? 1 : 0;
	}
	
	private boolean cutoff(){
		return safe.size() == 0; 
	}
	
	@Override
	public boolean transition() {
		return safe.size() == 0;
	}
	
	private int numShortChains() {
		int numShortChains = 0;
		Stack<Edge> stack = new Stack<Edge>();
		// Keep taking short chains while keeping count
		while(takeShortChain(stack) != 0) {
			numShortChains++;
		}
		if (board.getFreeEdges().length == 0) {
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
			bestEdge.place(Piece.BLUE);
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
		
		board.place(edge, Piece.BLUE);
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
					edge.place(Piece.BLUE);
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
		
		board.place(edge, Piece.BLUE);
		stack.push(edge);
		
		for(Cell cell : edge.getCells()){
			if (cell.numFreeEdges() != 0){
				isLoop |= hasDeadEnd(cell, stack);
			}
		}
		
		while(!stack.isEmpty()){
			board.unplace(stack.pop());
		}
		return isLoop;
	}
	
	private boolean hasDeadEnd(Cell cell, Stack<Edge> stack){
		
		int n = cell.numFreeEdges();
		
		if(n > 1){
			// this cell is not available for capture
			return false;
		} else if (n == 1){
			for(Edge edge : cell.getEdges()){
				if(edge.isEmpty()){
					
					// claim this piece
					edge.place(Piece.BLUE);
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
