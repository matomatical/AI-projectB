package unimelb.farrugiulian.hexifence.agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import unimelb.farrugiulian.hexifence.board.*;
import unimelb.farrugiulian.hexifence.board.features.*;

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
	protected void notify(Edge edge) {
		
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

	public static Feature[] chainify(Board board){
		
		// do we need a feature map instead?
		ArrayList<Feature> features = new ArrayList<Feature>();
		
		Cell[] cells = board.getCells();
		HashSet<Cell> visited = new HashSet<Cell>();
		
		for(Cell cell : cells){
			// if this cell has not already been visited,
			if(!visited.contains(cell)){
				// classify it
				features.add(classify(cell, visited));
			}
		}
		
		return features.toArray(new Feature[features.size()]);
	}
	
	private static Feature classify(Cell cell, HashSet<Cell> visited) {
		
		int n = cell.numFreeEdges();
		
		Feature f;
		
		if(n == 0){
			// captured cell
			f = new Feature(Classification.DEAD);
			f.add(cell);
			
		} else if(n == 1){
			// capturable cell, start an open chain?
			// i suppose this shouldn't happen most of the time when we're calling chainify ?
//			f = new OpenChain();
//			f.add(cell);
			System.out.println("open chains! take these first?");
			return null;
		
		} else if(n == 2){
			// part of a chain/loop/we don't know yet
			f = new Feature(Classification.CHAIN);
			
			// add the cell then explore
			f.add(cell);
			explore(cell, null, visited, f);
			
		} else {
			// intersection!
			f = new Feature(Classification.INTERSECTION);
			f.add(cell);
			
		}
		
		return f;
	}

	private static void explore(Cell cell, Cell parent, HashSet<Cell> visited, Feature feature) {
		
		Edge[] edges = cell.getFreeEdges();
		
		// find the rest of the chain
		for(Edge edge : edges){
			
			// look at the neighbouring cells
			Cell other = edge.getOtherCell(cell);
			
			
			if(other == null){
				// we're at the edge of a board!
				feature.end(null);
			
			} else if(other == parent){
				// we're looking backwards
				continue;
				
			} else if (other.numFreeEdges() == 1){
				// turns out this is an open chain
				System.out.println("open chains! take these first?");
				feature.add(other);
				
			} else if(other.numFreeEdges() > 2){
				// we've found an intersection!
				if(feature.end(other)){
					// this is a non-isolated loop!
					feature.classify(Classification.LOOP);
				}
				
				// should we add the intersection TO the loop?
			
			} else if (other.numFreeEdges() == 2){
			
				if(feature.add(other)){
					// no loop, keep up the recursion 
					explore(other, cell, visited, feature);
					
				} else {
					// this cell is already in the chain, meaning it's an isolated loop!
					// this is an isolated loop!
					feature.classify(Classification.ISO_LOOP);
				}
			}
		}
		
		// mark this cell as visited
		visited.add(cell);
		return;
	}

	private Edge getEndgameChoice() {
		// at the end of the game, search not based on edged but based on
		// endgame actions like consuming, sacrificing, and double boxing
		return null;
	}
}