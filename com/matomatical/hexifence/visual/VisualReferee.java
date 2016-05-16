/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package com.matomatical.hexifence.visual;

import aiproj.hexifence.Move;
import unimelb.farrugiulian.hexifence.board.Board;

/**
 * Modified Referee class which can be used as a model for a LibGDX Game
 * 
 * @author Matt Farrugia [farrugiam@student.unimelb.edu.au]
 **/
public class VisualReferee {

  Move move;
  VisualPlayer p1, p2, now, next, temp;
  int num = 1;
  int maxMoves, numMoves = 0;
  long startTime;

  boolean playing, silence;

  /**
   * Create a new VisualReferee
   * 
   * @param dimension
   *          the board dimension to play on
   * @param p1
   *          the first Player
   * @param p2
   *          the second Player
   * @param silence
   *          true if you don't want the referee to print to stdout
   */
  public VisualReferee(int dimension, VisualPlayer p1, VisualPlayer p2,
      boolean silence) {
    startTime = System.currentTimeMillis();

    // the players playing the game
    this.p1 = p1;
    this.p2 = p2;

    // player 1 moves first
    now = p1;
    next = p2;

    // the maximum number of moves
    maxMoves = dimension * (9 * dimension - 3);

    // the loop guard (except loop is external)
    this.playing = true;

    // do we want to print to the console too?
    this.silence = silence;
  }

  public VisualBoard update() {

    if (numMoves == maxMoves && playing) {
      System.out.println("Winner is " + Board.name(p1.getWinner()));
      System.out.println("Game time was "
          + (System.currentTimeMillis() - startTime) / 1000F + " seconds");
      playing = false;
    }

    if (playing) {

      // get the move from the current player
      move = now.makeMove();

      // record the move
      numMoves++;
      if (!silence) {
        System.out.println("Player " + num + " placing " + move.P + " at "
            + move.Row + ":" + move.Col);
        // display the board according to this player
        now.printBoard(System.out);
      }
      VisualBoard board = now.getBoard();

      // send the move to the other player
      int result = next.opponentMove(move);
      if (result < 0) {
        // oops! current player made an invalid move?
        playing = false;

      } else if (result == 0) {
        // swap players for next turn // switches 1 -> 2 -> 1 -> 2
        temp = now;
        now = next;
        next = temp;
        num = 3 - num;
      } else {
        // player gets another turn! no swap
      }

      return board;
    }

    // if the game has already ended, just return the first player's board
    return p1.getBoard();
  }
}
