package aiproj.hexifence;

import java.io.PrintStream;

import unimelb.farrugiulian.hexifence.board.Board;

/*   
 *   Player Interface:
 *      Includes basic functions required by referee 
 *
 *   @author lrashidi
 */
public interface Player{
	
	/** This function is called by the referee to initialise the player.
	 *  Return 0 for successful initialization and -1 for failed one.
	 */
	public int init(int n, int p);
	
	/** Function called by referee to request a move by the player.
	 *  Return object of class Move
	 * 'This method is called by the referee to request a move by your player.
	 * Based on the current board configuration, your player should select its
	 * next move and return it as an object of the aiproj.hexifence.Move class.
	 * Note that each player needs to maintain its own internal state
	 * representation of the current board configuration.'
	 */
	public Move makeMove();
	
	/** Function called by referee to inform the player about the opponent's move
	 *  Return -1 if the move is illegal otherwise return 0
	 * 'This method is called by the referee to inform your player about the
	 * opponent's most recent move, so that you can maintain your board
	 * configuration. The input parameter is an object from the class
	 * aiproj.hexifence.Move, which includes the information about the last
	 * move made by the opponent. Based on your board configuration, if your
	 * player thinks this move is illegal you need to return -1 otherwise this
	 * function should return 0 if no new cell has been captured or 1 if a new
	 * cell (or two) has been captured by the opponent.'
	 * 
	 *  This move is illegal: Return -1
	 *  This move is legal and no new cell has been captured by the opponent: Return 0
	 *  This move is legal and a new cell (or two) has been captured by the opponent: Return 1
	 * 
	 */
	public int opponentMove(Move m);

	/** This function when called by referee should return the winner
	 *	Return -1, 0, 1, 2, 3 for INVALID, EMPTY, WHITE, BLACK, DEAD respectively
	 *
	 * 'This method should check the board configuration for a possible winner
	 * and return the winner as an integer value according to the Piece
	 * interface (-1=INVALID, 0=EMPTY, 1=BLUE, 2=RED, 3=DEAD).
	 * 
	 * Note that EMPTY corresponds to a non-terminal state of the game, DEAD
	 * corresponds to a draw, and INVALID corresponds to the case that the game
	 * has ended because the opponent move was illegal (e.g., placing a piece
	 * on an already occupied or captured cell).'
	 */
	public int getWinner();
	
	/** Function called by referee to get the board configuration in String format
	 * 'This method is called by the referee to ask your player to print its
	 * board configuration to a PrintStream object output. Note that this
	 * output will be different from the input format specified in Project
	 * Part A, i.e., we would suggest using B, R, or + corresponding to a
	 * Blue edge in play, a Red edge in play, or an empty edge, respectively.
	 * The notations b, r, and - correspond to a captured Blue cell, a captured
	 * Red cell and a non-existing edge respectively.
	 * 
	 * Please note that we place the captured cells on “non-existing” edges in
	 * the printout, for instance the first “r” in the second row is sitting in
	 * the position of a non-existing edge.'
	 */
	public void printBoard(PrintStream output);
}
