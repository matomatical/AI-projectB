package unimelb.farrugiulian.hexifence.agent;

import java.util.Scanner;

import unimelb.farrugiulian.hexifence.board.Edge;

public class AgentMe extends AgentBasic{

	private Scanner sc = new Scanner(System.in);
	
	protected Edge getChoice(){

		super.board.printIndexedTo(System.out);
		System.out.print("where would you like to move? ('row col')");
		
		int i = sc.nextInt();
		int j = sc.nextInt();
		
		return board.getEdge(i, j);
	}
}