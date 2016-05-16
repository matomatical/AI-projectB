/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package com.matomatical.hexifence.visual;

/** VisualBoard Interface so that any board can be used with the Hexifence
 *  Visualiser 
 * @author Matt Farrugia [farrugiam@student.unimelb.edu.au]
 **/
public interface VisualBoard{

	/** getColor returns Piece.EMPTY, Piece.BLUE, or Piece.RED
	 *  depending on the color of the cell or edge at position
	 *  i,j according to the grid in the project specification
	 **/
	int getColor(int i, int j);
}
