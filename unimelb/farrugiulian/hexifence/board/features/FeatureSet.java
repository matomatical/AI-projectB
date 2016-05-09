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
	private ArrayList<Chain> twoChains = new ArrayList<Chain>();

	private ArrayList<Loop> longLoops = new ArrayList<Loop>();
	private ArrayList<Loop> shortLoops = new ArrayList<Loop>();
	
	private ArrayList<Intersection> intersections = new ArrayList<Intersection>();
	
	private int myScore = 0, yourScore = 0;
	
	public FeatureSet(Board board, int piece){
		
		// process board into raw features
		ArrayList<RawFeature> features = chainify(board);
		
		// process raw features into rich features
		process(features, piece);
	}
	
	private void process(ArrayList<RawFeature> features, int piece){
		HashMap<Cell, Intersection> map = new HashMap<Cell, Intersection>(); 
		
		for(RawFeature raw : features){
			if(raw.classification() == Classification.DEAD){
				
				// no need to store these as features, but we will track the score
				if(raw.getCells()[0].getColor() == piece){
					myScore++;
				
				} else {
					yourScore++;
				}
				
			} else if(raw.classification() == Classification.CHAIN){
				// create new chain feature
				Chain chain = new Chain(raw.getCells());
				
				// link to 0-2 intersection(s)
				for(Cell end : raw.getEnds()){
					if(end != null){
						Intersection intersection = map.getOrDefault(end, new Intersection(end));
						
						intersection.addChain(chain);
						chain.addIntersection(intersection);
						
						map.put(end, intersection);
					}
				}
				
				// store the feature
				if(chain.length() > 2){
					longChains.add(chain);
				} else if(chain.length() < 2){
					shortChains.add(chain);
				} else {
					twoChains.add(chain);
				}
				
				
			} else if(raw.classification() == Classification.ISO_LOOP){
				// create new IsoLoop feature
				Loop loop = new Loop(raw.getCells());
				
				// it's isolated, so no intersections to think about
				
				// just store the loop
				if(loop.length() > 3){
					longLoops.add(loop);
				} else {
					shortLoops.add(loop);
				}
			} else if(raw.classification() == Classification.LOOP){
				// create new loop feature
				Loop loop = new Loop(raw.getCells()); 
				
				// link to intersection
				Cell end = raw.getEnds()[0];
				Intersection intersection = map.getOrDefault(end, new Intersection(end));
				
				intersection.addLoop(loop);
				loop.addIntersection(intersection);
				
				map.put(end, intersection);
				
				// and store the loop
				if(loop.length() > 3){
					longLoops.add(loop);
				} else {
					shortLoops.add(loop);
				}
				
			} else if(raw.classification() == Classification.INTERSECTION){
				
				// make a new intersection feature (if it doesn't already exist)
				Cell cell = raw.getCells()[0];
				Intersection intersection = map.getOrDefault(cell, new Intersection(cell));	
				
				// store the intersection
				intersections.add(intersection);
				
				// and ensure it's in the map
				map.put(cell, intersection);
				
			} else {
				// we've encountered something we haven't considered (open chains etc)
				System.err.println("Hey! Looks like you're not processing "
					+ raw.classification().name() + " in FeatureSet.process()");
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
