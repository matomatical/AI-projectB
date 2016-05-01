/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                   Project A - Testing the Current State                   *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 03/04/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.agent;

import unimelb.farrugiulian.hexifence.board.Board;
import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;
import unimelb.farrugiulian.hexifence.board.boardreading.AsciiBoardReader;
import unimelb.farrugiulian.hexifence.board.boardreading.BoardReader;
import unimelb.farrugiulian.hexifence.board.boardreading.InvalidInputException;

/**
 * Simple Agent class to drive our project A submission. Adapted for project B
 * changes builds a board based on an input file and then inspects it to find
 * required information about the state of that board.
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran [juliant1]
 */
public class AgentA {

  /**
   * Driver program. Creates board, initialises it with input from System.in and
   * then inspects it to display required information.
   * 
   * @param args
   *          command line arguments (unused - input should come from System.in)
   */
  public static void main(String[] args) {

    // First, get the first line, which specifies board dimension

    BoardReader br = new AsciiBoardReader();

    Board board = null;
    try {
      board = br.makeBoard(null);
    } catch (InvalidInputException e) {
      System.out.println(e.getMessage());
      System.exit(0);
    }

    // Then, inspect the board to find and print the required information

    System.out.println(numMoves(board));

    System.out.println(mostCapturesOneMove(board));

    System.out.println(numCapturableCells(board));
  }

  /**
   * Calculates the number of edges available for placing a piece
   * 
   * @param board
   *          the board to inspect
   * @return the number of available pieces
   */
  private static int numMoves(Board board) {

    // each free edge represents a possible move
    return board.numFreeEdges();
  }

  /**
   * Calculates the maximum number of cell captures that could take place due to
   * the selection of any free edge on the current board
   * 
   * @param board
   *          the board to analyse
   * @return the maximum number of cell captures available in one turn
   */
  private static int mostCapturesOneMove(Board board) {

    int mostSoFar = 0, current;

    // consider each free edge,
    for (Edge e : board.getFreeEdges()) {

      // how many captures would it give?
      current = e.numCapturableCells();

      // keep track of the highest result
      if (current > mostSoFar) {
        mostSoFar = current;
      }
    }

    return mostSoFar;
  }

  /**
   * Calculates the number of cells that are one edge away from capture, and
   * could therefore be captured in the next move
   * 
   * @param board
   *          the board to analyse
   * @return the number of cells that are immediately available for capture
   */
  private static int numCapturableCells(Board board) {

    int n = 0;

    // consider each cell,
    for (Cell c : board.getCells()) {

      // is this cell captureable? (does it have only 1 free edge?)
      if (c.numFreeEdges() == 1) {
        n++;
      }
    }

    return n;
  }
}
