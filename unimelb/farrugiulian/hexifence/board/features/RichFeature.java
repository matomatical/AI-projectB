package unimelb.farrugiulian.hexifence.board.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

public abstract class RichFeature {
	
	protected final FeatureSet fs;
	
	protected ArrayList<Cell> cells;
	protected ArrayList<Intersection> ints = new ArrayList<Intersection>(2);

	private boolean open = false; // ?? open loops become chains WAIT THEY BECOME DOUBLE OPEN CHAINS WOO

	
	public RichFeature(Cell[] cells, FeatureSet fs){
		this.fs = fs;
		this.cells = new ArrayList<Cell>(Arrays.asList(cells));
		
		for(Cell cell : cells){
			fs.map(cell, this);
		}
	}
	
	public RichFeature(RichFeature old){
		
		this.fs = old.fs;
		
		this.cells = new ArrayList<Cell>(old.cells);
	}

	public void addIntersection(Intersection intersection) {
		this.ints.add(intersection);
	}

	
	
	public int length() {
		return cells.size();
	}
	
	public ArrayList<Cell> getCells(){
		return this.cells;
	}

	public boolean isIsolated(){
		return (ints.size() < 1);
	}
	
	public boolean isOpen(){
		return this.open ;
	}
	
	public abstract void open();
	public abstract void bait(); // bait only makes sense for a 2 chain!
	public abstract void consume();
	public abstract void doubleBox();
	
	public abstract Edge openingMove();
	public abstract Edge baitingMove();
	public abstract Stack<Edge> consumingMove();
	public abstract Stack<Edge> doubleBoxingMove();
}