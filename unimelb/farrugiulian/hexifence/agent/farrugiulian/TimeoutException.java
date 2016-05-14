package unimelb.farrugiulian.hexifence.agent.farrugiulian;

import unimelb.farrugiulian.hexifence.board.Edge;

public class TimeoutException extends Exception {

	/** generated serialiseable version uid */
	private static final long serialVersionUID = -5179983027717240244L;
	
	public TimeoutException(){
		super("Time is up!");
	}
}
