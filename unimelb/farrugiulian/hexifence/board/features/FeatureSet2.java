package unimelb.farrugiulian.hexifence.board.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import unimelb.farrugiulian.hexifence.board.Board;
import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

import unimelb.farrugiulian.hexifence.board.features.Feature.Classification;

public class FeatureSet2 {

	private ArrayList<Feature> features = new ArrayList<Feature>();
	
	private int myScore = 0, yourScore = 0;
	
	private HashMap<Cell, Feature> map = new HashMap<Cell, Feature>();
	
	public FeatureSet2(Board board, int piece){
		
		// perform a specialised DFS over cells to construct our collection of
		// board feature
		Cell[] cells = board.getCells();
		HashSet<Cell> visited = new HashSet<Cell>();
		
		for(Cell cell : cells){
			// if this cell has not already been visited,
			if(!visited.contains(cell)){
				// classify it
				classify(cell, visited, piece);
			}
		}
		
		// okay, now that we have placed all of the cells in a feature, let's
		// link all the features to eachother.
	}
	
	public void map(Cell cell, Feature feature) {
		
		this.map.put(cell, feature);
	}
	
	public Feature unmap(Cell cell){
		return this.map.get(cell);
	}
	
	private void classify(Cell cell, HashSet<Cell> visited, int piece) {
		
		int n = cell.numEmptyEdges();
		
		// see if this cell has already been captured
		if(n == 0){
			// captured cell
			if(cell.getColor() == piece){
				this.myScore++;
			} else {
				this.yourScore++;
			}
			
			return;
		}
		
		// otherwise, let's see if we can build a feature out of it!
		
		Feature feature;
		
		if(n == 1){
			// capturable cell, start an open chain
			// (it doesn't make sense for something that's ope not to be a chain)
			feature = new Feature(Classification.OPEN, this);
			feature.add(cell);
			explore(cell, null, visited, feature);
		
		} else if(n == 2){
			// part of a chain/loop/we don't know what yet
			feature = new Feature(Classification.CHAIN, this);
			
			// add the cell then explore
			feature.add(cell);
			explore(cell, null, visited, feature);
			
		} else {
			// intersection!
			feature = new Feature(Classification.INTERSECTION, this);
			feature.add(cell);
			
			// no need to explore!
		}
		
		features.add(feature);
	}

	private void explore(Cell cell, Cell parent, HashSet<Cell> visited, Feature feature) {
		
		Edge[] edges = cell.getEmptyEdges();
		
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
				
			} else if (other.numEmptyEdges() == 1){
				// turns out this is an open chain! this is safe, in that it
				// will never be replaced by a later observation
				feature.classify(Classification.OPEN);
				feature.add(other);
				
			} else if(other.numEmptyEdges() > 2){
				// we've found an intersection!
				if(feature.end(other)){
					// this is a non-isolated loop!
					feature.classify(Classification.LOOP);
				}
				
				// should we add the intersection TO the loop? nah
			
			} else if (other.numEmptyEdges() == 2){
			
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
