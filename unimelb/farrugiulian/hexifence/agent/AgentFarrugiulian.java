package unimelb.farrugiulian.hexifence.agent;

import java.util.Stack;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.agent.EndgameExpert.FeaturePair;
import unimelb.farrugiulian.hexifence.board.*;
import unimelb.farrugiulian.hexifence.board.features.EdgeSet;
import unimelb.farrugiulian.hexifence.board.features.FeatureSet;
import unimelb.farrugiulian.hexifence.board.features.RichFeature;

public class AgentFarrugiulian extends Agent {

	private static final int MIDGAME_SEARCH_DEPTH = 19;
	private static final int MIDGAME_SEARCH_TIMEOUT = 5000; // milliseconds
	
	private enum GameStage{
		OPENING, MIDGAME, ENDGAME;
	}
	
	private GameStage stage = GameStage.OPENING;
	
	private EdgeSet es;
	private FeatureSet fs;
	
	private long clock; // for search timing
	
	@Override
	public int init(int n, int p){
		if(super.init(n, p) != 0){
			// parent init failure!!
			return -1;
		}
		
		// parent init success!
		
		this.es = new EdgeSet(super.board);
		
		// return the same value as superclass
		return 0;
	}
	
	@Override
	protected void update(Edge edge) {
		
		// maintain the edge set
		this.es.update(edge);
		
		// possibly transition to next game stage 
		if(this.stage == GameStage.OPENING){
			if(this.es.numSafeEdges() < MIDGAME_SEARCH_DEPTH){
				System.err.println("Entering midgame");
				this.stage = GameStage.MIDGAME;
			}
		} else if (this.stage == GameStage.MIDGAME){
			if( ! this.es.hasSafeEdges()){
				System.err.println("Entering endgame");
				this.stage = GameStage.ENDGAME;
			}
		}
	}
	
	@Override
	public Edge getChoice(){
	
		// no matter the game stage, capture any consequence-free cells
		if(es.hasFreeEdges()){
			return es.getFreeEdge();
		}
		
		// otherwise, time to make a real decision
		switch(this.stage){
		default:
		case OPENING:
			return openingMove();
			
		case MIDGAME:
			return midgameMove();
			
		case ENDGAME:
			return endgameMove();
		}
	}
	
	public Edge openingMove(){
		
		// select any captureable edges, if they exist
		
		if(es.hasCapturingEdges()){
			return es.getCapturingEdge();
		}
		
		// if not, select any random safe edge
		
		if(es.hasSafeEdges()){
			return es.getSafeEdge();
		}
		
		// we won't get here because we will transition before we run out of
		// safe edges!
		
		return null;
	}
	
	private Edge midgameMove() {
		
		// select any captureable edges, if they exist
		
		if(es.hasCapturingEdges()){
			return es.getCapturingEdge();
		}
		
		// if not, do some searching to find a winning safe edge!
		
		clock = System.currentTimeMillis();
		return midgameMinimax(piece).choice;
//		System.err.println("Search complete! Predicted winner: " + Board.name(sp.piece));
	}
	
	private Edge endgameMove() {
		// If we have edges we can capture
		if (es.hasCapturingEdges()) {
			// Count the maximum number of cells we can take
			Stack<Edge> stack = new Stack<Edge>();
			int capturable = consumeAll(stack);
			while(!stack.isEmpty()){
				board.unplace(stack.pop());
			}
			
			// Check if we are in a position to double (double) box
			boolean isLoop = isLoop(es.getCapturingEdge());
			if (capturable == 2 || capturable == 4 && isLoop) {
				// Decide whether we should actually double box with a search
				consumeAll(stack);
				FeatureSet fs = new FeatureSet(board, super.piece);
				while(!stack.isEmpty()){
					board.unplace(stack.pop());
				}
				SearchPair<RichFeature> sp = featureSearch(fs, super.piece);
				if (sp.piece == super.piece) {
					// If double boxing makes us win, then double box (duh)
					Cell[] cells = es.getCapturingEdge().getCells();
					Cell cell;
					// Get the cell that has the edge that can double box
					if (cells[0].numEmptyEdges() == 2) {
						cell = cells[0];
					} else {
						cell = cells[1];
					}
					// Figure out which edge can double box
					if (cell.getEmptyEdges()[0] == es.getCapturingEdge()){
						return cell.getEmptyEdges()[1];
					} else {
						return cell.getEmptyEdges()[0];
					}
				} else {
					// If double boxing does not make us win, then take the cells
					// Sure it may not make us win either, but perhaps the opponent does
					// not need to throw as hard to let us win if we get more score now
					return es.getCapturingEdge();
				}
			}
			
			// We cannot double box, so take edges by the following priority:
			// Short loops, then long loops, then short chains, then long chains
			// Keep in mind that all free cells are automatically taken
			Edge bestEdge = null;
			isLoop = false;
			int captureSize = 10000;
			for (Edge edge : es.getCapturingEdges()) {
				int sacrificeSize = sacrificeSize(edge);
				boolean isCurrentLoop = isLoop(edge);
				if ((isCurrentLoop || !isLoop ) && sacrificeSize < captureSize) {
					bestEdge = edge;
					captureSize = sacrificeSize;
					isLoop = isCurrentLoop;
				}
			}
			return bestEdge;
		}
		
		// We do not have edges we can capture, so we need to make a sacrifice
		FeatureSet fs = new FeatureSet(board, super.piece);
		SearchPair<RichFeature> sp = featureSearch(fs, super.piece);
		if (sp.choice == null) {
			if (sp.piece == super.piece) {
				// No more intersected sacrifices and we should win
				// Get the smallest chain
				sp.choice = fs.getSmallestChain();
			} else {
				// No more intersected sacrifices and we should lose
				// Get a chain such that the last sacrifice is not a loop
				// (not too sure how to do this just yet so just get the smallest chain)
				sp.choice = fs.getSmallestChain();
			}
		}
		if (sp.piece == super.piece) {
			// We should win so make the sacrifice securely
			return sp.choice.secureOpen();
		} else {
			// We should lose so make a baiting sacrifice
			return sp.choice.baitOpen();
		}
	}
	
