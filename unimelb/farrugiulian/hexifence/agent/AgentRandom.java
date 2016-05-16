/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 24/04/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.agent;

import java.util.Random;

import unimelb.farrugiulian.hexifence.board.Edge;

/**
 * This agent plays the Game of Hexifence randomly, selecting any empty edge.
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran [juliant1]
 **/
public class AgentRandom extends Agent {

  /** Random number generator for selecting moves **/
  private Random rng;

  /** Create a new AgentRandom, seeded with the current time (nanoTime()) **/
  public AgentRandom() {
    long seed = System.nanoTime();
    rng = new Random(seed);
  }

  /** Choose a random empty edge to play **/
  @Override
  public Edge getChoice() {
    Edge[] edges = board.getEmptyEdges();
    return edges[rng.nextInt(edges.length)];
  }

  @Override
  protected void update(Edge edge) {
    // not keeping state! do nothing
  }
}