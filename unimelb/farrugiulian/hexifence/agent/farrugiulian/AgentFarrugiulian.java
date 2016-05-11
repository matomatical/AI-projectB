package unimelb.farrugiulian.hexifence.agent.farrugiulian;

import unimelb.farrugiulian.hexifence.agent.Agent;
import unimelb.farrugiulian.hexifence.board.*;

public class AgentFarrugiulian extends Agent {

	private enum GameStage{
		OPENING, MIDGAME, ENDGAME;
	}
	private GameStage stage = GameStage.OPENING;
	private Expert expert;
	
	@Override
	public int init(int n, int p){
		if(super.init(n, p) != 0){
			// parent init failure!!
			return -1;
		}
		
		// parent init success!
		
		// initialise the first expert
		this.expert = new OpeningExpert(super.board, 10);
		
		// return the same value as superclass
		return 0;
	}
	
	@Override
	protected void update(Edge edge) {
		
		this.expert.update(edge);
		
		if(expert.transition()){
			if(stage == GameStage.OPENING){
				stage = GameStage.MIDGAME;
				
				expert = new MidgameExpert(super.board);
				
			} else if(stage == GameStage.MIDGAME){
				stage = GameStage.ENDGAME;
				
				expert = new EndgameExpert(super.board);
				
			}
		}
	}
	
	@Override
	public Edge getChoice(){
	
		return expert.move();
	}
}