package aiproj.hexifence;
/*
 *   Piece:
 *      Define types of states that can appear on a board
 *      
 *   @author lrashidi
 *   
 */

public interface Piece {
    public static final int BLUE = 1, 
                            RED = 2,
                            DEAD = 3,
                            EMPTY = 0,
    		                INVALID = -1;
}
