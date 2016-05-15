package unimelb.farrugiulian.hexifence.agent;

import java.io.PrintStream;

import com.matomatical.hexifence.visual.VisualBoard;
import com.matomatical.hexifence.visual.VisualPlayer;

import aiproj.hexifence.*;
import unimelb.farrugiulian.hexifence.board.Board;
import unimelb.farrugiulian.hexifence.board.Edge;

public abstract class Agent implements VisualPlayer{
	/** Which piece the agent uses */
	protected int piece;
	/** Which piece the agent's opponent uses */
	protected int opponent;
	
	/** The current score of the agent */
	protected int myScore = 0;
	/** The current score of the agent's opponent */
	protected int yourScore = 0;
	
	/** The current state of action of the agent */
	private enum PlayerState {
		STARTING, MOVING, WAITING, CHEATED;
	}
	private PlayerState state;
	
	/** Representation of the game board */
	protected Board board;
	
	/** Initializes the agent */
	@Override
	public int init(int n, int p) {
		
		// initialise internal state to empty n-dimensional board
		this.board = new Board(n);
		
		// figure out who is who
		this.piece = p;
		this.opponent = Board.other(p);
		
		// initialise player state
		this.state = PlayerState.STARTING;
		
		// all okay!
		return 0;
	}
	
	/** Queries the agent for its next move */
	protected abstract Edge getChoice();

	/** Notifies the agent that an edge has been placed on the board */
	// let the agent know that a piece has been placed on the board
	protected abstract void update(Edge edge);
	
	/** Makes the agent's next move */
	@Override
	public Move makeMove(){
		
		// decide on a move
		Edge choice = this.getChoice();	

		// calculate score
		int n = choice.numCapturableCells();
		this.myScore += n;
		
		// update player state
		if(this.state == PlayerState.MOVING || this.state == PlayerState.STARTING){
			if(n==0){
				// your turn
				this.state = PlayerState.WAITING;
			} else {
				// my turn again
				this.state = PlayerState.MOVING;
			}
		
		} else {
			// it's not my turn! state == WAITING or CHEATED
			System.out.println("Farrugiulian: hey! it's not my turn! " + this.state.name());
		}
		
		// place piece on board
		choice.place(this.piece);
		this.update(choice);
		
		// and finally, return the move
		return newMove(choice);
	}
	
	/** Helper function to generate a new move from an edge and our piece
	 * @param edge - the Edge we're generating a move for
	 * @return a new move based on this edge
	 **/
	private Move newMove(Edge edge){
		
		// make a new move
		Move m = new Move();
		
		// set it up
		m.Row = edge.i;
		m.Col = edge.j;
		m.P = this.piece;
		
		// return it
		return m;
	}

	/** Makes the agent's opponent's next move */
	@Override
	public int opponentMove(Move m) {

		// behaviour depends on state of this player
		
		if(this.state == PlayerState.MOVING){
			
			// hey! it's still MY turn!
			this.state = PlayerState.CHEATED;
			return -1;
		
		} else if(this.state == PlayerState.WAITING
					|| this.state == PlayerState.STARTING){
			// we ARE expecting a move
			// better validate the move...

			Edge edge = board.getEdge(m.Row, m.Col);
			
			if(edge == null || !edge.isEmpty()){
				// is was an invalid move!
				this.state = PlayerState.CHEATED;
				return -1;
				
			} else {
				// it was a valid move
				
				// how many did they capture?
				int n = edge.numCapturableCells();
				this.yourScore += n;
				
				// record the move
				edge.place(opponent);
				this.update(edge);
				
				if(n > 0){
					// damn, they scored, they get another turn
					this.state = PlayerState.WAITING;
					return 1;
				
				} else {
					// they didn't score, my turn next
					this.state = PlayerState.MOVING;
					return 0;
				}
			}
			
		} else {
			// they must have cheated earlier, we shouldn't get here
			return -1;
		}
	}
	
	/** Checks if there is a winner */
	@Override
	public int getWinner() {
		
		if(this.state == PlayerState.CHEATED){
			// something went wrong
			return Piece.INVALID;
			
		} else {
			// nothing has gone wrong yet
			
			if(myScore + yourScore < board.numCells()){
				// the game has not ended yet	
				return Piece.EMPTY;
			
			} else {
				// the game is over
				if(myScore > yourScore){
					return this.piece; 		// i won (likely)
					
				} else if (myScore == yourScore){
					return Piece.DEAD; 		// it was a tie (possible)
					
				} else {
					return this.opponent; 	// you won (unlikely)
				}
			}
		}
	}

	/** Prints an ASCII representation of the game board */
	@Override
	public void printBoard(PrintStream output) {
		board.printTo(output);
	}
	
	/** Returns a board that can be visually displayed */
	@Override
	public VisualBoard getBoard(){
		return board;
	}
}