	private SearchPair<Edge> midgameMinimax(int piece) {
		
		// base case / cutoff test; are we at lockdown?
		if(! this.es.hasSafeEdges()){
			return new SearchPair<Edge>(null, winner(piece));
		}
		
		// otherwise, we still have some searching to do!
		
		// for each edge, who is the winning player if we play that edge?
		
		SearchPair<Edge> result = null;
		Edge[] safes = es.getSafeEdges().toArray(new Edge[es.numSafeEdges()]);
		for(Edge edge : safes){
			
			// do we still have time?
			if(System.currentTimeMillis() - clock > MIDGAME_SEARCH_TIMEOUT){
				return new SearchPair<Edge>(safes[safes.length - 1],
														Board.other(piece));
			}
			
			// play edge
			edge.place(piece);
			this.update(edge);
			
			// recursively search for result
			// piece will always swap, since we're only trying safe edges!
			SearchPair<Edge> pair = midgameMinimax(Board.other(piece));
			
			// unplay edge
			edge.unplace();
			es.rewind(edge);
			
			pair.choice = edge;
			if (pair.piece == piece){
				return pair; // found a winning move for this player!
			} else {
				// this move is not a winner, continue the searching!
				result = pair;
			}
		}
		
		return result; 
	}
	
	private SearchPair<RichFeature> featureSearch(FeatureSet features, int piece) {
		int numIntersectedSacrifices = numIntersectedSacrifices(features);
		if (numIntersectedSacrifices == 0) {
			// Simple parity evaluation right now
			int numSacrifices = numSacrifices(features);
			int winningPiece = (piece == Piece.BLUE) ^ (numSacrifices % 2 == 0) ? Piece.BLUE : Piece.RED;
			return new SearchPair<RichFeature>(null, winningPiece);
		}
		
		SearchPair<RichFeature> result = null;
		for (int i = 0; i < numIntersectedSacrifices; i++) {
			FeatureSet featuresTmp = new FeatureSet(features);
			takeFeature(getIntersectedSacrifices(featuresTmp).get(i));
			
			SearchPair<RichFeature> pair = featureSearch(featuresTmp, Board.other(piece));
			
			pair.choice = getIntersectedSacrifices(features).get(i);
			if (pair.piece == piece){
				return pair; // found a winning move for this player!
			} else {
				// this move is not a winner, continue the searching!
				result = pair;
			}
		}
		
		return result;
	}
	
	private int winner(int piece){
		// pessimistic winner function
		return Board.other(piece);
	}
	
	private class SearchPair<Type> {
		public Type choice;
		public int piece;
		SearchPair(Type choice, int piece){
			this.choice = choice;
			this.piece = piece;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	// Old helper functions
	
	//Works ONLY if the scoring QueueHashSet is accurate
	private int consumeAll(Stack<Edge> stack){
		int capturable = 0;
		for (Edge edge : es.getCapturingEdges()){
			if (edge.isEmpty()){
				edge.place(super.opponent);
				stack.push(edge);
				capturable++;
				if (edge.getCells()[0].numEmptyEdges() > 0){
					capturable += sacrifice(edge.getCells()[0], stack);
				}
				if (edge.getCells().length == 2 && edge.getCells()[1].numEmptyEdges() > 0){
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
			if (edge.getCells()[0].numEmptyEdges() > 1 && (edge.getCells().length == 1
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