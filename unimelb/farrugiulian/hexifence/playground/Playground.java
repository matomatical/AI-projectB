package unimelb.farrugiulian.hexifence.playground;

import aiproj.hexifence.Move;
import aiproj.hexifence.Player;
import unimelb.farrugiulian.hexifence.board.Board;

public class Playground {

	Move move;
	Player p1, p2, now, next, temp;
	int num = 1;
	int maxMoves, numMoves = 0;

	boolean playing, silence;
	
	public Playground(int dimension, Player p1, Player p2, boolean silence){
		
		this.p1 = p1;
		this.p2 = p2;
		
		now = p1;
		next = p2;
		
		maxMoves = dimension * (9 * dimension - 3);
		
		this.playing = true;
		this.silence = silence;
	}
	
	public void update() {
		
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
		}
	}

	public Board getBoard() {
		return p1.getBoard();
	}
}
