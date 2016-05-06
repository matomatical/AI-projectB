package aiproj.hexifence;

/*   
 *   Referee:
 *      A mediator between two players. It is responsible to initialize 
 *      the players and pass the plays between them and terminates the game. 
 *      It is the responsibility of the players to check whether they have won and
 *      maintain the board state.
 *
 *   @author lrashidi
 */


public class Referee implements Piece{

	private static Player P1;
	private static Player P2;
	private static Move lastPlayedMove;
	
	/*
	 * Input arguments: first board size, second path of player1 and third path of player2
	 */
	public static void main(String[] args)
	{
		lastPlayedMove = new Move();
		int NumberofMoves = 0;
		int dimension = Integer.valueOf(args[0]);
		int boardEmptyPieces=(dimension)*(9*dimension-3);
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
		
		int opponentResult=0;
		int turn=1;
		

                NumberofMoves++;
                lastPlayedMove=P1.makeMove();
                System.out.println("Placing to. "+lastPlayedMove.Row+":"+lastPlayedMove.Col+" by "+lastPlayedMove.P);
                P1.printBoard(System.out);
		boardEmptyPieces--;
		turn =2;

		while(boardEmptyPieces > 0 && P1.getWinner() == 0 && P2.getWinner() ==0)
		{
		if (turn == 2){			

			opponentResult = P2.opponentMove(lastPlayedMove);
			if(opponentResult<0)
			{
				System.out.println("Exception: Player 2 rejected the move of player 1.");
				P1.printBoard(System.out);
				P2.printBoard(System.out);
				System.exit(1);
			}			
			else if(P2.getWinner()==0  && P1.getWinner()==0 && boardEmptyPieces>0){
				NumberofMoves++;
				if (opponentResult>0){
					lastPlayedMove = P1.makeMove();
					System.out.println("Placing to. "+lastPlayedMove.Row+":"+lastPlayedMove.Col+" by "+lastPlayedMove.P);
					turn = 2;
					P1.printBoard(System.out);
				}	
				else{	
					lastPlayedMove = P2.makeMove();
					turn=1;
					System.out.println("Placing to. "+lastPlayedMove.Row+":"+lastPlayedMove.Col+" by "+lastPlayedMove.P);
					P2.printBoard(System.out);
				}
				boardEmptyPieces--;
			}
		}
		else{	
			
			opponentResult = P1.opponentMove(lastPlayedMove);
			if(opponentResult<0)
			{
				System.out.println("Exception: Player 1 rejected the move of player 2.");
				P2.printBoard(System.out);
				P1.printBoard(System.out);
				System.exit(1);
			}
			else if(P2.getWinner()==0  && P1.getWinner()==0 && boardEmptyPieces>0){
                                NumberofMoves++;
                                if (opponentResult>0){
                                        lastPlayedMove = P2.makeMove();
                                        System.out.println("Placing to. "+lastPlayedMove.Row+":"+lastPlayedMove.Col+" by "+lastPlayedMove.P);
                                        turn = 1;
                                        P2.printBoard(System.out);
                                }
				else{
                                        lastPlayedMove = P1.makeMove();
                                        turn=2;
                                        System.out.println("Placing to. "+lastPlayedMove.Row+":"+lastPlayedMove.Col+" by "+lastPlayedMove.P);
                                        P1.printBoard(System.out);
                                }
                                boardEmptyPieces--;
			}	
		}
			
		}
		
		
		if(turn == 2){
		    opponentResult = P2.opponentMove(lastPlayedMove);
		    if(opponentResult < 0) {
			System.out.println("Exception: Player 2 rejected the move of player 1.");
			P1.printBoard(System.out);
			P2.printBoard(System.out);
			System.exit(1);
		    }
		} else {
		    opponentResult = P1.opponentMove(lastPlayedMove);
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
		System.out.println("Total Number of Moves Played in the Game: "+ NumberofMoves);
		System.out.println("Referee Finished !");
	}
	

}
