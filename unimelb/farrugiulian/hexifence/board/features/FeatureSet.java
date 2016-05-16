/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.board.features;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import unimelb.farrugiulian.hexifence.board.*;

import unimelb.farrugiulian.hexifence.board.features.Feature.Classification;

/** FeatureSet is a class for creating, storing and associating Board Features
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran   [juliant1]
 */
public class FeatureSet {

	/** Collection of features on the board
	 *  Kept up-to-date through {@link Feature#consume Feature.consume()}
	 *  (though the actual board is never altered)
	 **/
	private ArrayList<Feature> features = new ArrayList<Feature>();
	
	/** Get the FeatureSet's Features
	 * @return the array of Feature objects that this FeatureSet is storing
	 * (NOT a copy!)
	 **/
	public ArrayList<Feature> getFeatures(){
		return features;
	}
	
	/** Get the feature with index i **/
	public Feature getFeature(int i){
		return features.get(i);
	}
	
	/** Is the FeatureSet empty?
	 * @return true iff the FeatureSet is out of features
	 **/
	public boolean isEmpty(){
		return features.isEmpty();
	}
	
	/** Positive advantage piece **/
	private int piece;
	/** How many points is this.piece ahead? **/
	private int advantage = 0;
	
	/** Get the difference in score from {@code piece}'s perspective
	 * @param piece the piece to look at the advantage as
	 * @return how many points ahead that piece is (and a negative number means
	 * they are actually behind!)
	 **/
	public int score(int piece){
		return advantage * ( (piece == this.piece) ? 1 : -1 );
	}
	/** Add some points to a piece's score and update the advantage
	 * @param piece the piece gaining these cells
	 * @param score the number of cells to add to that piece's score
	 **/
	public void score(int piece, int score){
		advantage += score * ((piece == this.piece) ? 1 : -1);
	}
	
	/** Map between cells and features **/
	private HashMap<Cell, Feature> map = new HashMap<Cell, Feature>();

	/** Map this cell to this feature for future reference **/
	protected void map(Cell cell, Feature feature){
		this.map.put(cell, feature);
	}

	/** Get the frature containing this cell, or null if it's not contained
	 * in any feature
	 **/
	protected Feature unmap(Cell cell){
		return this.map.get(cell);
	}

	/** Create a FeatureSet based on a board
	 * @param board A <b>post-lockdown</b> board to analyse for features 
	 * @param piece We just need one piece to know which way to store the
	 * advantage (doesn't actually make a difference)
	 **/
	public FeatureSet(Board board, int piece){
		
		this.piece = piece;
		
		// perform a specialised DFS over cells to construct our collection of
		// board features
		Cell[] cells = board.getCells();
		HashSet<Cell> visited = new HashSet<Cell>();
		
		for(Cell cell : cells){
			// if this cell has not already been visited,
			if(!visited.contains(cell)){
				// classify it!
				classify(cell, visited);
			}
		}
		
		// okay, now we have placed all of the cells in a feature, and while
		// doing so, linked each cell to its feature through this.map
		// and using this map, features can get their neighbouring features!
	}
	
	/** FeatureSet copy constructor
	 * Creates a copy of this featureset of limited depth: its features are new
	 * but still reference cells from the same underlying Board as the original
	 * @param fs the FeatureSet to copy
	 **/
	public FeatureSet(FeatureSet that){
		
		this.piece = that.piece;
		this.advantage = that.advantage;
		
		for(Feature feature : that.features){
			this.features.add(new Feature(feature, this));
			// takes care of adding the feature's cells to the new map too!
		}
		
		System.out.println("And the old Map:");
		for(Cell cell : that.map.keySet()){
			System.out.println(cell + " maps to " + that.map.get(cell));
		}
		System.out.println();
		
		System.out.println("And the new Map:");
		for(Cell cell : this.map.keySet()){
			System.out.println(cell + " maps to " + this.map.get(cell));
		}
		
		for(int i = 0; i < that.features.size(); i++){
			
			System.out.println("copied " + that.getFeature(i) + " connected to ");
			for(Feature f : that.getFeature(i).getFeatures()){
				System.out.println("--> " + f);
			}
			System.out.println("became " + this.getFeature(i) + " connected to ");
			for(Feature f : this.getFeature(i).getFeatures()){
				System.out.println("--> " + f);
			}
			
			System.out.println();
		}
	}
	
	/** Classification algorithm that reads a board starting from a cell and
	 *  constructing a feature to add to the feature set and map
	 * @param cell cell to begin the classification search at
	 * @param visited running HashSet of visited Cells to maintain
	 **/
	private void classify(Cell cell, HashSet<Cell> visited) {
		
		int n = cell.numEmptyEdges();
		
		// see if this cell has already been captured
		if(n == 0){
			// captured cell
			score(cell.getColor(), 1);
			return;
		}
		
		// otherwise, let's see if we can build a feature out of it!
		
		Feature feature;
		
		if(n == 1){
			// capturable cell, start an open chain
			// (it doesn't make sense for something that's ope not to be a chain)
			feature = new Feature(Classification.OPEN, this);
			feature.add(cell);
			explore(cell, null, visited, feature);
		
		} else if(n == 2){
			// part of a chain/loop/we don't know what yet
			feature = new Feature(Classification.CHAIN, this);
			
			// add the cell then explore
			feature.add(cell);
			explore(cell, null, visited, feature);
			
		} else {
			// intersection!
			feature = new Feature(Classification.INTERSECTION, this);
			feature.add(cell);
			
			// no need to explore!
		}
		
		features.add(feature);
	}

	/** Helper function for classification, recursively constructs and
	 *  classifies a feature based on the nature of the cells it encounters
	 * @param cell The next cell to recursively explore
	 * @param parent The previous cell (to avoid branching backwards) - null
	 *        at top level of recursion
	 * @param visited HashSet of visited cells to maintain
	 * @param feature Current Feature being constructed
	 */
	private void explore(Cell cell, Cell parent, HashSet<Cell> visited, Feature feature) {
		
		Edge[] edges = cell.getEmptyEdges();
		
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
				
			} else if (other.numEmptyEdges() == 1){
				// turns out this is an open chain! this is safe, in that it
				// will never be replaced by a later observation
				feature.classify(Classification.OPEN);
				feature.add(other);
				
			} else if(other.numEmptyEdges() > 2){
				// we've found an intersection!
				if(feature.end(other)){
					// this is a non-isolated loop!
					feature.classify(Classification.LOOP);
				}
				
				// should we add the intersection TO the loop? nah
			
			} else if (other.numEmptyEdges() == 2){
			
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

	/** Removes a feature from this featureset and its cells from the map
	 * @param feature the feature to remove
	 */
	protected void remove(Feature feature) {
		// TODO could do this faster ?
		features.remove(feature);
		
		// also need to remove all of its cells from the map
		for(Cell cell : feature.getCells()){
			map.remove(cell);
		}
		
		// done!
	}
}
