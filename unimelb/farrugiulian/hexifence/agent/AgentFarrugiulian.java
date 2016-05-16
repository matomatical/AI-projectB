/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.*;
import unimelb.farrugiulian.hexifence.board.features.*;

/** This Agent plays the game of Hexifence in three stages:
 *  <br>
 *  <ul>
 *  	<li>
 *  		During the opening state, the agent greedily captures any available
 *  		cells and then makes a random safe move (one that does not offer
 *  		any cells to the oponent)
 *  	</li>
 *  	<li>
 *  		When there are only MIDGAME_SEARCH_DEPTH safe edges left, the agent
 *  		will begin to conduct an approximate adversarial search all the way
 *  		to a terminal state, considering only safe edges until none remain,
 *  		and then considering board features such as sacrifices, chains and
 *  		loops. It will attemt to find a winning move within a specified
 *  		time, and then play that move.
 *  	</li>
 *  	<li>
 *  		Once the board has no more safe edges left, the agent will again
 *  		search until a terminal state considering sacrifices and other
 *  		features looking for a winning move, or a move that wil give the
 *  		opponent a chance to make a mistake if there are no winning moves
 *  		left.
 *  	</li>
 *  </ul>
 *  
 *  @author Matt Farrugia [farrugiam]
 *  @author Julian Tran   [juliant1]
 **/
public class AgentFarrugiulian extends Agent {
	
	/** Number of Safe edges that signals the beginning of the Midgame stage**/
	private static final int MIDGAME_SEARCH_DEPTH = 30;
	
	/** Timeout (in ms) for midgame searching **/
	private static final int MIDGAME_SEARCH_TIMEOUT = 500;
	
	/** Enum to keep track of game stage **/
	private enum GameStage{
		OPENING, MIDGAME, ENDGAME;
	}
	/** Begin in opening stage **/
	private GameStage stage = GameStage.OPENING;
	
	/** Collection of empty edges on the current board **/
	private EdgeSet es;
	
	/** For timing the midgame searches **/
	private long clock;
	
	/** Initialize the agent **/
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
	
	/** Returns the move to be made by the agent in the opening stages */
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
	
	/** Returns the move to be made by the agent in the midgame stages */
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
	
	/** Returns the move to be made by the agent in the endgame */
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
				SearchPair<Feature> sp = featureSearch(fs, Board.other(piece));
				while(!stack.isEmpty()){
					board.unplace(stack.pop());
				}
				if (sp.piece == super.piece) {
					System.out.println("Double boxing");
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
					System.out.println("Not double boxing");
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
			fs = new FeatureSet(board, super.piece);
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
	
	/** Performs a minimax search on the safe edges on the board until there
	 *  are none left!
	 * @param piece Find a move given this this piece is to play next
	 * @return SearchPair of an edge and the winning player assuming optimal
	 * play according to our search functions (which are not actually optimal
	 * as they are not considering all possible or equivalently possible moves,
	 * for example we do not consider sacrificing while safe edges remain)
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
							&& connection.length() < 3 && connection.getFeatures().size() == 2) {
						shouldAdd = true;
					}
				}
				if (shouldAdd) {
					for (Feature sacrifice : feature.getFeatures()) {
						if (sacrifice.length() < 3 && !intersectedSacrifices.contains(sacrifice)) {
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
			// If this feature is a chain that has length strictly less than the
			// current smallest sacrifice, then update
			// If this feature is a loop that is a sensible sacrifice (the
			// intersection it connects to has at least 3 features, or it has another
			// loop) and has length strictly less than the current smallest sacrifice,
			// them update
			// If this feature is an isolated loop that has length less than or equal
			// to the current smallest sacrifice, then update
			if (feature.classification() == Feature.Classification.CHAIN
					&& (smallestSacrifice == null
							|| feature.length() < smallestSacrifice.length())
					|| feature.classification() == Feature.Classification.LOOP
					&& (feature.getFeatures().get(0).getFeatures().size() >= 3
							|| feature.getFeatures().get(0).getFeatures().get(0) != feature
									&& feature.getFeatures().get(0).getFeatures().get(0).classification() == Feature.Classification.LOOP
							|| feature.getFeatures().get(0).getFeatures().get(1) != feature
									&& feature.getFeatures().get(0).getFeatures().get(1).classification() == Feature.Classification.LOOP)
					&& (smallestSacrifice == null
							|| feature.length() < smallestSacrifice.length())
					|| feature.classification() == Feature.Classification.ISO_LOOP
					&& (smallestSacrifice == null
							|| feature.length() <= smallestSacrifice.length())) {
				smallestSacrifice = feature;
			}
		}
		if (smallestSacrifice == null) {
			for (Feature feature : features.getFeatures()) {
				System.out.println(feature.toString());
			}
			try {
				throw new IOException();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			while (true) {}
		}
		return smallestSacrifice;
	}
	
	private int playout(FeatureSet features, int piece) {
		int currentPiece = piece;
		while (!features.isEmpty()) {
			Feature smallestSacrifice = getSmallestSacrifice(features);
			// Check if this smallest chain either an isolated loop of size 3, or a
			// chain or loop of size less than 3 (we are asssuming that any loop being
			// considered is a sensible sacrifice)
			if ((smallestSacrifice.classification() == Feature.Classification.CHAIN
					|| smallestSacrifice.classification() == Feature.Classification.LOOP)
					&& smallestSacrifice.length() < 3
					|| smallestSacrifice.classification() == Feature.Classification.ISO_LOOP
					&& smallestSacrifice.length() == 3) {
				// We cannot double box these sacrifices, so next player's turn
				smallestSacrifice.consume(Board.other(currentPiece), false);
				currentPiece = Board.other(currentPiece);
			} else {
				// We double box these sacrifices
				Feature.Classification classification = smallestSacrifice.classification();
				smallestSacrifice.consume(Board.other(currentPiece), true);
				int numIsolatedClusters = numIsolatedClusters(features);
				// But maybe it is not a good idea to, because this sacrifice may
				// create isolated 3 clusters, or end the game
				if (numIsolatedClusters % 2 == 1
						^ numIsolatedClusters == features.getFeatures().size()) {
					// Fix up the score and the current player as if we did not double box
					int swing = classification == Feature.Classification.CHAIN ? 2 : 4;
					features.score(currentPiece, -swing);
					features.score(Board.other(currentPiece), swing);
					currentPiece = Board.other(currentPiece);
				}
			}
		}
		// Return the winning piece
		if (stage == GameStage.ENDGAME) {
			System.out.println(features.score(Piece.BLUE));
		}
		return features.score(piece) > 0 ? piece : Board.other(piece);
	}
	
	// Counts the number of isolated clusters
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