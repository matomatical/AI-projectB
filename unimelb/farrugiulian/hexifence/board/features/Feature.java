package unimelb.farrugiulian.hexifence.board.features;

import java.util.ArrayList;

import com.matomatical.util.QueueHashSet;

import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

public class Feature {

	public enum Classification{
		CHAIN, ISO_LOOP, LOOP, INTERSECTION, OPEN
	}
	private Classification type;
	
	private QueueHashSet<Cell> cells = new QueueHashSet<Cell>();
	
	private int nends = 0;
	private Cell[] ends = new Cell[2];

	private FeatureSet fs;
	
	protected Feature(Classification type, FeatureSet fs){
		this.type = type;
		this.fs = fs;
	}
	
	public Feature(Feature that, FeatureSet fs) {
		
		// keep the old type
		this.type = that.type;
		
		// but use the new featureset! ;)
		this.fs = fs;
		
		for(Cell cell : this.cells){
			this.add(cell); // takes care of adding the cells to the new map
		}
	}

	protected void classify(Classification type){
		this.type = type;
	}
	
	protected boolean add(Cell cell){
		
		if(cells.add(cell)){
			this.fs.map(cell, this);
			return true;
		}
		
		return false;
	}
	
	protected boolean end(Cell cell){
		ends[nends++] = cell; // may be null
		
		// return true if both ends have been added and are the same
		// (non-isolated loop)
		return nends == 2 && ends[0] == ends[1] && ends[0] != null;
	}

	protected Cell[] getCells(){
		return cells.toArray(new Cell[cells.size()]);
	}
	
	protected Cell[] getEnds() {
		return ends;
	}

	
	
	public void consume(){
		
	}
	
	/**
	 * @param the 
	 * @return the best edge to play
	 **/
	public Edge choose(boolean baiting){
		// for NOW we're just returning ANY old edge to open this feature
		// but later we'll be a little more careful ;)
		return this.cells.element().getEmptyEdges()[0];
	}
	
	/** 
	 * @return An ArrayList of Features
	 **/
	public ArrayList<Feature> getFeatures(){
		if(type == Classification.INTERSECTION){
			// this is an intersection! returns its neighbouring features
			Cell cell = cells.element();
			
			ArrayList<Feature> features = new ArrayList<Feature>();
			
			for(Edge edge : cell.getEmptyEdges()){
				Cell other = edge.getOtherCell(cell);
				if(other != null){
					Feature f = this.fs.unmap(other);
					
					// unless we're thinking of adding a loop thats already in,
					// add this feature
					if(f.classification() != Classification.LOOP
							|| ! features.contains(f)){
						features.add(f);
					}
				}
			}
			
			return features;
			
		} else {
			// this is not an intersection! return its non-null ending features
			
			ArrayList<Feature> features = new ArrayList<Feature>();
			
			for(Cell end : ends){
				if(end != null){
					Feature f = this.fs.unmap(end);
					
					// unless we're a loop and this intersection is already in,
					// add this feature
					if(this.classification() != Classification.LOOP
							|| ! features.contains(f)){
						features.add(f);
					}
				}
			}
			
			return features;
		}
	}

	public Classification classification(){
		return this.type;
	}

	public int length(){
		return cells.size();
	}
	
	public String toString(){
		return type.name() +" of length "+ length() +": "+ cells.toString();
	}
}
