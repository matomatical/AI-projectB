package unimelb.farrugiulian.hexifence.agent;

import java.util.Random;

import unimelb.farrugiulian.hexifence.board.Edge;

public class AgentRandom extends Agent{

	private Random rng;
	
	public AgentRandom(){
		long seed = System.nanoTime();
		rng = new Random(seed);
		System.err.println("AgentGreedy seed: "+seed);
	}
	
	@Override
	public Edge getChoice(){
		Edge[] edges = board.getFreeEdges();
		return edges[rng.nextInt(edges.length)];
	}

	@Override
	protected void notify(Edge edge) {
		// not keeping state
	}
}