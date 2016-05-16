/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 16/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package com.matomatical.hexifence.visual;

import aiproj.hexifence.Player;

/** VisualPlayer Interface so that any player can be used with the Hexifence
 *  Visualiser 
 * @author Matt Farrugia [farrugiam@student.unimelb.edu.au]
 **/
public interface VisualPlayer extends Player{
	
	/** getBoard returns a visual board object that can be inspected and
	 *  rendered
	 **/
	public VisualBoard getBoard();
}
