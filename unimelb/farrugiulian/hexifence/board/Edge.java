
package unimelb.farrugiulian.hexifence.board;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.Cell;

public class Edge extends Index {

	private Cell[] cells = new Cell[2];
	private int ncells = 0;
	
	public Edge(int i, int j){
		super(i,j);
	}

	public void link(Cell cell) {
		this.cells[ncells++] = cell;
	}

	public void place(int piece) {
		if(this.isEmpty() && piece != Piece.EMPTY){
			
			// claim nearby pieces
			for(Cell cell : this.cells){
				if(cell != null && cell.numFreeEdges() == 1){
					cell.color = piece;
				}
			}
			
			// and set ths piece
			this.color = piece;
		}
	}
	
	public void unplace(){
		if(color != Piece.EMPTY){
			
			// un-claim nearby claimed pieces
			for(Cell cell : this.cells){
				if(cell != null && cell.color != Piece.EMPTY){
					cell.color = Piece.EMPTY;
				}
			}
			
			// and un-set ths piece
			this.color = Piece.EMPTY;
		}
	}

	public int numCapturableCells() {
		
		// can't capture anything with this edge if it's already taken !!
		if(!this.isEmpty()){
			return 0;
		}

		// otherwise, let's count the ones we'd capture if we placed here
		int n = 0;
		for(Cell cell : cells){
			if(cell != null && cell.numFreeEdges() == 1){
				n++;
			}
		}
		return n;
	}

	public Cell[] getCells() {
		Cell[] cells = new Cell[this.ncells];
		for(int i = 0; i < ncells; i++){
			cells[i] = this.cells[i];
		}
		return cells;
	}
	
	public Cell getOtherCell(Cell cell){
		
		// side of board
		if(cells.length == 1){
			return null;
		}
		
		// not side of board
		return (cells[0] == cell) ? cells[1] : cells[0];
		
	}
	
}
