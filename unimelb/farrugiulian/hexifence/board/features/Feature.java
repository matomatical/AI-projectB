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
 * A board feature (for example, an intersection, chain or loop) for making
 * late-game decisions
 * 
 * @author Matt Farrugia
 * @author Julian Tran
 */
public class Feature {

  /** The possible types of classification a feature can have **/
  public enum Classification {
    CHAIN, ISO_LOOP, LOOP, INTERSECTION, OPEN
  }

  /** This feature's classification **/
  private Classification type;

  /** the cells that make up this feature **/
  private QueueHashSet<Cell> cells;

  /** the number of ends that have been added to this feature so far **/
  private int nends = 0;
  /** the cells marking the ends of this feature **/
  private Cell[] ends;

  /** the feature set that this feature belongs to **/
  private FeatureSet fs;

  /**
   * Create a new, empty Feature
   * 
   * @param type
   *          The classification of this feature
   * @param fs
   *          The FeatureSet it belongs to
   **/
  protected Feature(Classification type, FeatureSet fs) {
    this.type = type;
    this.fs = fs;

    this.cells = new QueueHashSet<Cell>();
    this.ends = new Cell[2];
  }

  /**
   * Create a new Feature based of an old one
   * 
   * @param that
   *          The old feature to copy
   * @param fs
   *          The feature set this new feature should belong to
   **/
  protected Feature(Feature that, FeatureSet fs) {
    // keep the old type
    this.type = that.type;

    // but use the new featureset and map!
    this.fs = fs;

    this.cells = new QueueHashSet<Cell>();
    for (Cell cell : that.cells) { // oh and copy the OLD cells not new ones
      this.add(cell); // takes care of adding the cells to the new map
    }

    // oh yeah, and keep the ends! deeply!
    this.nends = that.nends;
    this.ends = new Cell[2];
    for (int i = 0; i < 2; i++) {
      this.ends[i] = that.ends[i];
    }
  }

  /**
   * Set the classification of this feature
   * 
   * @param type
   *          to this classification
   **/
  protected void classify(Classification type) {
    this.type = type;
  }

  /**
   * Add a cell to this feature (automatically updates the feature map)
   * 
   * @param cell
   *          The cell to add
   * @return true iff this cell was not already inside this feature
   **/
  protected boolean add(Cell cell) {

    boolean added = cells.add(cell);

    this.fs.map(cell, this);

    return added;
  }

  /**
   * Add a new end to this feature
   * 
   * @param cell
   *          the cell marking the end of the feature (or null for side of
   *          board)
   * @return true if this (non-null) end has already been added (because both
   *         ends are now non-null and the same) marking an intersected loop
   **/
  protected boolean end(Cell cell) {
    ends[nends++] = cell; // may be null

    // return true if both ends have been added and are the same
    // (non-isolated loop)
    return nends == 2 && ends[0] == ends[1] && ends[0] != null;
  }

  /** Get an array of the cells in this feature **/
  protected Cell[] getCells() {
    return cells.toArray(new Cell[cells.size()]);
  }

  /**
   * Is this feature isolated?
   * 
   * @return true iff this feature is isolated (has no neighbouring features)
   **/
  private boolean isIsolated() {
    return this.getFeatures().isEmpty();
  }

