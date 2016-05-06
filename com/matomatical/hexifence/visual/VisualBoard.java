
package com.matomatical.hexifence.visual;

public interface VisualBoard{

	/** getColor returns Piece.EMPTY, Piece.BLUE, or Piece.RED
	 *  depending on the color of the cell or edge at position
	 *  i,j according to the grid in the project specification
	 */
	int getColor(int i, int j);
}
