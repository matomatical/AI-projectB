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
		
		isolatedShortChains = numIsolatedSacrifices(); // For calculating parity
		
		// Do a search to find whether to double box or not
		if (features.getOpenFeatures().size() > 0) {
			
		}
		
		// How do we open up long chains now?
		if (numIntersectedSacrifices() == 0) {
			
		}
		
		// Do a search to find which intersected sacrifice to open
		RichFeature bestFeature;
		float bestValue;
		float testValue;
		bestValue = Float.NEGATIVE_INFINITY;
		for (RichFeature feature : getIntersectedSacrifices()) {
			feature.open();
			testValue = minimax(false);
			if (testValue > bestValue) {
				bestValue = testValue;
				bestFeature = feature;
			}
			features.rewind();
		}
		return bestFeature.openMove();
	}
	
	private float minimax(boolean max) {
		if (numIntersectedSacrifices() == 0) {
			// Simple parity evaluation right now, not sure if actually correct
			return (((max ? 0 : 1) + isolatedShortChains) % 2 == 0 ? 0 : 1);
		}
		
		features.getOpenFeatures[0].consume();
		
		float bestValue;
		if (max) {
			bestValue = Float.NEGATIVE_INFINITY;
			for (RichFeature feature : getIntersectedSacrifices()) {
				feature.open();
				bestValue = Math.max(bestValue, minimax(true));
				features.rewind();
			}
		} else {
			bestValue = Float.POSITIVE_INFINITY;
			for (RichFeature feature : getIntersectedSacrifices()) {
				feature.open();
				bestValue = Math.min(bestValue, minimax(false));
				features.rewind();
			}
		}
		
		features.rewind();
		
		return bestValue;
	}
	
	private int numIsolatedSacrifices() {
		return features.numIsolatedShortChains() + features.numIsolatedClusters();
	}
	
	private int numIntersectedSacrifices() {
		return features.numIntersectedShortChains() + features.numIntersectedClusters();
	}
	
	private ArrayList<RichFeature> getIntersectedSacrifices() {
		return features.getIntersectedShortChains().addAll(features.getIntersectedClusters());
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
