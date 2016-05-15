package unimelb.farrugiulian.hexifence.agent.farrugiulian;

import aiproj.hexifence.Piece;
import unimelb.farrugiulian.hexifence.board.Board;
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
		// TODO Auto-generated method stub
		return null;
	}
	
	private FeaturePair minimax(FeatureSet features, int piece) {
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
			
			FeaturePair pair = minimax(featuresTmp, Board.other(piece));
			
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
