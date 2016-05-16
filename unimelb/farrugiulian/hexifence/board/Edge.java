/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.board;

import aiproj.hexifence.Piece;

/**
 * This Edge class represents a hexifence board edhe, separating up to 2 cells
 * within a game grid. It contains methods for accessing its information and
 * that of its nearby cells
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran [juliant1] *
 **/
public class Edge extends Index {

  /** Separated cells **/
  private Cell[] cells = new Cell[2];
  /** the number of actual cells **/
  private int ncells = 0;

  /** Create a new, empty, unlinked edge at this position in the board **/
  public Edge(int i, int j) {
    super(i, j);
  }

  /** link a cell to this edge **/
  public void link(Cell cell) {
    this.cells[ncells++] = cell;
  }

  /** Place a piece on this edge **/
  public void place(int piece) {
    if (this.isEmpty() && piece != Piece.EMPTY) {

      // claim nearby pieces
      for (Cell cell : this.cells) {
        if (cell != null && cell.numEmptyEdges() == 1) {
          cell.color = piece;
        }
      }

      // and set ths piece
      this.color = piece;
    }
  }

  /** Remove the piece from this edge **/
  public void unplace() {
    if (color != Piece.EMPTY) {

      // un-claim nearby claimed pieces
      for (Cell cell : this.cells) {
        if (cell != null && cell.color != Piece.EMPTY) {
          cell.color = Piece.EMPTY;
        }
      }

      // and un-set ths piece
      this.color = Piece.EMPTY;
    }
  }

  /** Count the cells we would win if we placed a piece on this edge **/
  public int numCapturableCells() {

    // can't capture anything with this edge if it's already taken !!
    if (!this.isEmpty()) {
      return 0;
    }

    // otherwise, let's count the ones we'd capture if we placed here
    int n = 0;
    for (Cell cell : cells) {
      if (cell != null && cell.numEmptyEdges() == 1) {
        n++;
      }
    }
    return n;
  }

  /** Get an array of cells next to this edge **/
  public Cell[] getCells() {
    Cell[] cells = new Cell[this.ncells];
    for (int i = 0; i < ncells; i++) {
      cells[i] = this.cells[i];
    }
    return cells;
  }

  /**
   * Get the cell opposite from this cell across this edge (returning null if
   * this edge is on the side of the board)
   **/
  public Cell getOtherCell(Cell cell) {

    // side of board
    if (cells.length == 1) {
      return null;
    }

    // not side of board
    return (cells[0] == cell) ? cells[1] : cells[0];

  }
}
