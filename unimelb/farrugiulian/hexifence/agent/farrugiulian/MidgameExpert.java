package unimelb.farrugiulian.hexifence.agent.farrugiulian;

import java.util.Stack;

import com.matomatical.util.QueueHashSet;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.*;
import unimelb.farrugiulian.hexifence.board.features.FeatureSet;

public class MidgameExpert implements Expert {

	/** variables for keeping track of time during search */
	private static long TIMEOUT = 5000;
	private long startTime;
	
	/** edges that represent cells free for the taking */
	private QueueHashSet<Edge> scoring;
	private QueueHashSet<Edge> safe;

	private Board board;
	private int piece;
	
	public MidgameExpert(Board board, int piece) {
	
		this.board = board;
		this.piece = piece;
		
		// TODO it'd be nice if we could use the same sets as the opening expert,
		// as they are already up to date! perhaps merge these two agents?
		// AHA! lets refactor this into an 'EdgeSet' that contains all these methods we're using :)
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
				// these edges are now no longer sacrifices, they're all safe! TODO 79 characters
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
		try {
			SearchPair sp = minimax(null, piece);
			System.err.println("Search complete! Expected winner: " + Board.name(sp.piece));
			return sp.edge;
		} catch(TimeoutException e){
			
			System.err.println("Aborting search due to timeout! "
					+ "playing last hopeful piece " + e.edge.toString());
			return e.edge;
		}
		
		
		
		// TODO: PERHAPS CONSIDER SACRIFICING TO SWITCH PARITY,
		//       IF ALL EVALUATIONS TURN OUT BAD?

		// if(sp.value >= 0){
		//	return sp.edge;
		// } else {
		//	// search sacrifices? 
		// }
		
		// TODO: TRY MONTE-CARLO TOO
	}

	
	// alright let's rethink our search
	
	/*
	minimax(last edge played, piece to play):
	
		// base cases
		if cutoff:
			return (null, winning piece) // really all we care about is who wins

		if timeout:
			throw timeout exception?
		
		result = null
		
		// recursive case
		for all edges => last edge played // (for all edges: if edge < last edge played: continue, ...)
			
			play edge
			
			pair = minimax(edge, (captured) ? piece : other piece)
			
			unplay edge

			if pair == null:
				// no pieces down this path! skip this edge
				continue
			
			pair.edge = this
			if pair.piece == piece
				return pair
			else // (pair.piece == other piece)
				// this move is not winning, continue searching
				result = pair
			
		// if we make it out of this loop, no pieces were winning for this player
		// (either there were no smaller edges or there were no winning edges)
		
		return result
		
		// (null if there were no possibilities)
	 */
	
	private SearchPair minimax(Edge last, int piece) throws TimeoutException{
		
		// keep track of time
		if (System.currentTimeMillis() - startTime > TIMEOUT) {
			System.out.println("Taking too long, returning");
			// return new SearchPair(edge, maxing ? 0 : 1);
			throw new TimeoutException(last);
		}
		
		// base case, are we at lockdown?
		if(cutoff()){
			return new SearchPair(null, winner(piece));
		}
		
		// otherwise, we still have some searching to do!
		
		// for each edge greater than this edge,
		// who is the winning player if we play that edge?

		SearchPair result = null;
		
		for(Edge edge : safe.toArray(new Edge[safe.size()])){
			if(last != null && last.compareTo(edge) <= 0){
				continue;
			}
			
			// play edge
			edge.place(piece);
			this.update(edge);
			
			// recursively search for result
			SearchPair pair = minimax(edge, Board.other(piece));
			// piece will swap, we're only trying safe edges right now!
			
			// unplay edge
			edge.unplace();
			this.rewind(edge);

			if(pair == null){
				// no pieces down this path! skip this edge
				continue;
			} else {
				pair.edge = edge;
				if (pair.piece == piece){
					return pair; // found a winning move for this player!
				} else {
					// this move is not a winner, continue the searching!
					result = pair;
				}
			}
			
		}
		
		return result; 
	}
	
	private int winner(int piece) {
		// it's piece's turn, who is going to win? Piece.BLUE or Piece.RED?
		return (piece == Piece.BLUE) ? Piece.RED : Piece.BLUE;
	}


	private class SearchPair {
		public Edge edge;
		public int piece;
		SearchPair(Edge edge, int value){
			this.edge = edge;
			this.piece = piece;
		}
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


//
//private SearchPair minimaxOLD(Edge last, int piece) throws TimeoutException{
//
//	
//	
//	
//	// base case, no more searching
//	if(cutoff()){
//		return new SearchPair(null, evaluation((piece == this.piece)));
//	
//	}
//	
//	// otherwise, we have some searching to do!
//	
//	// try to maximise if we're paying OUR piece,
//	// otherwise try to minimise
//	
//	boolean maxing = (piece == this.piece);
//	
//	// try all of the safe edges
//	
//	//SearchPair best = new SearchPair(null, 0);
//	SearchPair best = null;
//	
//	// copy for safe iterating
//	Edge[] edges = safe.toArray(new Edge[safe.size()]);
//	
//	for(Edge edge : edges){
//		
//		// only consider edges that are greater than the last edge
//		// (this ensures that each combination is only evaluated
//		//  once, in increasing order by edges)
//		if(last == null || last.compareTo(edge) < 0){
//			
//			// if it's less, skip this edge
//			continue;
//		}
//		
//		// otherwise, actually try this edge
//		
//		safe.remove(edge);
//		
//		// swap pieces unless a cell is captured
//		int nextPiece = piece;
//		if(edge.numCapturableCells() == 0){
//			nextPiece = (piece == Piece.RED) ? Piece.BLUE : Piece.RED;	
//		}
//		
//		// make this move
//		edge.place(piece);
//		this.update(edge);
//		
//		// recursively evaluate this edge
//		SearchPair sp = minimaxOLD(edge, nextPiece);
//		
//		// if the function returned null, either we're out
//		// of time, or there were no edges down this way?
//		if(sp == null){
//			
//			// TODO
//			continue;
//		}
//		
//		sp.edge = edge;
//		
//		// compare this choice
//		if(best == null){
//			best = sp;
//		}
//		if(maxing) {
//			if(sp.value > best.value){
//				best = sp;
//			}
//			if (best.value == 1) {
//				edge.unplace();
//				this.rewind(edge);
//				return best;
//			}
//		} else {
//			if(sp.value < best.value){
//				best = sp;
//			}
//			if (best.value == 0) {
//				edge.unplace();
//				this.rewind(edge);
//				return best;
//			}
//		}
//			
//		// unmake this move
//		edge.unplace();
//		this.rewind(edge);
//		
//	}
//	
//	// return the best option (will still be null if no remaining edges were less)
//	return best;
//}


//private class Path {
//	public Stack<Edge> edges = new Stack<Edge>();
//	public final int value;
//	public Path(int value){
//		this.value = value;
//	}
//}

//private class SearchPair {
//	public Edge edge;
//	public int value;
//	SearchPair(Edge edge, int value){
//		this.edge = edge;
//		this.value = value;
//	}
//}

//private int evaluation(boolean max){
//	return max ^ ((numShortChains() % 2) == 0) ? 1 : 0;
//}