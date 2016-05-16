/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                   Project A - Testing the Current State                   *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 03/04/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.board.boardreading;

/**
 * Custom exception to alert others about symbol mismatches within the input
 * used for board configuration. Has specific and general constructors.
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran [juliant1]
 **/
public class InvalidSymbolException extends Exception {

  /** Generated serial version uid **/
  private static final long serialVersionUID = -8365606279059739723L;

  /**
   * Create an exception to repseresnt a particular symbol mismatch at some
   * index. Creates an informative message to describe the mismatch.
   * 
   * @param i
   *          row index where encountered
   * @param j
   *          clumns index where encountered
   * @param symbol
   *          encountered symbol
   * @param expectedSymbol
   *          expected symbol
   **/
  public InvalidSymbolException(int i, int j, String symbol,
      String expectedSymbol) {
    super("Invalid symbol " + symbol + " at index (" + i + "," + j + "),"
        + " expecting " + expectedSymbol);
  }

  /**
   * Create an exception to repseresnt a general symbol mismatch. Creates an
   * informative message to describe the mismatch based on a user specified
   * reason.
   * 
   * @param symbol
   *          encountered symbol
   * @param reason
   *          custom reason why this caused an exception
   **/
  public InvalidSymbolException(String symbol, String reason) {
    super("Invalid symbol \"" + symbol + "\": " + reason);
  }
}
