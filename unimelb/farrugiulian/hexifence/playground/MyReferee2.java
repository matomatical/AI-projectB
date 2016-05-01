package unimelb.farrugiulian.hexifence.playground;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;
import aiproj.hexifence.Player;

public class MyReferee2 {

	public static void main(String[] args){
		
		// get command line arguments
		Options ops = new Options(args); // stores command line arguments
		
		System.out.println("Referee started! Let's play Hexifence!");
		
		// load and initialise the player classes
		System.out.println("Loading the players...");
		
		Player[] p = new Player[2];
		
		try{
			// player 1
			
			System.out.println("Welcome, Player 1; " + ops.playerOne + "!");
			p[0] = (Player)(Class.forName(ops.playerOne).newInstance());
			p[0].init(ops.dimension, Piece.BLUE);
			
			// player 2
			System.out.println("And also Player 2; " + ops.playerTwo + "!");
			p[1] = (Player)(Class.forName(ops.playerTwo).newInstance());
			p[1].init(ops.dimension, Piece.RED);
			
		} catch(Exception e){
			System.out.println("Unable to load Players!");
			System.out.println(e.getMessage());
			System.exit(1);
		}
		
		
		// run the actual game
		
		System.out.println("Let the game begin!");
		
		int numMoves = 0, maxMoves = ops.dimension * (9 * ops.dimension - 3);
		int currentPlayer = 0;
		
		while(numMoves < maxMoves){
			
			// get the move from the current player
			Move move = p[currentPlayer].makeMove();
			
			// record the move
			numMoves++;
			System.out.println("Player "+ (1+currentPlayer) + " placing " + move.P
					+" at " + move.Row + ":" + move.Col);
			
			// display the board according to this player
			p[currentPlayer].printBoard(System.out);
			
			// send the move to the other player
			int result = p[1-currentPlayer].opponentMove(move);
			if(result < 0){
				// oops! current player made an invalid move?
				System.out.println("Exception: Player "+(1+currentPlayer)
						+" rejected the move of Player "+(2-currentPlayer)+".");
				p[0].printBoard(System.out);
				p[1].printBoard(System.out);
				System.exit(1); // little harsh!
				
			} else if (result == 0){
				// swap players for next turn		
				currentPlayer = 1 - currentPlayer; // switches 0 -> 1 -> 0 -> 1 -> ...
				
			} else {
				// current player gets another turn! no swap
			}
		}
		
		// wrap it up
		System.out.println("-------------- game ended --------------");
		
		System.out.println("Total moves played: " + numMoves);
		
		System.out.println("Player 1 reports winner as: " + p[0].getWinner());
		System.out.println("Player 1 final board:");
		p[0].printBoard(System.out);
		
		System.out.println("Player 2 reports winner as: " + p[1].getWinner());
		System.out.println("Player 2 final board:");
		p[1].printBoard(System.out);
		
		System.out.println("Thanks for playing! Referee end.");
	}	
}
