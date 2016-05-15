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

	
	/** e.g. 4-chain.consume(Piece.RED, true)
	 * will modify the feature set so that BLUE opens the chain, RED scores two
	 * points and then double-boxes the last 2 cells leaving them for BLUE
	 * (net advantage change is 0)
	 **/
	public void consume(int piece, boolean boxing){
		
		// well, make sure we're not dealing with an intersection
		if(this.type == Classification.INTERSECTION){
			System.err.println("Can't consume an intersection!");
			return;
		}
		
		if(this.type == Classification.ISO_LOOP){
			// fs.remove(this);
			
		}
		
		if(this.type == Classification.CHAIN){
			// consuming a chain
		}
	}
	
	/** Selecting a feature for opening by returning an Edge that can be used
	 *  to open it. 
	 *  @param baiting True if you would like to return an edge that offers the
	 *  oponent a chance to double box or false for you would like to prevent
	 *  double boxing (actually, that only works for two-chains
	 *  @return an edge to play to open this feature, or null if this feature
	 *  is an intersection.
	 **/
	public Edge choose(boolean baiting){
		
		if(this.type == Classification.INTERSECTION){
			return null;
			
		} else {
			if(baiting){
				
			} else {
				
			}
			// for NOW we're just returning ANY old edge to open this feature
			// but later we'll be a little more careful ;)	
			return this.cells.element().getEmptyEdges()[0];
		}		
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
