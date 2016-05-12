package unimelb.farrugiulian.hexifence.agent.farrugiulian;

import com.matomatical.util.QueueHashSet;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.*;

public class MidgameExpert implements Expert {

	/** edges that represent cells free for the taking */
	private QueueHashSet<Edge> free;
	private QueueHashSet<Edge> safe;

	private int piece;
	
	public MidgameExpert(Board board, int piece) {
	
		this.piece = piece;
		
		// TODO it'd be nice if we could use the same sets as the opening expert,
		// as they are already up to date! perhaps merge these two agents?
		free = new QueueHashSet<Edge>();
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
	@Override
	public void update(Edge edge) {
		
		// remove this edge from any set it was in
		// (take advantage of lazy evaluation here)
		if(safe.remove(edge) || free.remove(edge)){}
		
		
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
					free.add(e);
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
				// this was the only edge, it's a freebie now
				free.add(edge);
				
			} else if(n == 2){
				// these edges are no longer free! they're
				// sacrifices now
				for(Edge e : cell.getFreeEdges()){
					free.remove(e);
				}
				
			} else if (n > 2){
				// these edges are now no longer sacrifices, they're all safe!
				for(Edge e : cell.getFreeEdges()){
					safe.add(e);
				}
			}
		}
	}


	@Override
	public Edge move() {
		
		// select moves that capture a cell, if they exist

		// PERHAPS CONSIDER SACRIFICING (IF ALL EVALUATIONS TURNS OUT BAD) TO SWITCH PARITY?
		if(free.size() > 0){
			return free.remove();
		}
		
		// if not, there are only safe edges, start a search for the best move
		System.out.println("starting minimax!");
		SearchPair sp = minimax(piece);
		System.out.println("ending minimax!");
		System.out.println("returning: "+sp.edge.toString());
		return sp.edge;
		
		// TODO: PERHAPS CONSIDER SACRIFICING TO SWITCH PARITY,
		//       IF ALL EVALUATIONS TURN OUT BAD?

		// if(sp.value >= 0){
		//	return sp.edge;
		// } else {
		//	// search sacrifices? 
		// }
		
		// TODO: TRY MONTE-CARLO TO
	}

	private SearchPair minimax(int piece){
		
		// base case, no more searching
		if(cutoff()){
			return new SearchPair(null, evaluation());
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
			} else if(maxing) {
				if(sp.value > best.value){
					best = sp;
				}
			} else {
				if(sp.value < best.value){
					best = sp;
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
		// TODO
		
		return 0;
	}
	
	private boolean cutoff(){
		return safe.size() == 0; 
	}
	
	@Override
	public boolean transition() {
		return safe.size() == 0;
	}
}
