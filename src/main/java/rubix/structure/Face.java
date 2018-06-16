package rubix.structure;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Collection;

class Face {

	public static final int N_SIDES = 4;

	static class Block implements Cube.Block {
		private Face parent;
		private int hash;
		private Map<Subaxis, Cube.Block> adjBlocks = new HashMap<>();
		
		@Override
		public int hashCode() { return hash; }
		
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
	private int order;
	private Map<Direction, Face.Block> corners = new HashMap<>();
	private Map<Direction, Edge> perimeter = new HashMap<>();
	private Map<Direction, Subaxis> definition = Direction.getDefaultDef();
	
	public int getOrder() { return order; }
	
	public Direction getNormal()
		{ return normal; }
		
	public Map<Direction, Subaxis> getDef()
		{ return new HashMap<>(definition); }
	
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
	
	private static int BLOCKS_CREATED = 0;
	public static Face.Block newBlock(Face face) {
		Face.Block block = new Face.Block();
		block.parent = face;
		block.hash = Face.BLOCKS_CREATED++;
		return block;
	}
	
	void allocate() {
		
		Face.Block topLeft    = null,    topRight = null,
		           bottomLeft = null, bottomRight = null;
		
		Direction[][] dirs = Face.getPlaneDirs(normal);
		
		// CP = chain pointer, CH = chain head
		Block currCP = null, prevCP, currCH = null, prevCH = null, flank; 
		
		for(int i = 0; i < order; i++) {
			prevCP = prevCH;
			flank = null;
			
			for(int j = 0; j < order; j++) {
				currCP = Face.newBlock(this);
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

	/* put an edge 'e' which is perpendicularly adjacent
	   to this face in the direction 'perpD'. More clearly
	   'perpD' is the direction perpendicular to the 
	   axial direction of the edge and outwards relative 
	   to the face */
	void putEdge(Edge e, Direction perpD) {
		if(e.getSize() != order) {
			throw new RuntimeException(
			"Given edge does not fit at the side of this face.");
		}
		
		e.setNormal(normal); // align the edge
		Direction d = e.getAxialDirection(normal);
		
		Cube.Block faceBlock = getCorner(d);
		
		for(Cube.Block block = e.getStart().getBlock(d);
			block != e.getEnd(); block = block.getBlock(d),
			faceBlock.getBlock(d)) {
			faceBlock.putBlock(perpD, block);
			block.putBlock(Direction
				.getReverse(perpD), faceBlock);
		} 
		
		perimeter.put(perpD, e);
		
		// put the direction of the
		// plane relative to the edge
		e.putFace(this, Direction
			.getReverse(perpD));
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
			adjacentFace.putEdge(edges[i], normal);
			edges[i].getStart().putEdge(inwardNorm, normals[next]);
		}
		
		Face.Block[] corners_ = new Face.Block[N_SIDES];
		for(int i = 0; i < N_SIDES; i++)
			corners_[(i + nTurns) % axes.length] = corners.get(directions[i]);
		for(int i = 0; i < N_SIDES; i++)
			corners.put(directions[i], corners_[i]);
	}
	
	static class Builder {
		private Face face;
		private int order;
		private Direction normal;
		
		Builder() { reset(); } 
		
		Builder reset() {
			face = null; 
			// default parameters
			order = 3;
			normal = Direction.TOP;
			return this; 
		}
		
		Builder setOrder(int order) 
			{ this.order = order; return this; }
		
		Builder setNormal(Direction normal) 
			{ this.normal = normal; return this; }
		
		Face build() {
			face = new Face();
			face.normal = normal;
			face.order = order;
			face.allocate();
			Face face_ = face;
			reset(); return face_;
		}
	}
}
