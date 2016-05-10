package unimelb.farrugiulian.hexifence.board.features;

import java.util.HashMap;

import unimelb.farrugiulian.hexifence.board.Cell;

public class Loop extends RichFeature {

	public Loop(Cell[] cells, FeatureSet fs){
		super(cells, fs);
	}

	public Loop(Loop old, HashMap<Cell, Intersection> map){
		super(old);
		
		for(Intersection oldInt : old.ints){

			Intersection newInt = map.getOrDefault(oldInt.cell, new Intersection(oldInt.cell, old.fs));
			
			newInt.addLoop(this);
			this.addIntersection(newInt);
			
			map.put(newInt.cell, newInt);
		}
	}
}
