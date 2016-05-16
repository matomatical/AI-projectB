package unimelb.farrugiulian.hexifence.agent;

import java.util.ArrayList;
import java.util.Stack;

import com.sun.xml.internal.fastinfoset.sax.Features;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.*;
import unimelb.farrugiulian.hexifence.board.features.*;

public class AgentFarrugiulian extends Agent {
	/** Enter the midgame state when the number of safe edges fall below this
	 *  value
	 **/
	private static final int MIDGAME_SEARCH_DEPTH = 19;
	
	/** How long to minimax search safe edges for during midgame before giving
	 *  up (in ms)
	 **/
	private static final int MIDGAME_SEARCH_TIMEOUT = 5000;
	
	/** Finite state machine. During the opening state, the agent greedily
	 *  makes scoring moves, then safe moves. During the midgame state, the
	 *  agent greedily makes scoring moves, then does a approximate adversarial
	 *  search for the best safe move. During the endgame state, the agent
	 *  greedily makes scoring moves that have no consequences, then plays out
	 *  the game heuristically using an adversarial search for the best way to
	 *  break up intersections between chains and loops.
	 **/
	private enum GameStage{
		OPENING, MIDGAME, ENDGAME;
	}
	
	/** State variable, initially in the opening state */
	private GameStage stage = GameStage.OPENING;
	
	/** Classification of the empty edges on the board */
	private EdgeSet es;
	
	/** For timing the midgame searches */
	private long clock;
	
	/** Initializes the agent */
	@Override
	public int init(int n, int p){
		if(super.init(n, p) != 0){
			// parent init failure!!
			return -1;
		}
		
		// parent init success!
		
		this.es = new EdgeSet(super.board, true);
		
		// return the same value as superclass
		return 0;
	}
	
	/** Notifies the agent that an edge has been placed on the board */
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
	
	/** Queries the agent for its next move */
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
	
	/** Returns the move to be made by the agent in the opening state */
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
	
	/** Returns the move to be made by the agent in the midgame state */
	private Edge midgameMove() {
		
		// select any captureable edges, if they exist
		
		if(es.hasCapturingEdges()){
			return es.getCapturingEdge();
		}
		
		// if not, do some searching to find a winning safe edge!
		
		clock = System.currentTimeMillis();
		SearchPair<Edge> sp = safeEdgeSearch(piece);
		System.out.println("Predicted winner: " + Board.name(sp.piece));
		return sp.choice;
	}
	
	/** Returns the move to be made by the agent in the endgame state */
	private Edge endgameMove() {
		/*Stack<Edge> stack = new Stack<Edge>();
		consumeAll(stack);
		int numShortChains = numShortChains();
		while(!stack.isEmpty()){
			board.unplace(stack.pop());
		}
		System.out.print(Board.name(piece) + " has " + numShortChains + " short chains left and should ");
		if (numShortChains % 2 == 0) {
			System.out.println("lose");
		} else {
			System.out.println("win");
		}
		while (true) {}*/
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
				SearchPair<Feature> sp = featureSearch(fs, super.piece);
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
		SearchPair<Feature> sp = featureSearch(fs, super.piece);
		if (sp.choice == null) {
			if (sp.piece == super.piece) {
				// No more intersected sacrifices and we should win
				// Get the smallest chain
				sp.choice = getSmallestSacrifice(fs);
			} else {
				// No more intersected sacrifices and we should lose
				// Get a chain such that the last sacrifice is not a loop
				// (not too sure how to do this just yet so just get the smallest chain)
				sp.choice = getSmallestSacrifice(fs);
			}
		}
		if (sp.piece == super.piece) {
			// We should win so make the sacrifice securely
			return sp.choice.choose(false);
		} else {
			// We should lose so make a baiting sacrifice
			return sp.choice.choose(true);
		}
	}
	
	/** Performs a minimax search on the safe edges on the board until there are
	 * none left
	 * @param piece
	 * @return
	 **/
	private SearchPair<Edge> safeEdgeSearch(int piece) {
		
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
				// nope, give up and return an edge we have not tried yet
				// (that way at least it is not definitely a losing edge)
				System.out.println("Timeout, returning");
				return new SearchPair<Edge>(safes[safes.length - 1], piece);
			}
			
			// play edge
			edge.place(piece);
			es.update(edge);
			
			// recursively search for result
			// piece will always swap, since we're only trying safe edges!
			SearchPair<Edge> pair = safeEdgeSearch(Board.other(piece));
			
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
	
