/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.board;

import java.io.PrintStream;

import com.matomatical.hexifence.visual.VisualBoard;

import aiproj.hexifence.Piece;

/**
 * A Hexifence Game Board, used for storing the state of the game and also
 * trying different scenarios
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran [juliant1]
 */
public class Board implements VisualBoard {

  /** The dimension of the game board (also referred to as radius) */
  public final int dimension;
  /** the width of the board grid */
  public final int width;
  // (store BOTH width AND dimension as we will use both of them frequently)

  /** the array storing the Cells and Edges that make up the board */
  private Index[][] grid;

  /**
   * Create, initialise an empty board
   * 
   * @param dimension
   *          the dimension (radius) of the board in cells
   */
  public Board(int dimension) {

    this.dimension = dimension;
    this.width = 4 * dimension - 1;

    // initialise the board grid

    this.grid = new Index[width][width];

    // for each row that has cells
    for (int i = 1; i < width; i += 2) {
      // for each cell in that row
      for (int j = 1 + Integer.max(0, i - (width / 2)); j < Integer
          .min(width, i + (width / 2)); j += 2) {
        // (only touches odd,odd squares in valid part of board)

        // make a new cell at this index
        Cell cell = new Cell(i, j);
        grid[i][j] = cell;

        // create surrounding edges
        int[] is = { i, i + 1, i + 1, i, i - 1, i - 1 };
        int[] js = { j - 1, j, j + 1, j + 1, j, j - 1 };

        for (int k = 0; k < 6; k++) {
          Edge edge = (Edge) grid[is[k]][js[k]];
          if (edge == null) {
            // oops! haven't created this edge yet!
            edge = new Edge(is[k], js[k]);
            grid[is[k]][js[k]] = edge;
          }

          // link this cell and this edge
          edge.link(cell);
          cell.link(edge);
        }
      }
    }
  }

  /**
   * true iff this edge represents a valid edge or cell, as long as it's only
   * called after the board grid has been properly initialised
   **/
  public boolean isValid(int i, int j) {
    return (grid[i][j] != null);
  }

  /** true iff these indices point at a cell **/
  public boolean isCell(int i, int j) {
    return i % 2 == 1 && j % 2 == 1;
  }

  /** true iff these indices point at an edge **/
  public boolean isEdge(int i, int j) {
    return isValid(i, j) && !isCell(i, j);
  }

  /**
   * On the edge at this index, place this piece
   * 
   * @throws NullPointerException
   *           if this index does not refer to an edge
   **/
  public void place(int i, int j, int piece) {
    getEdge(i, j).place(piece);
  }

  /** On this edge, place this piece **/
  public void place(Edge edge, int piece) {
    edge.place(piece);
  }

  /**
   * Unplace the piece on the edge at this index
   * 
   * @throws NullPointerException
   *           if this index does not refer to an edge
   **/
  public void unplace(int i, int j) {
    getEdge(i, j).unplace();
  }

  /** Unplace the piece on this edge **/
  public void unplace(Edge edge) {
    edge.unplace();
  }

  /**
   * Get a cell specified by specific indices
   * 
   * @param i
   *          the row index
   * @param j
   *          the column index
   * @return the cell at i, j in the grid, or null if i, j is not a cell
   */
  public Cell getCell(int i, int j) {
    if (grid[i][j] instanceof Cell) {
      return (Cell) grid[i][j];
    } else {
      return null;
    }
  }

  /**
   * Get an edge specified by specific indices
   * 
   * @param i
   *          the row index
   * @param j
   *          the column index
   * @return the edge at i, j in the grid, or null if i, j is not a edge
   */
  public Edge getEdge(int i, int j) {
    if (grid[i][j] instanceof Edge) {
      return (Edge) grid[i][j];
    } else {
      return null;
    }
  }

  /**
   * Count how many cells there are
   * 
   * @return the number of cells on the board
   */
  public int numCells() {
    // the number of cells is 3n(n-1)+1
    // (en.wikipedia.org/wiki/Centered_hexagonal_number)
    return 3 * dimension * (dimension - 1) + 1;
  }

  /**
   * Generates an array of all the cells on the board
   * 
   * @return an array of all the boards' cells
   */
  public Cell[] getCells() {

    Cell[] cells = new Cell[numCells()];
    int n = 0;

    // for each row with cells
    for (int i = 1; i < width; i += 2) {
      // for each cell in that row
      for (int j = 1 + Integer.max(0, i - width / 2); j < Integer
          .min(width, i + width / 2); j += 2) {

        cells[n++] = getCell(i, j);
      }
    }

    return cells;
  }

