package com.matomatical.hexifence.visual;

import aiproj.hexifence.Player;

public interface VisualPlayer extends Player{
	
	/** getBoard returns a visual board object that can be inspected and
	 *  rendered
	 */
	public VisualBoard getBoard();
}
