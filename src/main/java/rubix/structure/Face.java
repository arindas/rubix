package rubix.structure;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

class Face {

	static class Block implements Cube.Block {
		private Face parent;
		private Map<Subaxis, Cube.Block> adjBlocks;
		
		public Color getColor(Direction d)
		{ return Color.getColor(parent.definition.get(d)); }
		
		public Cube.Block getBlock(Direction d)
		{ return adjBlocks.get(parent.definition.get(d)); }
		
		public void putBlock(Direction d, Cube.Block b)
		{ adjBlocks.put(parent.definition.get(d), b); }
		
		void joinParent(Face face) {
			Map<Subaxis, Subaxis> oldToNew = new HashMap<>();
			for(Direction d : Direction.DIRECTIONS) {
				oldToNew.put(parent.definition.get(d),
					face.definition.get(d));
			}
			
			Map<Subaxis, Cube.Block> adjBlocks_ = new HashMap<>();
			
			for(Subaxis axis : Subaxis.SUBAXES) {
				// using the new axis mapped from the old axis
				// map the value of the old axis to the new axis
				adjBlocks_.put(oldToNew.get(axis), adjBlocks.get(axis));
			}
			
			adjBlocks = adjBlocks_; 
			this.parent = face;
		}
	} 

	Direction normal;
	Face.Block topLeft, bottomRight;
	Map<Direction, Edge> perimeter;
	Map<Direction, Subaxis> definition;
	
	Face.Block getCorner(Direction direction) {
		if(topLeft.getBlock(direction) != null)
			return topLeft;
		else if(bottomRight.getBlock(direction) != null)
			return bottomRight;
		throw new RuntimeException("Given direction "+direction+
		"is not along the perimeter of the face.");
	}
	
	private static final int X = 0, Y = 1, POS = 0, NEG = 1;
	private static Direction[][] getPlaneDirs(Direction normal) {
		Direction[] dirs = Direction.getPlane(normal);
		
		return new Direction[][]{
			{dirs[1], dirs[3]}, // X+, X-
			{dirs[0], dirs[2]}  // Y+, Y-
		};
	}
	
	void allocate(int order) {
		
		Direction[][] dirs = Face.getPlaneDirs(normal);
		
		// CP = chain pointer, CH = chain head
		Block currCP = null, prevCP, currCH = null, prevCH = null, flank; 
		
		for(int i = 0; i < order; i++) {
			prevCP = prevCH;
			flank = null;
			
			for(int j = 0; j < order; j++) {
				currCP = new Face.Block();
				currCP.putBlock(dirs[Y][POS], prevCP);
				currCP.putBlock(dirs[X][NEG], flank);
				if(flank != null) { 
					flank.putBlock(dirs[X][POS], currCP); 
				} else { currCH = currCP; }
				
				if(prevCP != null) {
					prevCP.putBlock(dirs[Y][NEG], currCP);
					prevCP = (Face.Block) prevCP.getBlock(dirs[X][POS]);
				}
				
				flank = currCP;
			}
			
			if(prevCH == null)
				topLeft = currCH;
			
			prevCH = currCH;
		}
		
		bottomRight = currCP;
	}
	
	Direction putEdge(Edge e) {
		Direction d = e.direction;
		Cube.Block faceBlock = getCorner(d);
		Direction[][] dirs = Face.getPlaneDirs(d);
		
		for(Cube.Block block = e.start.getBlock(d);
			block != e.end; block = block.getBlock(d),
			faceBlock.getBlock(d)) {
			faceBlock.putBlock(dirs[Y][POS], block);
			block.putBlock(dirs[Y][NEG], faceBlock);
		}
		
		perimeter.put(dirs[Y][POS], e);
		
		// return the direction of the
		// plane relative to the edge
		return dirs[Y][NEG];
	}
	
	void rotate(int nTurns) {
		Direction[] directions = Direction.getPlane(normal);
		Subaxis[] axes = new Subaxis[directions.length];
		// normalize nTurns
		while(nTurns < 0) {nTurns += axes.length;}
		nTurns %= axes.length;
		
		for(int i = 0; i < axes.length; i++)
			axes[(i + nTurns) % axes.length] = definition.get(directions[i]);
			
		for(int i = 0; i < axes.length; i++)
			definition.put(directions[i], axes[i]);

		Collection<Edge> edges = perimeter.values();
		for(Edge edge : edges) {
			Face adjacentFace = edge.faces
				.get(Direction.getReverse(normal));
			edge.rotate(normal, nTurns);
			edge.putFace(adjacentFace);
			edge.putFace(this);
		}
	}
}
