package unimelb.farrugiulian.hexifence.agent;

import java.util.Random;
import java.util.Stack;

import unimelb.farrugiulian.hexifence.board.Cell;
import unimelb.farrugiulian.hexifence.board.Edge;

public class AgentGreedy extends AgentBasic{

	private Random rng;

	public AgentGreedy(){
		long seed = System.nanoTime();
		rng = new Random(seed);
		System.err.println("AgentGreedy seed: "+seed);
	}
	
	@Override
	public Edge getChoice(){
		
		Edge[] edges = board.getFreeEdges();
		
		// first select moves that will capture a cell
		
		// (or two, doesn't matter if we take the ones first, we will get another turn)
		// hmm there's room for optimisation and planning ahead here, following a pre-thought path
		// in subsequent turns
		// also responding to opponent moves not just validating, gives place to search for changes (could save time)
		
		// i guess keep state: a list of currently available cells for capture
		// would have to keep it up to date in light of own moves and opponent moves
		// (removing caputred cells and opening cells for capture)
		
		for(Edge edge : edges){
			if(edge.numCapturableCells()>0){
				return edge;
			}
		}
		
		// then select moves that wont sacrifice a cell
		
		int offset = rng.nextInt(edges.length);
		
		for(int i = 0; i < edges.length; i++){
			Edge edge = edges[(i + offset) % edges.length];
			
			boolean safe = true;
			Cell[] cells = edge.getCells();
			for(Cell cell : cells){
				if(cell.numFreeEdges() == 2){
					// this cell is not safe to capture around
					safe = false;
				}
			}
			if(safe){
				return edge;
			}
		}
		
		// all remaining edges represent possible sacrifices,
		// just find the best option (least damage)
		
		Edge bestEdge = edges[0];
		int bestCost = sacrificeSize(edges[0]);
		
		for(int i = 1; i < edges.length; i++){
			Edge edge = edges[i];
			int cost = sacrificeSize(edge);
			if(cost < bestCost){
				bestEdge = edge;
				bestCost = cost;
			}
		}
		
		return bestEdge;
	}

	private int sacrificeSize(Edge edge) {
		int size = 0;
		Stack<Edge> stack = new Stack<Edge>();
		
		board.place(edge, super.opponent);
		stack.push(edge);
		
		for(Cell cell : edge.getCells()){
			size += sacrifice(cell, stack);
		}
		
		while(!stack.isEmpty()){
			board.unplace(stack.pop());
		}
		
		return size;
	}

	private int sacrifice(Cell cell, Stack<Edge> stack){
		
		int n = cell.numFreeEdges();
		
		if(n > 1){
			// this cell is not available for capture
			return 0;
			
		} else if (n == 1){
			for(Edge edge : cell.getEdges()){
				if(edge.isEmpty()){
					
					// claim this piece
					edge.place(super.opponent);
					stack.push(edge);
					
					// follow opposite cell
					Cell other = edge.getOtherCell(cell);
					
					if(other == null){
						return 1;
					}
					
					return 1 + sacrifice(other, stack);
				}	
			}
		}
		
		// n == 0, which means this cell is a dead end for the taking
		return 1;
	}
}