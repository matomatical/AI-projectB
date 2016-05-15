package unimelb.farrugiulian.hexifence.agent;

import unimelb.farrugiulian.hexifence.board.Edge;

public class TimeoutException extends Exception {

	/** generated serialiseable version uid */
	private static final long serialVersionUID = -5179983027717240244L;
	
	public Edge edge;
	
	public TimeoutException(Edge edge){
		super("Time is up!");
		this.edge = edge;
	}
	
	public TimeoutException(){
		this(null);
	}
}
