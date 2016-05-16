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

public class AgentMe extends Agent{

	private Scanner sc = new Scanner(System.in);
	
	protected Edge getChoice(){

		super.board.printIndexedTo(System.out);
		System.out.print("where would you like to move? ('row col')");
		
		int i = sc.nextInt();
		int j = sc.nextInt();
		
		return board.getEdge(i, j);
	}

	@Override
	protected void update(Edge edge) {
		// not tracking state
	}
}