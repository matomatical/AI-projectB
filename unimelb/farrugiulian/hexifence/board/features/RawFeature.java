package unimelb.farrugiulian.hexifence.board.features;

import java.util.LinkedHashSet;

import unimelb.farrugiulian.hexifence.board.Cell;

public class RawFeature {

	private Classification type;
	
	private LinkedHashSet<Cell> cells = new LinkedHashSet<Cell>();
	
	private int nends = 0;
	private Cell[] ends = new Cell[2];
	
	
	
	public RawFeature(Classification type){
		this.type = type;
	}
	
	public void classify(Classification type){
		this.type = type;
	}
	
	public Classification classification(){
		return this.type;
	}
	
	
	public boolean add(Cell cell) {
		return cells.add(cell);
	}

	public int length(){
		return cells.size();
	}
	
	public boolean end(Cell cell){
		ends[nends++] = cell; // may be null
		
		// return true if both ends have been added and are the same (non-isolated loop)
		return nends == 2 && ends[0] == ends[1] && ends[0] != null;
	}

	public Cell[] getCells(){
		return cells.toArray(new Cell[cells.size()]);
	}
	
	public Cell[] getEnds() {
		return ends;
	}
	
	public String toString(){
		return type.name() + " of length " + length() + ": " + cells.toString();
	}
}