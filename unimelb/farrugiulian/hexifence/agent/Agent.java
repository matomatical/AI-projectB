/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.agent;

import java.io.PrintStream;

import com.matomatical.hexifence.visual.VisualBoard;
import com.matomatical.hexifence.visual.VisualPlayer;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.Board;
import unimelb.farrugiulian.hexifence.board.Edge;

/**
 * Abstract Agent class for fulfilling the Player interface and ensuring that we
 * play a legal game of hexifence (all the way down to selecting actual moves
 * from the board; selecting a valid (and intelligent) move is down to Agent's
 * subclasses)
 * 
 * Allows for multple AI Agent implementations using the Template Pattern
 * through abstract getChoice() and update(Edge) methods which can be overriden
 * with specific behaviour
 * 
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran [juliant1]
 **/
public abstract class Agent implements VisualPlayer {
  /** Which piece the agent uses **/
  protected int piece;
  /** Which piece the agent's opponent uses **/
  protected int opponent;

  /** The current score of the agent **/
  protected int myScore = 0;
  /** The current score of the agent's opponent **/
  protected int yourScore = 0;

  /** The current state of action of the agent **/
  private enum PlayerState {
    STARTING, MOVING, WAITING, CHEATED;
  }

  private PlayerState state;

  /** Representation of the game board **/
  protected Board board;

  @Override
  public int init(int n, int p) {

    // initialise internal state to empty n-dimensional board
    this.board = new Board(n);

    // figure out who is who
    this.piece = p;
    this.opponent = Board.other(p);

    // initialise player state
    this.state = PlayerState.STARTING;

    // all okay!
    return 0;
  }

  /**
   * Queries the agent for its next move
   * 
   * @return The next Edge to play (up to the subclass to ensure it's legal)
   **/
  protected abstract Edge getChoice();

  /**
   * Notifies this agent that an edge has been placed on the board, by either
   * the opponent or ourselves
   * 
   * @param edge
   *          The piece that was just played
   **/
  protected abstract void update(Edge edge);

  /** Select and return the best next move according to this agent **/
  @Override
  public Move makeMove() {

    // decide on a move
    Edge choice = this.getChoice();

    // calculate score
    int n = choice.numCapturableCells();
    this.myScore += n;

    // update player state
    if (this.state == PlayerState.MOVING
        || this.state == PlayerState.STARTING) {
      if (n == 0) {
        // your turn
        this.state = PlayerState.WAITING;
      } else {
        // my turn again
        this.state = PlayerState.MOVING;
      }

    } else {
      // it's not my turn! state == WAITING or CHEATED
      System.out.println(
          "Farrugiulian: hey! it's not my turn! " + this.state.name());
    }

    // place piece on board
    choice.place(this.piece);
    this.update(choice);

    // and finally, return the move
    return newMove(choice);
  }

  /**
   * Helper function to generate a new move from an edge and our piece
   * 
   * @param edge
   *          the Edge we're generating a move for
   * @return a new move based on this edge
   **/
  private Move newMove(Edge edge) {

    // make a new move
    Move m = new Move();

    // set it up
    m.Row = edge.i;
    m.Col = edge.j;
    m.P = this.piece;

    // return it
    return m;
  }

  /** Respond to opponent move (validating and updating our state) **/
  @Override
  public int opponentMove(Move m) {

    // behaviour depends on state of this player

    if (this.state == PlayerState.MOVING) {

      // hey! it's still MY turn!
      this.state = PlayerState.CHEATED;
      return -1;

    } else if (this.state == PlayerState.WAITING
        || this.state == PlayerState.STARTING) {
      // we ARE expecting a move
      // better validate the move...

      Edge edge = board.getEdge(m.Row, m.Col);

      if (edge == null || !edge.isEmpty()) {
        // is was an invalid move!
        this.state = PlayerState.CHEATED;
        return -1;

      } else {
        // it was a valid move

        // how many did they capture?
        int n = edge.numCapturableCells();
        this.yourScore += n;

        // record the move
        edge.place(opponent);
        this.update(edge);

        if (n > 0) {
          // damn, they scored, they get another turn
          this.state = PlayerState.WAITING;
          return 1;

        } else {
          // they didn't score, my turn next
          this.state = PlayerState.MOVING;
          return 0;
        }
      }

    } else {
      // they must have cheated earlier, we shouldn't get here
      return -1;
    }
  }

  /** Checks if there is a winner **/
  @Override
  public int getWinner() {

    if (this.state == PlayerState.CHEATED) {
      // something went wrong
      return Piece.INVALID;

    } else {
      // nothing has gone wrong yet

      if (myScore + yourScore < board.numCells()) {
        // the game has not ended yet
        return Piece.EMPTY;

      } else {
        // the game is over
        if (myScore > yourScore) {
          return this.piece; // i won (likely)

        } else if (myScore == yourScore) {
          return Piece.DEAD; // it was a tie (possible)

        } else {
          return this.opponent; // you won (unlikely)
        }
      }
    }
  }

  /**
   * Prints and ASCII representation of the game board to the PrintStream
   **/
  @Override
  public void printBoard(PrintStream output) {
    board.printTo(output);
  }

  /** Returns a board that can be visually displayed **/
  @Override
  public VisualBoard getBoard() {
    return board;
  }
}