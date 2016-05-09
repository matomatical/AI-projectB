package unimelb.farrugiulian.hexifence.board.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import unimelb.farrugiulian.hexifence.board.Board;
import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

public class FeatureSet {

	private ArrayList<Chain> longChains = new ArrayList<Chain>();
	private ArrayList<Chain> shortChains = new ArrayList<Chain>();
	
	// what should we track?
	private ArrayList<Loop> longLoops = new ArrayList<Loop>();
	private ArrayList<Loop> shortLoops = new ArrayList<Loop>();
	private ArrayList<Intersection> intersections = new ArrayList<Intersection>();
	// what other features?
	
	// 
	private int myScore = 0, yourScore = 0;
	
	public FeatureSet(Board board){
		
		// process board into raw features
		ArrayList<RawFeature> features = chainify(board);
		
		// process raw features into rich features
		process(features);
	}
	
	private void process(ArrayList<RawFeature> features){
		HashMap<RawFeature, RichFeature> map = new HashMap<RawFeature, RichFeature>; 
		
		for(RawFeature raw : features){
			if(raw.classification() == Classification.DEAD){
				if(raw.getCells()[0])
			}
		}
	}

	private ArrayList<RawFeature> chainify(Board board){
		
		// do we need a feature map instead?
		ArrayList<RawFeature> features = new ArrayList<RawFeature>();
		
		Cell[] cells = board.getCells();
		HashSet<Cell> visited = new HashSet<Cell>();
		
		for(Cell cell : cells){
			// if this cell has not already been visited,
			if(!visited.contains(cell)){
				// classify it
				features.add(classify(cell, visited));
			}
		}
		
		return features;
	}
	
	private RawFeature classify(Cell cell, HashSet<Cell> visited) {
		
		int n = cell.numFreeEdges();
		
		RawFeature f;
		
		if(n == 0){
			// captured cell
			f = new RawFeature(Classification.DEAD);
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
			f = new RawFeature(Classification.CHAIN);
			
			// add the cell then explore
			f.add(cell);
			explore(cell, null, visited, f);
			
		} else {
			// intersection!
			f = new RawFeature(Classification.INTERSECTION);
			f.add(cell);
			
		}
		
		return f;
	}

	private void explore(Cell cell, Cell parent, HashSet<Cell> visited, RawFeature feature) {
		
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
}
