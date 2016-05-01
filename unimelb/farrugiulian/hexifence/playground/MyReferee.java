package unimelb.farrugiulian.hexifence.playground;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;
import aiproj.hexifence.Player;

public class MyReferee {

	public static void main(String[] args){
		
		// get command line arguments
		Options ops = new Options(args); // stores command line arguments
		
		System.out.println("Referee started! Let's play Hexifence!");
		
		// load and initialise the player classes
		
		System.out.println("Loading the players...");
		
		Player p1 = null, p2 = null;
		
		try{
			// player 1
			System.out.println("Welcome, Player 1; " + ops.playerOne + "!");
			p1 = (Player)(Class.forName(ops.playerOne).newInstance());
			p1.init(ops.dimension, Piece.BLUE);
			
			// player 2
			System.out.println("And also Player 2; " + ops.playerTwo + "!");
			p2 = (Player)(Class.forName(ops.playerTwo).newInstance());
			p2.init(ops.dimension, Piece.RED);
			
		} catch(Exception e){
			System.out.println("Unable to load Players!");
			System.out.println(e.getMessage());
			System.exit(1);
		}
		
		
		// run the actual game
		
		System.out.println("Let the game begin!");
		
		int numMoves = 0, maxMoves = ops.dimension * (9 * ops.dimension - 3);
		Player currentPlayer = p1, nextPlayer = p2, temp; int num = 1;
		
		while(numMoves < maxMoves){
			
			// get the move from the current player
			Move move = currentPlayer.makeMove();
			
			// record the move
			numMoves++;
			System.out.println("Player "+ num + " placing " + move.P
					+" at " + move.Row + ":" + move.Col);
			
			// display the board according to this player
			currentPlayer.printBoard(System.out);
			
			// send the move to the other player
			int result = nextPlayer.opponentMove(move);
			if(result < 0){
				// oops! current player made an invalid move?
				System.out.println("Exception: Player "+num+" rejected the move of Player "+(3-num)+".");
				p1.printBoard(System.out);
				p2.printBoard(System.out);
				System.exit(1); // little harsh!
				
			} else if (result == 0){
				// swap players for next turn		
				temp = currentPlayer; currentPlayer = nextPlayer; nextPlayer = temp;
				num = 3 - num; // switches 1 -> 2 -> 1 -> 2 -> 1 ...
			} else {
				// current player gets another turn! no swap
			}
		}
		
		// wrap it up
		System.out.println("-------------- game ended --------------");
		
		System.out.println("Total moves played: " + numMoves);
		
		System.out.println("Player 1 reports winner as: " + p1.getWinner());
		System.out.println("Player 1 final board:");
		p1.printBoard(System.out);
		
		System.out.println("Player 2 reports winner as: " + p2.getWinner());
		System.out.println("Player 2 final board:");
		p2.printBoard(System.out);
		
		
		System.out.println("Thanks for playing! Referee end.");
	}	
}