  /**
   * Modifies a FeatureSet so that this feature is captured, and resulting
   * changes to neighbouring features are made (e.g. longer chains forming at
   * intersections).
   * 
   * @param piece
   *          piece used to consume the chain (NOTE: not the piece doing the
   *          opening of the chain! the other one)
   * 
   * @param boxing
   *          true for double-boxing (or double-double-boxing for loops) (has no
   *          effect if double boxing is not possible)
   * 
   * @Example {@code myFourChain.consume(Piece.RED, true)}: will modify the
   *          feature set so that BLUE opens the chain, RED captures 2 cells and
   *          then double-boxes the last 2 cells leaving them for BLUE. (net
   *          advantage change is 0)
   **/
  public void consume(int piece, boolean boxing) {

    // well, make sure we're not dealing with an intersection
    if (this.type == Classification.INTERSECTION) {
      System.err.println("Can't consume an intersection!");
      return;
    }

    // otherwise, there are still lots of cases to consider!
    // first thing's first, remove this feature from the featureset!
    this.fs.remove(this);

    // now, if it's an isolated chain or loop, no other features care,
    // we can just score it
    if (this.type == Classification.ISO_LOOP) {
      if (boxing && this.length() > 3) {
        // if we're boxing and this isn't a cluster, update the score
        this.fs.score(piece, this.length() - 4); // length - 4 for me
        this.fs.score(Board.other(piece), 4); // leaving 4 for you
      } else {
        this.fs.score(piece, this.length()); // all for me, thank you
      }
      return; // all done!
    } else if (this.type == Classification.CHAIN && this.isIsolated()) {

      if (boxing && this.length() > 1) {
        // if we're boxing and this isn't a short chain, update score
        this.fs.score(piece, this.length() - 2); // length - 2 for me
        this.fs.score(Board.other(piece), 2); // leaving 2 for you
      } else {
        this.fs.score(piece, this.length()); // all for me, thank you
      }
      return; // all done!
    }

    // okay, so far so good, but what if it's an intersecting feature!?
    // it may be okay, if the intersections have enough remaining chains
    // we'll have to count them to see

    // no matter what, we're going to score this feature like a chain
    // (since it's not an isoloop)

    if (boxing && this.length() > 1) {
      // if we're boxing and this isn't a short chain, update score
      this.fs.score(piece, this.length() - 2); // length - 2 for me
      this.fs.score(Board.other(piece), 2); // leaving 2 for you
    } else {
      this.fs.score(piece, this.length()); // all for me, thank you
    }

    // now, for each intersection coming off it, lets analyse them to
    // see if further changes need to be made
    for (Feature intersection : this.getFeatures()) {

      // get the features in this intersection
      ArrayList<Feature> features = intersection.getFeatures();

      if (features.size() > 2) {
        // this intersection is definitely still in tact! it's actually
        // as if we were an isolated chain!

        // nothing more to do here on this side...
        continue;

      } else if (features.size() == 2) {
        // if either feature is a loop, we're still in tact!

        boolean loops = false;
        for (Feature feature : features) {
          if (feature.classification() == Classification.LOOP) {
            loops = true; // found one!
          }
        }
        if (loops) {
          // nothing more to do on this side!
          continue;
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
        for (int i = 0; i < a.nends; i++) {
          if (a.ends[i] != intersection.cells.element()) {
            // found the right and of a!
            for (int j = 0; j < b.nends; j++) {
              if (b.ends[j] == intersection.cells.element()) {
                // found the right end of b!
                b.ends[j] = a.ends[i];
              }
            }
          }
        }

        // add a and intersection's cells to b
        for (Cell cell : a.getCells()) {
          b.add(cell);
        }
        b.add(intersection.cells.element());

        // it's also possible that this created a loop!
        if (b.ends[0] == b.ends[1] && b.ends[0] != null) {
          b.classify(Classification.LOOP);
        }

      } else if (features.size() < 2) {
        Feature last = features.get(0);

        // either way, this intersection is no longer an intersection
        this.fs.remove(intersection);

        if (last.classification() == Classification.LOOP) {
          // if this feature is a loop, we have to add the
          // final cell to it and turn it into an iso loop

          last.add(intersection.cells.element());
          last.classify(Classification.ISO_LOOP);

          // also wipe this loop's ends
          last.nends = 0;

        } else if (last.classification() == Classification.CHAIN) {
          // otherwise, if it's a chain, we should be consuming
          // it, too!

          last.add(intersection.cells.element());

          // wipe this end from the chain
          for (int i = 0; i < last.nends; i++) {
            if (last.ends[i] == intersection.cells.element()) {
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

  /**
   * Selecting a feature for opening by returning an Edge that can be used to
   * open it.
   * 
   * @param baiting
   *          True if you would like to return an edge that offers the oponent a
   *          chance to double box or false for you would like to prevent double
   *          boxing (actually, that only works for two-chains
   * @return an edge to play to open this feature, or null if this feature is an
   *         intersection.
   **/
  public Edge choose(boolean baiting) {

    if (this.type == Classification.INTERSECTION) {
      System.err.println("Choose on an intersection! Return null.");
      return null;

    }

    if (this.length() == 2) {

      // if this is length two, either of its cells will contain a secure
      // and a baiting edge
      Cell cell = this.cells.element();

      // iterate through the two edges and return the correct one
      for (Edge edge : cell.getEmptyEdges()) {
        // if we're baiting and this is an edge edge or if we're
        // not baiting and this is an inside egde (non edge edge)
        if (baiting ^ (this == fs.unmap(edge.getOtherCell(cell)))) {
          return edge;
        }
      }
    }

    // if we get there then we are not length 2 and we can't really
    // make this choice, return any old opening edge
    return this.cells.element().getEmptyEdges()[0];
  }

  /**
   * Get this feature's neighbouring features
   * 
   * @return An ArrayList of Features connected to this Feature
   **/
  public ArrayList<Feature> getFeatures() {

    // case 1: it's an intersection, it must have some features nearby
    if (type == Classification.INTERSECTION) {

      // this is an intersection! returns its neighbouring features
      Cell cell = cells.element();

      ArrayList<Feature> features = new ArrayList<Feature>();

      for (Edge edge : cell.getEmptyEdges()) {
        Cell other = edge.getOtherCell(cell);
        // make sure we're not on the side of the board (shouldn't
        // happen because we're assuming lockdown and these side edges
        // would be safe)
        if (other != null) {
          Feature f = this.fs.unmap(other);
          // make sure this feature still exists in the feature set
          if (f != null) {
            // unless we're thinking of adding a loop thats already
            // in, add this feature
            if (f.classification() != Classification.LOOP
                || !features.contains(f)) {
              features.add(f);
            }
          }
        }
      }

      return features;

      // case 2: it's not an intersection, it may have intersections nearby
    } else {
      // this is not an intersection! return its non-null ending features

      ArrayList<Feature> features = new ArrayList<Feature>();

      for (Cell end : ends) {
        if (end != null) {
          Feature f = this.fs.unmap(end);

          // unless we're a loop and this intersection is already in,
          // add this feature
          if (this.classification() != Classification.LOOP
              || !features.contains(f)) {
            features.add(f);
          }
        }
      }

      return features;
    }
  }

  /** The classification of this feature **/
  public Classification classification() {
    return this.type;
  }

  /** The length of this feature (in cells) **/
  public int length() {
    return cells.size();
  }

  /** Format this feature's classification, length and contained cells **/
  @Override
  public String toString() {
    return type.name() + " of length " + length() + ": "
        + cells.toString();
  }
}
