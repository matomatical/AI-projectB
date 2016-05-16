/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.board.features;

import java.util.ArrayList;

import com.matomatical.util.QueueHashSet;

import unimelb.farrugiulian.hexifence.board.Board;
import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

/**
 * 
 * @author Matt Farrugia
 * @author Julian Tran
 *
 */
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
	
	protected Cell[] getCells(){
		return cells.toArray(new Cell[cells.size()]);
	}
	
	protected Cell[] getEnds() {
		return ends;
	}

	private boolean isIsolated() {
		return this.getFeatures().isEmpty();
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
		} else if (this.type == Classification.CHAIN && this.isIsolated()){
			
			if(boxing && this.length() > 1){
				// if we're boxing and this isn't a short chain, update score
				this.fs.score(piece, this.length() - 2); // length - 2 for me
				this.fs.score(Board.other(piece),    2); // leaving 2 for you
			} else {
				this.fs.score(piece, this.length()); // all for me, thank you
			}
			return;
		}
		
		// okay, so far so good, but what if it's an intersecting feature!?
		// it may be okay, if the intersections have enough remaining chains...
		// no matter what, we're going to score this feature like a chain
		// (since it's not an isoloop)
		
		if(boxing && this.length() > 1){
			// if we're boxing and this isn't a short chain, update score
			this.fs.score(piece, this.length() - 2); // length - 2 for me
			this.fs.score(Board.other(piece),    2); // leaving 2 for you
		} else {
			this.fs.score(piece, this.length()); // all for me, thank you
		}

		// now, for each intersection coming off it,
		for(Feature intersection : this.getFeatures()){
			// analyse this intersection's remaining features to decide if
			// further changes need to be made!
			ArrayList<Feature> features = intersection.getFeatures();
			
			if(features.size() > 2){
				// this intersection is definitely still in tact! it's actually
				// as if we were an isolated chain!
				// nothing more to do here...
				return;
				
			} else if (features.size() == 2){
				// if either feature is a loop, we're still in tact!
				for(Feature feature : features){
					if(feature.classification() == Classification.LOOP){
						// we're still good!
						
						return;
					}
				}
				
				// if we make it to here, there are no loops! we're looking at
				// two chains that need to be merged
				Feature a = features.get(0);
				Feature b = features.get(1);

				// remove a and intersection from the set
				this.fs.remove(intersection);
				this.fs.remove(a);
				
				// be careful with ends!!!
				// set b's end which IS intersection to a's end which IS NOT				
				for(int i = 0; i < a.nends; i++){
					if(a.ends[i] != intersection.cells.element()){
						// found the right and of a!
						for(int j = 0; j < b.nends; j++){
							if(b.ends[j] == intersection.cells.element()){
								// found the right end of b!
								b.ends[j] = a.ends[i];
							}
						}
					}
				}
				
				// add a and intersection's cells to b
				for(Cell cell : a.getCells()){
					b.add(cell);
				}
				b.add(intersection.cells.element());
				
				
			} else if (features.size() < 2){
				Feature last = features.get(0);
				
				// either way, this intersection is no longer an intersection
				this.fs.remove(intersection);
				
				if(last.classification() == Classification.LOOP){
					// if this feature is a loop, we have to add the
					// final cell to it and turn it into an iso loop
					
					last.add(intersection.cells.element());
					last.classify(Classification.ISO_LOOP);
					// also wipe this loop's ends
					last.nends = 0;
					
				} else if(last.classification() == Classification.CHAIN){
					// otherwise, if it's a chain, we should be consuming
					// it, too!
					
					last.add(intersection.cells.element());
					
					// wipe this end from the chain
					for(int i = 0; i < last.nends; i++){
						if(last.ends[i] == intersection.cells.element()){
							last.ends[i] = null;
						}
					}
					
					// we've already accounted for double boxing, so lets
					// make sure we grab all of these!
					last.consume(piece, false);
				} else {
					// not sure what to do here!
					System.err.println("I don't know how to consume " + this);
				}
			}
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
	 * @return An ArrayList of Features connected to this Feature
	 **/
	public ArrayList<Feature> getFeatures(){
		if(type == Classification.INTERSECTION){
			// this is an intersection! returns its neighbouring features
			Cell cell = cells.element();
			
			ArrayList<Feature> features = new ArrayList<Feature>();
			
			for(Edge edge : cell.getEmptyEdges()){
				Cell other = edge.getOtherCell(cell);
				// make sure we're not on the side of the board (shouldn't
				// happen because we're assuming lockdown)
				if(other != null){
					Feature f = this.fs.unmap(other);
					// make sure this feature still exists in the feature set
					if(f != null){
						// unless we're thinking of adding a loop thats already in,
						// add this feature
						if(f.classification() != Classification.LOOP
								|| ! features.contains(f)){
							features.add(f);
						}
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