	/** Performs a minimax search on the intersected sacrifice features on the
	 * board until there are none left
	 * @param piece
	 * @return
	 **/
	private SearchPair<Feature> featureSearch(FeatureSet features, int piece) {
		int numIntersectedSacrifices = numIntersectedSacrifices(features);
		if (numIntersectedSacrifices == 0) {
			// Simple parity evaluation right now
			//int numSacrifices = numSacrifices(features);
			//int winningPiece = (piece == Piece.BLUE) ^ (numSacrifices % 2 == 0) ? Piece.BLUE : Piece.RED;
			int winningPiece = playout(features, piece);
			return new SearchPair<Feature>(null, winningPiece);
		}
		
		SearchPair<Feature> result = null;
		for (int i = 0; i < numIntersectedSacrifices; i++) {
			FeatureSet featuresTmp = new FeatureSet(features);
			getIntersectedSacrifices(featuresTmp).get(i).consume(Board.other(piece), false);
			
			SearchPair<Feature> pair = featureSearch(featuresTmp, Board.other(piece));
			
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
	
	private ArrayList<Feature> getIntersectedSacrifices(FeatureSet features) {
		ArrayList<Feature> intersectedSacrifices = new ArrayList<Feature>();
		for (Feature feature : features.getFeatures()) {
			if (feature.classification() == Feature.Classification.INTERSECTION) {
				boolean shouldAdd = false;
				for (Feature connection : feature.getFeatures()) {
					if (connection.classification() == Feature.Classification.CHAIN
							&& connection.length() < 3 && connection.getFeatures().size() == 2
							&& (connection.getFeatures().get(0) != feature
									|| connection.getFeatures().get(1) != feature)) {
						shouldAdd = true;
						break;
					}
				}
				if (shouldAdd) {
					for (Feature sacrifice : feature.getFeatures()) {
						if (sacrifice.length() < 3) {
							intersectedSacrifices.add(sacrifice);
						}
					}
				}
			}
		}
		return intersectedSacrifices;
	}
	
	private int numIntersectedSacrifices(FeatureSet features) {
		return getIntersectedSacrifices(features).size();
	}
	
	private Feature getSmallestSacrifice(FeatureSet features) {
		Feature smallestSacrifice = null;
		for (Feature feature : features.getFeatures()) {
			// Well this is a mouthful:
			// If this feature is a chain that is not a loop OR is a loop that does
			// not open up more sacrifices, then make it the smallest sacrifice
			// if there is not a current smallest sacrifice, or if it is actually
			// strictly smaller to it
			// If this feature is an isolated loop, then make it the smallest
			// sacrifice if there is not a current smallest sacrifice, or if it is 
			// actually smaller or equal to it
			if (feature.classification() == Feature.Classification.CHAIN
					&& (feature.getFeatures().size() < 2
							|| feature.getFeatures().get(0).getFeatures().size() > 3)
					&& (smallestSacrifice == null
							|| feature.length() < smallestSacrifice.length())
					|| feature.classification() == Feature.Classification.ISO_LOOP
					&& (smallestSacrifice == null
							|| feature.length() <= smallestSacrifice.length())) {
				smallestSacrifice = feature;
			}
		}
		return smallestSacrifice;
	}
	
	private int playout(FeatureSet features, int piece) {
		int currentPiece = piece;
		while (!features.isEmpty()) {
			Feature smallestSacrifice = getSmallestSacrifice(features);
			// Check if this smallest chain either an isolated loop of size 3,
			if (smallestSacrifice.classification() == Feature.Classification.CHAIN
					&& smallestSacrifice.length() < 3
					&& (smallestSacrifice.getFeatures().size() < 2
							|| smallestSacrifice.getFeatures().get(0).getFeatures().size() > 3)
					|| smallestSacrifice.classification() == Feature.Classification.ISO_LOOP
					&& smallestSacrifice.length() == 3) {
				smallestSacrifice.consume(Board.other(currentPiece), false);
				currentPiece = Board.other(currentPiece);
			} else {
				smallestSacrifice.consume(Board.other(currentPiece), true);
				int numIsolatedClusters = numIsolatedClusters(features);
				if (numIsolatedClusters % 2 == 1
						^ numIsolatedClusters == features.getFeatures().size()) {
					features.score(currentPiece, -2);
					features.score(Board.other(currentPiece), 2);
					currentPiece = Board.other(currentPiece);
				}
			}
		}
		return features.score(piece) > 0 ? piece : Board.other(piece);
	}
	
	private int numIsolatedClusters(FeatureSet features) {
		int numIsolatedClusters = 0;
		for (Feature feature : features.getFeatures()) {
			if (feature.classification() == Feature.Classification.ISO_LOOP
					&& feature.length() == 3) {
				numIsolatedClusters++;
			}
		}
		return numIsolatedClusters;
	}
	
	/** Evaluation function for the midgame search
	 * @param piece
	 *       Which piece plays next
	 * @return
	 *       The piece expected to win
	 **/
	private int winner(int piece){
		//return Board.other(piece);
		//return (piece == Piece.BLUE) ^ (numShortChains() % 2 == 0) ? Piece.BLUE : Piece.RED;
		
		// Mate you thought you were done minimaxing? Sorry your evaluation function
		// is another minimax...
		FeatureSet features = new FeatureSet(board, piece);
		return featureSearch(features, piece).piece;
	}
	
	/** Return type class for searches in the midgame and endgame
	 * @param <Type>
	 *       The type of the choices being made in the search
	 */
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