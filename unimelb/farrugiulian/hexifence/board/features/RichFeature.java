package unimelb.farrugiulian.hexifence.board.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

public abstract class RichFeature {
	
	private final FeatureSet fs;
	
	ArrayList<Cell> cells;
	ArrayList<Intersection> ints = new ArrayList<Intersection>(2);
	
	
	public RichFeature(Cell[] cells, FeatureSet fs){
		this.fs = fs;
		this.cells = new ArrayList<Cell>(Arrays.asList(cells));
	}
	
	public int length() {
		return cells.size();
	}
	
	public void addIntersection(Intersection intersection) {
		this.ints.add(intersection);
	}

	public boolean isolated(){
		return (ints.size() < 1);
	}
	
	public abstract FeatureSet open();
	/** bait only makes sense for a 2 chain! */
	public abstract FeatureSet bait();
	public abstract FeatureSet consume();
	public abstract FeatureSet doubleBox();
	
	public abstract Edge openingMove();
	public abstract Edge baitingMove();
	public abstract Stack<Edge> consumingMove();
	public abstract Stack<Edge> doubleBoxingMove();
}