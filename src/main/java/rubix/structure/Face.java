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
		
		private void joinParent(Face face) {
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
	private Map<Direction, Face.Block> corners;
	private Map<Direction, Edge> perimeter = new HashMap<>();
	private Map<Direction, Subaxis> definition = Direction.getDefaultDef();
	
	/* returns the corner of the face from which 
	   an edge along this direction would start */
	Face.Block getCorner(Direction direction) 
	{ return corners.get(direction); }
	
	static final int X = 0, Y = 1, POS = 0, NEG = 1;
	static Direction[][] getPlaneDirs(Direction normal) {
		Direction[] dirs = Direction.getPlane(normal);
		
		return new Direction[][]{
			{dirs[1], dirs[3]}, // X+, X-
			{dirs[0], dirs[2]}  // Y+, Y-
		};
	}
	
	void allocate(int order) {
		
		Face.Block topLeft = null,       topRight = null,
		           bottomLeft = null, bottomRight = null;
		
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
		
		// assign corners to the directions at 
		// the beginning of which they are placed 
		corners.put(dirs[X][POS],     topLeft);
		corners.put(dirs[Y][NEG],    topRight);
		corners.put(dirs[X][NEG], bottomRight);
		corners.put(dirs[Y][POS],  bottomLeft);
	}
	
	Direction putEdge(Edge e) {
		Direction d = e.getAxialDirection(normal);
		
		if(d == null) {
			throw new RuntimeException(
			"Given edge is not coplanar to this face.");
		}
		
		Cube.Block faceBlock = getCorner(d);
		Direction[][] dirs = Face.getPlaneDirs(d);
		
		for(Cube.Block block = e.getStart().getBlock(d);
			block != e.getEnd(); block = block.getBlock(d),
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
			normals[i] = edges[i].getStart().getEdge(inwardNorm);
		}
		
		for(int i = 0; i < N_SIDES; i++) {
			int next = (i + nTurns) % N_SIDES;
			Face adjacentFace = adjFaces[next];
			edges[i].rotate(normal, nTurns);
			edges[i].putFace(adjacentFace);
			edges[i].putFace(this); 
			edges[i].getStart().putEdge(inwardNorm, normals[next]);
		}
		
		Face.Block[] corners_ = new Face.Block[N_SIDES];
		for(int i = 0; i < N_SIDES; i++)
			corners_[(i + nTurns) % axes.length] = corners.get(directions[i]);
		for(int i = 0; i < N_SIDES; i++)
			corners.put(directions[i], corners_[i]);
	}
}
