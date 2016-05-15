package unimelb.farrugiulian.hexifence.agent;

import unimelb.farrugiulian.hexifence.board.*;
import unimelb.farrugiulian.hexifence.board.features.EdgeSet;
import unimelb.farrugiulian.hexifence.board.features.FeatureSet;

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
		// TODO Auto-generated method stub
		return null;
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
}