  /**
   * Count how many edges there are
   * 
   * @return the number of edges on the board
   */
  public int numEdges() {
    // the number of edges is n(9n-3) (I did the math)
    return this.dimension * (9 * this.dimension - 3);
  }

  /**
   * Generates an array of all the baord's edges
   * 
   * @return array of all the edges from the board
   */
  public Edge[] getEdges() {

    Edge[] edges = new Edge[numEdges()];
    int n = 0;

    // for each row
    for (int i = 0; i < width; i++) {
      // for each EDGE in that row
      // (loop start and loop guard skip non-board grid squares, while
      // loop increment skips cells too but only if we're on an odd row)
      for (int j = Integer.max(0, i - (width / 2)); j < Integer.min(width,
          1 + i + (width / 2)); j += 1 + (i % 2)) {

        edges[n++] = getEdge(i, j);
      }
    }

    return edges;
  }

  /**
   * Count how many edges have not been taken
   * 
   * @return the number of edges that are empty
   */
  public int numEmptyEdges() {
    int n = 0;

    // loop through all edges
    for (Edge e : getEdges()) {
      if (e.isEmpty()) {
        n++; // count empty ones
      }
    }
    return n;
  }

  /**
   * Generate an array of edges on the board that have not been placed on
   * 
   * @return an array of all the empty edges from the board
   */
  public Edge[] getEmptyEdges() {
    // we'll have to count the number of empty edges
    Edge[] freeEdges = new Edge[numEmptyEdges()];

    int n = 0;

    for (Edge e : getEdges()) {
      if (e.isEmpty()) {
        // add empty edges to output buffer
        freeEdges[n++] = e;
      }
    }

    return freeEdges;
  }

  /** Print this board to an output PrintStream **/
  public void printTo(PrintStream out) {
    int color;
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < width; j++) {
        if (!this.isValid(i, j)) {
          out.print("-");
        } else if (this.isCell(i, j)) {
          color = this.getCell(i, j).color;
          if (color == Piece.EMPTY) {
            out.print("-");
          } else if (color == Piece.RED) {
            out.print("r");
          } else {
            out.print("b");
          }
        } else {
          color = this.getEdge(i, j).color;
          if (color == Piece.EMPTY) {
            out.print("+");
          } else if (color == Piece.RED) {
            out.print("R");
          } else {
            out.print("B");
          }
        }
        if (j < width - 1) {
          out.print(" ");
        }
      }
      out.println();
    }
  }

  /**
   * Print this board plus surrounding grid indices. Will fail to print properly
   * if board's width is > 99
   **/
  public void printIndexedTo(PrintStream out) {

    // doesnt work for width > 99

    if (width < 10) {
      out.print("  ");
    } else {
      out.print("   ");
    }

    for (int i = 0; i < width; i++) {

      out.print(i);

      if (i < 10 && i < width - 1) {
        out.print(" ");
      }
    }

    for (int i = 0; i < width; i++) {

      if (width < 10) {
        out.print(i + " ");
      } else if (i < 10) {
        out.print(" " + i + " ");
      } else {
        out.print(i + " ");
      }

      for (int j = 0; j < width; j++) {
        if (!this.isValid(i, j)) {
          out.print("-");
        } else if (this.isCell(i, j)) {
          int color = this.getCell(i, j).color;
          if (color == Piece.EMPTY) {
            out.print("-");
          } else if (color == Piece.RED) {
            out.print("r");
          } else {
            out.print("b");
          }
        } else {
          int color = this.getEdge(i, j).color;
          if (color == Piece.EMPTY) {
            out.print("+");
          } else if (color == Piece.RED) {
            out.print("R");
          } else {
            out.print("B");
          }
        }
        if (j < width - 1) {
          out.print(" ");
        }
      }
      out.println();
    }
  }

  @Override
  public int getColor(int i, int j) {
    if (grid[i][j] != null) {
      return grid[i][j].color;
    } else {
      return Piece.INVALID;
    }
  }

  /** Static helper functions for easily working with Piece interface **/

  /** Get the opposing player's piece according to {@link Piece} **/
  public static int other(int piece) {
    return (piece == Piece.RED) ? Piece.BLUE : Piece.RED;
  }

  /** Get the color name of this piece **/
  public static String name(int piece) {
    return (piece == Piece.RED) ? "RED" : "BLUE";
  }
}
