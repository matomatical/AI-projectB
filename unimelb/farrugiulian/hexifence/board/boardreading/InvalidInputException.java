/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 03/04/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.board.boardreading;

/** Custom exception to alert reader about some user-defined issue with their
 *  Board input
 *  
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran   [juliant1]
 **/
public class InvalidInputException extends Exception {
	
	/** generated serial version uid */
	private static final long serialVersionUID = 226857444666195940L;

	public InvalidInputException(String message){
		super("Invalid input: " + message);
	}
}
