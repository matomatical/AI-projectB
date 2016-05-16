/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 12/04/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.board.boardreading;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.Board;

/**
 * A BoardReader that reads a board formatted in ASCII according to the Project
 * Specification
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran [juliant1]
 **/
public class AsciiBoardReader implements BoardReader {

  /**
   * New board based on ASCII board input at filename
   * 
   * @param filename
   *          Input filename (null for stdin)
   **/
  @Override
  public Board makeBoard(String input) throws InvalidInputException {

    InputStream in = null;

    if (input == null) {
      in = System.in;
    } else {
      try {
        in = new FileInputStream(input);
      } catch (FileNotFoundException e) {
        throw new InvalidInputException("Filename invalid");
      }
    }

    Scanner sc = new Scanner(in);

    try {

      // first line should contain dimension
      int dimension = Integer.parseInt(sc.nextLine());

      // make board with this dimension
      Board board = new Board(dimension);

      // read rest of input to configure board
      this.configure(board, sc);

      // return this board to the user
      return board;

    } catch (NoSuchElementException e) {
      // input was icomplete
      throw new InvalidInputException(
          "Incomplete! Follow input guidelines");

    } catch (NumberFormatException e) {
      // first line was not a valid integer
      throw new InvalidInputException(
          "Mismatch! First line should be integer dimension");

    } catch (InvalidSymbolException e) {
      // somewhere along the line there was an invalid symbol!
      throw new InvalidInputException(e.getMessage());

    } finally {
      sc.close();
    }
  }

  /**
   * Places edges on board according to input contained in scanner, which should
   * be formatted according to assignment specification.
   * 
   * @param scanner
   *          The scanner to take input from
   * @throws InvalidSymbolException
   *           if a non-allowed symbol is detected at a specific index.
   * @throws NoSuchElementException
   *           if the scanner runs out of input before reading is finished
   **/
  private void configure(Board board, Scanner scanner)
      throws InvalidSymbolException {

    String symbol = null;

    for (int i = 0; i < board.width; i++) {
      for (int j = 0; j < board.width; j++) {

        // read in the next symbol
        symbol = scanner.next();

        // what is it supposed to be at this index?

        // maybe it's not a cell or an edge!
        if (!board.isValid(i, j)) {// non cell/edge indices are null
          if (!symbol.equals("-")) {
            // in that case, we expect a "-"
            throw new InvalidSymbolException(i, j, symbol, "-");
          }

          // maybe it is a cell!
        } else if (board.isCell(i, j)) {// cells have (odd,odd) indices
          if (!symbol.equals("-")) {
            // we expect a "-" for a cell
            throw new InvalidSymbolException(i, j, symbol, "-");
          }

          // otherwise, it must be an edge!
        } else {

          if (symbol.equals("R")) {
            board.place(i, j, Piece.RED);
          } else if (symbol.equals("B")) {
            board.place(i, j, Piece.BLUE);

          } else if (!symbol.equals("+")) {
            // we expect "+", "R" or "B"
            throw new InvalidSymbolException(i, j, symbol, "R, B or +");
          }
        }
      }

      // after each row, check that there are no remaining symbols here
      String s;
      if (scanner.hasNextLine() && !(s = scanner.nextLine()).equals("")) {
        throw new InvalidSymbolException(s,
            "Unexpected symbol past end of line!");
      }
    }

    // after all rows, check that there are no remaining symbols
    if (scanner.hasNext()) {
      throw new InvalidSymbolException(scanner.next(),
          "Symbol encountered after input complete!");
    }

    // if we made it through all of that with no exceptions, the board
    // has been successfully configured
  }
}
