package unimelb.farrugiulian.hexifence.board.boardreading;

import unimelb.farrugiulian.hexifence.board.Board;

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
