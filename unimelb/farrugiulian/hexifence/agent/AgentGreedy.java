/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 2/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.agent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import com.matomatical.util.QueueHashSet;

import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

/**
 * This Agent plays the game with a Greedy heuristic; first capturing any
 * available cells, before choosing a random safe cell if one exists, before
 * offering the opponent the chance to score the fewest possible cells in its
 * next turn (by counting the cells captureable by each edge choice)
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julain Tran [juliant1]
 **/
public class AgentGreedy extends Agent {

  /** Collection of edges representing captureable cells **/
  private QueueHashSet<Edge> free;
  /** Collection of edges representing captureable cells **/
  private QueueHashSet<Edge> safe;
  /** Collection of edges representing captureable cells **/
  private QueueHashSet<Edge> sacr;

  /** Initialise the superclass and also the specifics for this class **/
  @Override
  public int init(int n, int p) {
    int r = super.init(n, p);

    long seed = System.nanoTime();
    List<Edge> edges = Arrays.asList(board.getEdges());
    Collections.shuffle(edges, new Random(seed));

    // System.err.println("Greedy seed:" + seed);

    // all edges are safe on an empty board
    safe = new QueueHashSet<Edge>(edges);

    free = new QueueHashSet<Edge>();
    sacr = new QueueHashSet<Edge>();

    return r;
  }

  @Override
  protected void update(Edge edge) {

    // remove this edge from play
    if (!free.remove(edge)) {
      if (!safe.remove(edge)) {
        sacr.remove(edge);
      }
    }

    // upate all potentially-affected edges

    for (Cell cell : edge.getCells()) {

      int n = cell.numEmptyEdges();

      if (n == 2) {
        // these edges are no longer safe!
        for (Edge e : cell.getEmptyEdges()) {
          if (safe.remove(e)) {
            sacr.add(e);
          }
        }

      } else if (n == 1) {
        // these edges are no longer sacrifices, they're free!
        for (Edge e : cell.getEmptyEdges()) {
          if (sacr.remove(e)) {
            free.add(e);
          }
        }
      }
    }
  }

  @Override
  public Edge getChoice() {

    // selec moves that capture a cell

    if (free.size() > 0) {
      return free.remove();
    }

    // then select moves that are safe

    if (safe.size() > 0) {
      return safe.remove();
    }

    // then and only then, select a move that will lead to the smallest
    // available sacrifice

    Edge[] edges = board.getEmptyEdges();

    // find the best option

    Edge bestEdge = edges[0];
    int bestCost = sacrificeSize(edges[0]);

    for (int i = 1; i < edges.length; i++) {
      Edge edge = edges[i];
      int cost = sacrificeSize(edge);
      if (cost < bestCost) {
        bestEdge = edge;
        bestCost = cost;
      }
    }

    return bestEdge;
  }

  /**
   * Helper function to calcualte the size of the sacrifice made when claiming
   * an edge
   * 
   * @param edge
   *          the edge to claim to give this sacrifice
   * @return the maximum number of cells that the opponent will be able to
   *         capture as a result
   */
  private int sacrificeSize(Edge edge) {

    // keep track of which edges we have placed
    Stack<Edge> stack = new Stack<Edge>();

    // place the first edge
    board.place(edge, super.opponent);
    stack.push(edge);

    // count the consequences
    int size = 0;
    for (Cell cell : edge.getCells()) {
      if (cell.numEmptyEdges() != 0) {
        size += sacrifice(cell, stack);
      }
    }

    // unplace all of the edges that were placed to restore the baord!
    while (!stack.isEmpty()) {
      board.unplace(stack.pop());
    }

    // you know, at this point we could probably cleverly keep all of these
    // edges up-to-date counts on their sacrifice size but there's no point
    // now!
    return size;
  }

  /**
   * temporarily greedily claim everything made available by a sacrifice
   * counting the number of claims and
   * 
   * @param cell
   *          the cell to start searching from
   * @param stack
   *          the stack of edges placed for unwinding later
   * @return the number of cells claimed down this path
   */
  private int sacrifice(Cell cell, Stack<Edge> stack) {

    int n = cell.numEmptyEdges();

    if (n > 1) {
      // this cell is not available for capture
      return 0;

    } else if (n == 1) {
      for (Edge edge : cell.getEdges()) {
        if (edge.isEmpty()) {

          // claim this piece
          edge.place(super.opponent);
          stack.push(edge);

          // follow opposite cell
          Cell other = edge.getOtherCell(cell);

          if (other == null) {
            return 1;
          }

          return 1 + sacrifice(other, stack);
        }
      }
    }

    // n == 0, which means this cell is a dead end for the taking
    return 1;
  }
}