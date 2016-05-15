package unimelb.farrugiulian.hexifence.agent;

import java.util.Stack;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.Board;
import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;
import unimelb.farrugiulian.hexifence.board.features.FeatureSet;
import unimelb.farrugiulian.hexifence.board.features.RichFeature;

public class EndgameExpert implements Expert {

	public EndgameExpert(Board board) {
		// TODO Auto-generated constructor stub
		System.out.println("I'm not ready yet!");
		System.exit(0);
	}

	@Override
	public void update(Edge edge) {
		// TODO Auto-generated method stub

	}

	@Override
	public Edge move() {
		// If we have edges we can capture
		if (es.hasCapturingEdges()) {
			// Count the maximum number of cells we can take
			Stack<Edge> stack = new Stack<Edge>();
			int capturable = consumeAll(stack);
			while(!stack.isEmpty()){
				board.unplace(stack.pop());
			}
			
			// Check if we are in a position to double (double) box
			boolean isLoop = isLoop(es.getCapturingEdge());
			if (capturable == 2 || capturable == 4 && isLoop) {
				// Decide whether we should actually double box with a search
				consumeAll(stack);
				FeatureSet fs = new FeatureSet(board, super.piece);
				while(!stack.isEmpty()){
					board.unplace(stack.pop());
				}
				FeaturePair fp = featureSearch(fs, super.piece);
				if (fp.piece == super.piece) {
					// If double boxing makes us win, then double box (duh)
					Cell[] cells = es.getCapturingEdge().getCells();
					Cell cell;
					// Get the cell that has the edge that can double box
					if (cells[0].numFreeEdges() == 2) {
						cell = cells[0];
					} else {
						cell = cells[1];
					}
					// Figure out which edge can double box
					if (cell.getFreeEdges()[0] == es.getCapturingEdge()){
						return cell.getFreeEdges()[1];
					} else {
						return cell.getFreeEdges()[0];
					}
				} else {
					// If double boxing does not make us win, then take the cells
					// Sure it may not make us win either, but perhaps the opponent does
					// not need to throw as hard to let us win if we get more score now
					return es.getCapturingEdge();
				}
			}
			
			// We cannot double box, so take edges by the following priority:
			// Short loops, then long loops, then short chains, then long chains
			// Keep in mind that all free cells are automatically taken
			Edge bestEdge = null;
			isLoop = false;
			int captureSize = 10000;
			for (Edge edge : es.getCapturingEdges()) {
				int sacrificeSize = sacrificeSize(edge);
				boolean isCurrentLoop = isLoop(edge);
				if ((isCurrentLoop || !isLoop ) && sacrificeSize < captureSize) {
					bestEdge = edge;
					captureSize = sacrificeSize;
					isLoop = isCurrentLoop;
				}
			}
			return bestEdge;
		}
		
		// We do not have edges we can capture, so we need to make a sacrifice
		FeatureSet fs = new FeatureSet(board, super.piece);
		FeaturePair fp = featureSearch(fs, super.piece);
		if (fp.feature == null) {
			if (fp.piece == super.piece) {
				// No more intersected sacrifices and we should win
				// Get the smallest chain
				fp.feature = fs.getSmallestChain();
			} else {
				// No more intersected sacrifices and we should lose
				// Get a chain such that the last sacrifice is not a loop
				// (not too sure how to do this just yet so just get the smallest chain)
				fp.feature = fs.getSmallestChain();
			}
		}
		if (fp.piece == super.piece) {
			// We should win so make the sacrifice securely
			return fp.feature.secureOpen();
		} else {
			// We should lose so make a baiting sacrifice
			return fp.feature.baitOpen();
		}
	}
	
	private FeaturePair featureSearch(FeatureSet features, int piece) {
		int numIntersectedSacrifices = numIntersectedSacrifices(features);
		if (numIntersectedSacrifices == 0) {
			// Simple parity evaluation right now
			int numSacrifices = numSacrifices(features);
			int winningPiece = (piece == Piece.BLUE) ^ (numSacrifices % 2 == 0) ? Piece.BLUE : Piece.RED;
			return new FeaturePair(null, winningPiece);
		}
		
		FeaturePair result = null;
		for (int i = 0; i < numIntersectedSacrifices; i++) {
			FeatureSet featuresTmp = new FeatureSet(features);
			takeFeature(getIntersectedSacrifices(featuresTmp).get(i));
			
			FeaturePair pair = featureSearch(featuresTmp, Board.other(piece));
			
			pair.feature = getIntersectedSacrifices(features).get(i);
			if (pair.piece == piece){
				return pair; // found a winning move for this player!
			} else {
				// this move is not a winner, continue the searching!
				result = pair;
			}
		}
		
		return result;
	}
	
	private int winner(int piece) {
		FeatureSet fs = new FeatureSet(board, piece);
		return featureSearch(fs, piece).piece;
	}
	
	private class FeaturePair {
		public RichFeature feature;
		public int piece;
		FeaturePair(RichFeature feature, int piece){
			this.feature = feature;
			this.piece = piece;
		}
	}

	@Override
	public boolean transition() {
		return false;
	}
}
