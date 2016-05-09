package unimelb.farrugiulian.hexifence.board.features;

import java.util.ArrayList;

import unimelb.farrugiulian.hexifence.board.Cell;

public class Intersection {

	FeatureSet fs;
	Cell cell;
	
	ArrayList<Chain> chains = new ArrayList<Chain>(3);
	ArrayList<Loop>  loops  = new ArrayList<Loop>(1);
	public Intersection(Cell cell, FeatureSet fs){
		this.fs = fs;
		this.cell = cell;
	}

	public void addChain(Chain chain) {
		this.chains.add(chain);
	}
	
	public void addLoop(Loop loop) {
		this.loops.add(loop);
	}
	
	
}
