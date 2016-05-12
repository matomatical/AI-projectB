package unimelb.farrugiulian.hexifence.board.features;

import java.util.HashMap;

import unimelb.farrugiulian.hexifence.board.Cell;

public class Chain extends RichFeature{

	private boolean isOpen = false;
	
	public Chain(Cell[] cells, FeatureSet fs) {
		super(cells, fs);
	}

	public Chain(Chain old, HashMap<Cell, Intersection> map){
		super(old);
		
		for(Intersection oldInt : old.ints){

			Intersection newInt = map.getOrDefault(oldInt.cell, new Intersection(oldInt.cell, old.fs));
			
			newInt.addChain(this);
			this.addIntersection(newInt);
			
			map.put(newInt.cell, newInt);
		}
	}
	
	// at the moment, I just dont think a chain can know if it's open or not
	
	// is it worth having an 'open chain' class? opening loops and stuff will
	// give us open chains anyway so classes will have to be swapped ANYWAY
}
