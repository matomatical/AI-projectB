package unimelb.farrugiulian.hexifence.agent;

import unimelb.farrugiulian.hexifence.board.Board;
import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

public class AgentMax extends Agent{

	
	private AgentGreedy starter;
	
	public int init(int n, int p){
		int retval = super.init(n, p);
		
		starter = new AgentGreedy();
		starter.init(n, p);
		
		starter.board = this.board;
		
		return retval;
	}
	
	@Override
	public Edge getChoice(){
		
		Edge[] edges = board.getFreeEdges();
		
		if(edges.length > 20){
			return starter.getChoice();
		}
		
		int max_depth = 4;
		
		// try the first edge
		Edge bestEdge = edges[0];
		
		boolean winner = (edges[0].numCapturableCells() > 0);
		board.place(edges[0], piece);
		int bestValue = minimax(board, max_depth, winner); // winner is false iff we didn't capture a cell
		board.unplace(edges[0]);
		
		// try all the other edges
		for(int i = 1; i < edges.length; i++){
			winner = (edges[i].numCapturableCells() > 0);
			board.place(edges[i], piece);
			int value = minimax(board, max_depth, winner); // winner is false iff we didn't capture a cell
			board.unplace(edges[i]);
			
			if(value > bestValue){
				bestValue = value;
				bestEdge = edges[i];
			}
		}
		
		return bestEdge;
	}

	private int minimax(Board board, int depth, boolean maximising){
	
		Edge[] edges = board.getFreeEdges();
		
		if(depth < 0 || edges.length < 1){
			return evaluate(board);
		}
		
		// try the first edge
		
		boolean winner = (edges[0].numCapturableCells() > 0);
		
		int placing_piece = maximising ? piece : opponent;
		
		if(!maximising){
			winner = !winner;
		}
		
		board.place(edges[0], placing_piece);
		
		int bestValue = minimax(board, depth-1, winner);
		
		board.unplace(edges[0]);
		
		// try all the other edges
		for(int i = 1; i < edges.length; i++){
			
			if(maximising){
				winner = (edges[i].numCapturableCells() > 0);
				
				board.place(edges[i], placing_piece);
				int value = minimax(board, depth-1, winner);
				board.unplace(edges[i]);
				
				if(value > bestValue){
					bestValue = value;
				}
			} else {
				winner = (edges[i].numCapturableCells() > 0);
				
				board.place(edges[i], placing_piece);
				int value = minimax(board, depth-1, winner);
				board.unplace(edges[i]);
				
				if(value < bestValue){
					bestValue = value;
				}
			}	
		}
		
		return bestValue;
	}
	
	private int evaluate(Board board){
		
		int evaluation = 0;
		
		for(Cell cell : board.getCells()){
			if(cell.getColor() == piece){
				evaluation += 1;
			} else if(cell.getColor() == opponent){
				evaluation -= 1;
			}
		}
		
		return evaluation;
	}
	
	
	@Override
	protected void notify(Edge edge) {
		// not keeping state
	}
}