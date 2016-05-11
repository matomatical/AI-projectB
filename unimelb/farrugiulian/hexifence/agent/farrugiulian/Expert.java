package unimelb.farrugiulian.hexifence.agent.farrugiulian;

import unimelb.farrugiulian.hexifence.board.Edge;

public interface Expert { 
	
	/** Notify this expert that an edge has been placed so that
	 *  it can update its internal state
	 *  @param edge The edge that has been placed on the board
	 **/
	public void update(Edge edge);
	
	/** Ask this expert for the next edge to place
	 *  @return the next Edge that should be played according
	 *          to this expert, or null if this expert cannot
	 *          make a decision
	 **/
	public Edge move();

	/** Ask this expert if it's time to transition
	 * @return true if this expert thinks it's time to transition
	 * Should return true before the expert would return null from {@link Expert#move() move()}
	 **/
	public boolean transition();
	
}
