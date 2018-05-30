package rubix.structure;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

class Face {

	public static final int N_SIDES = 4;

	static class Block implements Cube.Block {
		private Face parent;
		private Map<Subaxis, Cube.Block> adjBlocks = new HashMap<>();
		
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

	private Direction normal;
	private Face.Block topLeft, topRight, bottomLeft, bottomRight;
	private Map<Direction, Edge> perimeter;
	private Map<Direction, Subaxis> definition = Direction.getDefaultDef();
	
	/* returns the corner of the face from which 
	   an edge along this direction would start */
	private Face.Block getCorner(Direction direction) {
		Direction[][] dirs = Face.getPlaneDirs(normal);
		
		if(dirs[X][POS] == direction)
			return topLeft;
		else if(dirs[Y][NEG] == direction)
			return topRight;
		else if(dirs[X][NEG] == direction)
			return bottomRight;
		else if(dirs[Y][POS] == direction)
			return bottomLeft;
		else throw new RuntimeException("Given direction " +
			direction + "is not along the perimeter of the face.");
	}
	
	static final int X = 0, Y = 1, POS = 0, NEG = 1;
	static Direction[][] getPlaneDirs(Direction normal) {
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
				currCP.joinParent(this);
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
			
			if(prevCH == null) {
				topLeft  = currCH;
				topRight = currCP;
			}
			
			prevCH = currCH;
		}
		
		bottomLeft  = currCH;
		bottomRight = currCP;
	}
	
	Direction putEdge(Edge e) {
		Direction d = e.getDirection(normal);
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
		
		Edge[] edges    = new Edge[N_SIDES]; // edges of this face
		Face[] adjFaces = new Face[N_SIDES]; // faces adjacent to this face
		Edge[] normals  = new Edge[N_SIDES]; // edges normal to this face
		
		Direction inwardNorm = Direction.getReverse(normal);
		
		for(int i = 0; i < directions.length; i++) { 
			edges[i] = perimeter.get(directions[i]);
			edges[i].setNormal(normal);
			adjFaces[i] = edges[i].getFace(inwardNorm);
			normals[i] = edges[i].start.getEdge(inwardNorm);
		}
		
		for(int i = 0; i < N_SIDES; i++) {
			int next = (i + nTurns) % N_SIDES;
			Face adjacentFace = adjFaces[next];
			edges[i].rotate(normal, nTurns);
			edges[i].putFace(adjacentFace);
			edges[i].putFace(this); 
			edges[i].start.putEdge(inwardNorm, normals[next]);
		}
	}
}
