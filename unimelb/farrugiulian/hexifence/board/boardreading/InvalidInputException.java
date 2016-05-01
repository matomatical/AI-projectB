package unimelb.farrugiulian.hexifence.board.boardreading;

public class InvalidInputException extends Exception {
	
	/** generated serial version uid */
	private static final long serialVersionUID = 226857444666195940L;

	public InvalidInputException(String message){
		super("Invalid input: " + message);
	}
}
