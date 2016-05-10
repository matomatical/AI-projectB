package unimelb.farrugiulian.hexifence.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import com.matomatical.util.QueueHashSet;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;
import unimelb.farrugiulian.hexifence.board.features.Chain;
import unimelb.farrugiulian.hexifence.board.features.FeatureSet;
import unimelb.farrugiulian.hexifence.board.features.Intersection;
import unimelb.farrugiulian.hexifence.board.features.Loop;
import unimelb.farrugiulian.hexifence.board.features.RichFeature;

public class EndgameExpert extends Agent {
	FeatureSet features;
	boolean locked = false;
	int isolatedShortChains;
	private QueueHashSet<Edge> freeScoring;
	private QueueHashSet<Edge> scoring;
	private QueueHashSet<Edge> safe;
	private HashMap<Edge, Integer> sacr;
	
	@Override
	public int init(int n, int p){
		int r = super.init(n, p);
	
		List<Edge> edges = Arrays.asList(board.getEdges());
		Collections.shuffle(edges);
		
		freeScoring = new QueueHashSet<Edge>();
		scoring = new QueueHashSet<Edge>();
		safe = new QueueHashSet<Edge>(edges);
		sacr = new HashMap<Edge, Integer>(); // um how is this gonna work
		
		return r;
	}

	@Override
	protected void update(Edge edge) {
		
		// remove this edge from play
		if (!freeScoring.remove(edge)) {
			if(!scoring.remove(edge)) {
				if(!safe.remove(edge)) {
					sacr.remove(edge);
				}
			}
		}
		
		// update all potentially-affected edges
		
		for(Cell cell : edge.getCells()){
			
			int n = cell.numFreeEdges();
			
			if(n == 2){
				for(Edge e : cell.getFreeEdges()){
					// these edges are no longer safe!
					if(safe.remove(e)){
						sacr.put(e, sacrificeSize(e));
					} else if (freeScoring.remove(e)) {
						// And if they were a free scoring edge, now they result in another
						// scoring edge
						scoring.add(e);
					}
				}
			} else if (n == 1){
				// these edges are no longer sacrifices, they're free!
				Edge e = cell.getFreeEdges()[0];
				if(sacr.remove(e) != null){
					// But what type of free? It depends on whether the other cell is
					// a sacrifice or not
					if (e.getOtherCell(cell) != null
							&& e.getOtherCell(cell).numFreeEdges() == 2) {
						scoring.add(e);
					} else {
						freeScoring.add(e);
					}
				} else if (scoring.remove(e)) {
					freeScoring.add(e);
				}
			}
		}
		
		if (!locked && safe.size() == 0) {
			features = new FeatureSet(board, super.piece);
			locked = true;
		} else if (features != null) {
			features.update(edge);
		}
	}

	@Override
	protected Edge getChoice() {
		String color = super.piece == Piece.BLUE ? "Blue" : "Red";
		// Free scoring cells are always safe to take
		if (freeScoring.size() > 0) {
			return freeScoring.remove();
		}
		
		// then select moves that are safe
		if(safe.size() > 0){
			return safe.remove();
		}
		
		// Do a search to find whether to double box or not
		if (features.numOpenFeatures() > 0) {
			
		}
		
		ArrayList<RichFeature> intersectedSacrifices = getIntersectedSacrifices(features);
		int numIntersectedSacrifices = intersectedSacrifices.size();
		// How do we open up long chains now?
		if (numIntersectedSacrifices == 0) {
			
		}
		
		// Do a search to find which intersected sacrifice to open
		RichFeature bestFeature;
		float bestValue;
		float testValue;
		bestValue = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < numIntersectedSacrifices; i++) {
			FeatureSet featuresTmp = new FeatureSet(features);
			takeFeature(getIntersectedSacrifices(featuresTmp).get(i));
			testValue = minimax(featuresTmp, false);
			if (testValue > bestValue) {
				bestValue = testValue;
				bestFeature = intersectedSacrifices.get(i);
			}
		}
		return bestFeature.openMove();
	}
	
	private float minimax(FeatureSet features, boolean max) {
		int numIntersectedSacrifices = numIntersectedSacrifices(features);
		if (numIntersectedSacrifices == 0) {
			// Simple parity evaluation right now
			int numSacrifices = numSacrifices(features);
			return max ^ ((numSacrifices % 2) == 0) ? 1 : 0;
		}
		
		float bestValue;
		if (max) {
			bestValue = Float.NEGATIVE_INFINITY;
			for (int i = 0; i < numIntersectedSacrifices; i++) {
				FeatureSet featuresTmp = new FeatureSet(features);
				takeFeature(getIntersectedSacrifices(featuresTmp).get(i));
				bestValue = Math.max(bestValue, minimax(featuresTmp, false));
			}
		} else {
			bestValue = Float.POSITIVE_INFINITY;
			for (int i = 0; i < numIntersectedSacrifices; i++) {
				FeatureSet featuresTmp = new FeatureSet(features);
				takeFeature(getIntersectedSacrifices(featuresTmp).get(i));
				bestValue = Math.min(bestValue, minimax(featuresTmp, true));
			}
		}
		
		return bestValue;
	}
	
	// Ideally gets the number of short chains attached to an intersection, where
	// this intersection is attached to another intersection by a short chain
	private int numIntersectedSacrifices(FeatureSet features) {
		return getIntersectedSacrifices(features).size();
	}
	
	// Ideally gets the short chains attached to an intersection, where this
	// intersection is attached to another intersection by a short chain
	private ArrayList<RichFeature> getIntersectedSacrifices(FeatureSet features) {
		Intersection[] intersections = features.getIntersections();
		ArrayList<RichFeature> sacrifices = new ArrayList<RichFeature>();
		for (Intersection intersection : intersections) {
			ArrayList<RichFeature> chains = intersection.getShortChains();
			boolean add = false;
			for (RichFeature chain : intersection.getShortChains()) {
				if (chain.numIntersections() == 2) {
					add = true;
				}
			}
			if (add) {
				for (RichFeature chain : chains) {
					if (!sacrifices.contains(chain)) {
						sacrifices.add(chain);
					}
				}
			}
		}
		return sacrifices;
	}
	
	// Ideally returns the number chains of length 2 or less and 3 clusters
	private int numSacrifices(FeatureSet features) {
		Intersection[] intersections = features.getIntersections();
		int numSacrifices = 0;
		for (Intersection intersection : intersections) {
			if (intersection.hasShortChain()) {
				numSacrifices++;
			}
		}
		return numSacrifices + features.numIsolatedSacrifices();
	}
	
	// Ideally opens and consumes a chain, effectively removing it from its
	// feature set
	private void takeFeature(RichFeature feature) {
		FeatureSet features = feature.getFeatureSet();
		feature.open();
		while(features.hasOpenFeatures()) {
			features.getOpenFeatures()[0].secureMove();
		}
	}

	private int sacrificeSize(Edge edge) {
		int size = 0;
		Stack<Edge> stack = new Stack<Edge>();
		
		board.place(edge, super.opponent);
		stack.push(edge);
		
		for(Cell cell : edge.getCells()){
			if (cell.numFreeEdges() != 0){
				size += sacrifice(cell, stack);
			}
		}
		
		while(!stack.isEmpty()){
			board.unplace(stack.pop());
		}
		
		return size;
	}

	private int sacrifice(Cell cell, Stack<Edge> stack){
		
		int n = cell.numFreeEdges();
		
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
}
