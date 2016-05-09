package unimelb.farrugiulian.hexifence.agent;

import java.util.Random;

import unimelb.farrugiulian.hexifence.board.*;

public class AgentFarrugiulian extends Agent{

	// random number generator
	private Random rng = new Random(System.nanoTime());
	
	
	// track state of the game
	private enum GameState {
		OPENING, MIDGAME, ENDGAME
	}
	private GameState state = GameState.OPENING;
	
	
	// opening game variables
	
	
	// midgame variables
	private int maxSearchDepth = 0;
	
	// endgame variables
	
	
	
	@Override
	public int init(int n, int p){
		int r = super.init(n, p);

		// my init stuff
		
		return r;
	}
	
	@Override
	protected void update(Edge edge) {
		
		// respond to placed edge
		
		// transitions
		
//		int n = numSafeEdges;
//		if(n <= 0){
//			state = GameState.ENDGAME;
//		} else if (n <= maxSearchDepth){
//			state = GameState.MIDGAME;
//		}
	}
	
	@Override
	public Edge getChoice(){
	
		
		if(state == GameState.OPENING){
			return getOpeningChoice();
			
			
		} else if(state == GameState.MIDGAME){
			return getMidgameChoice();
			
			
		} else if(state == GameState.ENDGAME){
			return getEndgameChoice();
			
		} else {
			// state will always be one of these, this won't happen
			return null;
		}
	}

	
	
	private Edge getOpeningChoice() {
		// silly way to randomly play (could be more efficient by storing state!)
		return board.getFreeEdges()[rng.nextInt(board.numFreeEdges())];
	}

	
	
	private Edge getMidgameChoice() {
		// in the middle of the game, conduct search of some kind
		return null;
	}


	private Edge getEndgameChoice() {
		// at the end of the game, search not based on edged but based on
		// endgame actions like consuming, sacrificing, and double boxing
		return null;
	}
}