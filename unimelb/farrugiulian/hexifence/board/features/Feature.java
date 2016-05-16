package unimelb.farrugiulian.hexifence.board.features;

import java.util.ArrayList;

import com.matomatical.util.QueueHashSet;

import unimelb.farrugiulian.hexifence.board.Board;
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
	
	protected Feature(Feature that, FeatureSet fs) {
		
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

	private boolean isIsolated() {
		// return true if both ends have been added and are both null
		// this means consume has to keep ends up to date too !!
		return nends == 2 && ends[0] == null && ends[1] == null;
	}
	
	protected Cell[] getCells(){
		return cells.toArray(new Cell[cells.size()]);
	}
	
	protected Cell[] getEnds() {
		return ends;
	}

	/** Modifies a FeatureSet so that this feature is captured, and resulting
	 * changes to neighbouring features are made (e.g. longer chains forming at
	 * intersections).
	 * @param piece piece used to consume the chain (NOTE: not the piece
	 * doing the opening of the chain! the other one)
	 * @param boxing true for double-boxing (or double-double-boxing for loops)
	 * (has no effect if double boxing is not possible)
	 * @Example {@code myFourChain.consume(Piece.RED, true)}:
	 * will modify the feature set so that BLUE opens the chain, RED captures
	 * 2 cells and then double-boxes the last 2 cells leaving them for BLUE.
	 * (net advantage change is 0)
	 **/
	public void consume(int piece, boolean boxing){
		
		// well, make sure we're not dealing with an intersection
		if(this.type == Classification.INTERSECTION){
			System.err.println("Can't consume an intersection!");
			return;
		}
		
		// otherwise, there are still lots of cases to consider!		
		// first, remove this feature from the featureset!
		this.fs.remove(this);
		
		// now, if it's an isolated chain or loop, no other features care
		if(this.type == Classification.ISO_LOOP){
			if(boxing && this.length() > 3){
				// if we're boxing and this isn't a cluster, update the score
				this.fs.score(piece, this.length() - 4); // length - 4 for me
				this.fs.score(Board.other(piece),    4); // leaving 4 for you
			} else {
				this.fs.score(piece, this.length()); // all for me, thank you
			}
			return;
		} else if (this.type == Classification.CHAIN && this.isIsolated()){ // TODO: keep correct when we merge!
			if(boxing && this.length() > 1){
				// if we're boxing and this isn't a short chain, update score
				this.fs.score(piece, this.length() - 2); // length - 2 for me
				this.fs.score(Board.other(piece),    2); // leaving 2 for you
			} else {
				this.fs.score(piece, this.length()); // all for me, thank you
			}
			return;
		}
		
		// 
		
		
		
		
		
		
		
		
		
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
