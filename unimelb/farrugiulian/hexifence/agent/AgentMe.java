/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 24/04/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package unimelb.farrugiulian.hexifence.agent;

import java.util.Scanner;

import unimelb.farrugiulian.hexifence.board.Edge;

/** This agent does not think for itself, it asks stdin for help deciding which
 *  edges to take at any point in time!
 *  
 * @author Matt Farrugia [farrugiam]
 * @author Julian Tran   [juliant1]
 **/
public class AgentMe extends Agent{

	/** Source of input **/
	private Scanner sc = new Scanner(System.in);
	
	/** Prompts user for a choice and returns it **/
	protected Edge getChoice(){

		// print the indexed board for the user to see
		super.board.printIndexedTo(System.out);
		
		// prompt for input
		System.out.print("where would you like to move? ('row col')");
		
		// get the answers!
		int i = sc.nextInt();
		int j = sc.nextInt();
		
		// return this edge (if it's invalid, that's the user's fault)
		return board.getEdge(i, j);
	}

	@Override
	protected void update(Edge edge) {
		// not tracking state, do nothing
	}
}