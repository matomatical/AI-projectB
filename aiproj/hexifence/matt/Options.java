package aiproj.hexifence.matt;

class Options {
	int dimension;
	String playerOne;
	String playerTwo;
	
	/** get options from command line arguments */
	public Options(String[] args){
		try{
			this.dimension = Integer.parseInt(args[0]);
			this.playerOne = args[1];
			this.playerTwo = args[2];
		} catch (Exception e){
			this.printUsageExit();
		}
	}

	/** print a usage statement and exit the program */
	private void printUsageExit() {
		
		// get the calling program name
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
	    String mainClass = stack[stack.length - 1].getClassName();
		
	    // print the usage message
		System.out.print("usage: ");
		System.out.println("java " + mainClass + " "
				+ "dimension player_one player_two");
		
		System.out.println("  dimension:  "
				+ "integer board dimension (radius)");
		
		System.out.println("  player_one: "
				+ "qualified class name of first player");
		
		System.out.println("  player_two: "
				+ "qualified class name of second player");
		
		// and exit! as promised
		System.exit(1);
	}
}
