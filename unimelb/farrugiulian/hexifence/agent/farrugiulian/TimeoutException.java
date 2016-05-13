package unimelb.farrugiulian.hexifence.agent.farrugiulian;

import unimelb.farrugiulian.hexifence.board.Edge;

public class TimeoutException extends Exception {

	/** generated serialiseable version uid */
	private static final long serialVersionUID = -5179983027717240244L;

	
	public Edge edge;
	
	public TimeoutException(Edge edge){
		super("Timed is up!");
		this.edge = edge;
	}
}
