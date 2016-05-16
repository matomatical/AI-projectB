/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *            COMP30024 Artificial Intelligence - Semester 1 2016            *
 *                  Project B - Playing a Game of Hexifence                  *
 *                                                                           *
 *    Submission by: Julian Tran <juliant1> and Matt Farrugia <farrugiam>    *
 *                  Last Modified 02/05/16 by Matt Farrugia                  *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package aiproj.hexifence.matt;

import aiproj.hexifence.Move;
import aiproj.hexifence.Piece;
import aiproj.hexifence.Player;

/*
 *	Referee:
 *		A mediator between two players. It is responsible to initialize 
 *		the players and pass the plays between them and terminates the game. 
 *		It is the responsibility of the players to check whether they have
 *		won and maintain the board state.
 *
 *	@author lrashidi
 *  @author farrugiam (modifications)
 */


public class RefereeModified implements Piece{

	private static Player P1, P2;
	private static Move lastPlayedMove = new Move();
	
	/*
	 * Input arguments: first board size, second path of player1 and third path of player2
	 */
	public static void main(String[] args)
	{
		// set up the game
		
		int numberOfMoves = 0;
		int boardDimension = Integer.valueOf(args[0]);
		int boardEmptyPieces = boardDimension * (9 * boardDimension - 3);
		// we don't need to track remaining moves AND number of moves
		
		System.out.println("Referee started !");
		
		try{
			P1 = (Player)(Class.forName(args[1]).newInstance());
			P2 = (Player)(Class.forName(args[2]).newInstance());
		}
		catch(Exception e){
			System.out.println("Error "+ e.getMessage());
			System.exit(1);
		}
		
		P1.init(Integer.valueOf(args[0]), BLUE);
		P2.init(Integer.valueOf(args[0]), RED);
		
		
		// first turn
		
		int turn = 1;
		
		numberOfMoves++;
		lastPlayedMove = P1.makeMove();
		System.out.println("Placing to. " + lastPlayedMove.Row + ":"
			+ lastPlayedMove.Col + " by " + lastPlayedMove.P);
		P1.printBoard(System.out);
		boardEmptyPieces--; // we don't actually need boardEmptyPieces AND numberOfMoves
		
		turn = 2;

		
		// the rest of the turns (I think we could fix this)
		
		while(boardEmptyPieces > 0 && P1.getWinner() == 0 && P2.getWinner() ==0){
			
			if (turn == 2){			

				int opponentResult = P2.opponentMove(lastPlayedMove);
				
				if(opponentResult < 0) {
				
					System.out.println("Exception: Player 2 rejected the move of player 1.");
					P1.printBoard(System.out);
					P2.printBoard(System.out);
					System.exit(1);
				
				} else if(P2.getWinner()==0  && P1.getWinner()==0 && boardEmptyPieces>0){
				
					numberOfMoves++;
					
					if (opponentResult > 0){
						// p1 gets another turn!
						
						lastPlayedMove = P1.makeMove();
						System.out.println("Placing to. "+lastPlayedMove.Row+":"+lastPlayedMove.Col+" by "+lastPlayedMove.P);
						turn = 2;
						P1.printBoard(System.out);
					
					} else {
						// p2 gets a turn!
						
						lastPlayedMove = P2.makeMove();
						turn=1;
						System.out.println("Placing to. "+lastPlayedMove.Row+":"+lastPlayedMove.Col+" by "+lastPlayedMove.P);
						P2.printBoard(System.out);
					}
					
					boardEmptyPieces--;
				}
				
			} else {
				
				int opponentResult = P1.opponentMove(lastPlayedMove);
				
				if(opponentResult < 0) {
					System.out.println("Exception: Player 1 rejected the move of player 2.");
					P2.printBoard(System.out);
					P1.printBoard(System.out);
					System.exit(1);
				
				} else if(P2.getWinner()==0  && P1.getWinner()==0 && boardEmptyPieces>0){
					
					numberOfMoves++;
					
					if (opponentResult > 0){
						// p2 gets another turn!
						
						lastPlayedMove = P2.makeMove();
						System.out.println("Placing to. "+lastPlayedMove.Row+":"+lastPlayedMove.Col+" by "+lastPlayedMove.P);
						turn = 1;
						P2.printBoard(System.out);
						
					} else {
						// p1 gets a turn!
						
						lastPlayedMove = P1.makeMove();
						turn=2;
						System.out.println("Placing to. "+lastPlayedMove.Row+":"+lastPlayedMove.Col+" by "+lastPlayedMove.P);
						P1.printBoard(System.out);
					}
					
					boardEmptyPieces--;
				}
			}		
		}
		
		// after the loop, send the final move to the other player one last time
		
		if(turn == 2){
			int opponentResult = P2.opponentMove(lastPlayedMove);
			
			if(opponentResult < 0) {
			
				System.out.println("Exception: Player 2 rejected the move of player 1.");
				P1.printBoard(System.out);
				P2.printBoard(System.out);
				System.exit(1);
			}
		} else {
			
			int opponentResult = P1.opponentMove(lastPlayedMove);
			
			if(opponentResult < 0) {
				System.out.println("Exception: Player 1 rejected the move of player 2.");
				P2.printBoard(System.out);
				P1.printBoard(System.out);
				System.exit(1);
			
			}
		}
		
		
		System.out.println("--------------------------------------");
		System.out.println("P2 Board is :");
		P2.printBoard(System.out);
		System.out.println("P1 Board is :");
		P1.printBoard(System.out);
		
		System.out.println("Player one (BLUE) indicate winner as: "+ P1.getWinner());
		System.out.println("Player two (RED) indicate winner as: "+ P2.getWinner());
		System.out.println("Total Number of Moves Played in the Game: " + numberOfMoves);
		System.out.println("Referee Finished !");
	}
	

}
