/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 12/04/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.board.boardreading;

import unimelb.farrugiulian.hexifence.board.Board;

/**
 * Class capable of reading and constructing a hexifence board form some input
 * source
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran [juliant1]
 **/
public interface BoardReader {

  /**
   * Create a new board based on some input
   * 
   * @param input
   *          may be a filename, or a description of a board, or whatever
   * @return a new board initialised to the state described by input
   * @throws InvalidInputException
   *           if something goes wrong reading the input
   */
  public Board makeBoard(String input) throws InvalidInputException;
}
