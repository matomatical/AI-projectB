package com.matomatical.hexifence.visual;

import aiproj.hexifence.Move;

public class VisualReferee {

	Move move;
	VisualPlayer p1, p2, now, next, temp;
	int num = 1;
	int maxMoves, numMoves = 0;

	boolean playing, silence;
	
	public VisualReferee(int dimension, VisualPlayer p1, VisualPlayer p2, boolean silence){
		
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
		
		if(numMoves == maxMoves){
			playing = false;
		}
		
		if(playing){
			
			// get the move from the current player
			move = now.makeMove();
			
			// record the move
			numMoves++;
			if(!silence){
				System.out.println("Player "+ num + " placing " + move.P +" at " + move.Row + ":" + move.Col);
				// display the board according to this player
				now.printBoard(System.out);
			}
			VisualBoard board = now.getBoard();
			
			// send the move to the other player
			int result = next.opponentMove(move);
			if(result < 0){
				// oops! current player made an invalid move?
				playing = false;
				
			} else if (result == 0){
				// swap players for next turn		// switches 1 -> 2 -> 1 -> 2...
				temp = now; now = next; next = temp; num = 3 - num;
			} else {
				// player gets another turn! no swap
			}
			
			return board;
		}
		
		// if the game has already ended, just return the first player's board
		return p1.getBoard();
	}
}
