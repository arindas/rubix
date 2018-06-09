package rubix.structure;

import java.util.Map;
import java.util.HashMap;

import static rubix.structure.Face.*;

public class Edge {

	static class Block implements Cube.Block {
		private Edge parent; private int hash;
		private Map<Subaxis, Cube.Block> adjBlocks = new HashMap<>();
		
		public Color getColor(Direction d)
		{ return Color.getColor(parent.definition.get(d)); }

		public Cube.Block getBlock(Direction d)
		{ return adjBlocks.get(parent.definition.get(d)); }

		public void putBlock(Direction d, Cube.Block b)
		{ adjBlocks.put(parent.definition.get(d), b); }
		
		void joinParent(Edge edge) {
			Map<Subaxis, Subaxis> oldToNew = new HashMap<>();
			for(Direction d : Direction.DIRECTIONS) {
				oldToNew.put(edge.definition.get(d),
					edge.definition.get(d));
			}
			
			Map<Subaxis, Cube.Block> adjBlocks_ = new HashMap<>();
			
			for(Subaxis axis : Subaxis.SUBAXES) {
				// using the new axis mapped from the old axis
				// map the value of the old axis to the new axis
				adjBlocks_.put(oldToNew.get(axis), adjBlocks.get(axis));
			}
			
			adjBlocks = adjBlocks_; 
			this.parent = edge;
		}
		
		@Override public int hashCode() { return hash; }
	}

	private static int BLOCKS_CREATED = 0;
	private Corner start, end;
	private Map<Direction, Face> faces = new HashMap<>();
	
	private Direction axialDirection;

	private Map<Direction, Direction> normToDir = new HashMap<>();
	private Map<Direction, Subaxis> definition = Direction.getDefaultDef();
	
	public Corner getStart() { return start; }
	public Corner   getEnd() { return   end; }
	
	public static Edge.Block newBlock(Edge edge) {
		Edge.Block block = new Edge.Block();
		block.parent = edge;
		block.hash = Edge.BLOCKS_CREATED++;
		return block;
	}
	
	Direction getAxialDirection(Direction normal) {
		Direction d = normToDir.get(normal);
		if(d == null) {
			throw new RuntimeException(
			"Normal not in the visible sides of this edge.");
		}
		return d;
	}
	
	void setNormal(Direction normal) {
		axialDirection = getAxialDirection(normal);
		
		if(start.getBlock(axialDirection) == null) {
			// change of orientation: swap ends
			Corner temp = start;
			start = end; end = temp;
		}
	}
	
	void allocate(int nBlocks) {
		Direction reverse = Direction.getReverse(axialDirection);
		
		Cube.Block prev = start, curr = null; 
		
		for(int i = 0; i < nBlocks; i++) {
			curr = Edge.newBlock(this);
			prev.putBlock(axialDirection, curr);
			curr.putBlock(reverse, prev);
			prev = curr;
		}
		
		curr.putBlock(axialDirection, end);
		end.putBlock(reverse, curr);
	}

	void rotate(Direction normal, int nTurns) {
		setNormal(normal);
		
		// get plane directions
		Direction[] dirs = Direction.getPlane(normal);
		Subaxis[] axes = new Subaxis[dirs.length];
		// normalize nTurns
		while(nTurns < 0) nTurns += dirs.length;
		nTurns %= dirs.length;

		// set plane directions
		int i = 0; for(; i < dirs.length; i++) {
			if(axialDirection == dirs[i]) break;
		} axialDirection = dirs[(i + nTurns) % dirs.length];
		
		for(i = 0; i < axes.length; i++)
			axes[(i + nTurns) % dirs.length] = definition.get(dirs[i]);

		for(i = 0; i < axes.length; i++)
			definition.put(dirs[i], axes[i]);
		
		// rotate the corners
		start.rotate(normal, nTurns);
		end.rotate(normal, nTurns);
		
		Direction otherNormal = dirs[(i + nTurns - 1) % dirs.length];
		Direction reverse = Direction.getReverse(axialDirection);
		normToDir.put(normal, axialDirection);
		normToDir.put(otherNormal, reverse);
	}

	void putFace(Face face) {
		Direction faceDirection = face.putEdge(this);
		faces.put(faceDirection, face);
	}
	
	Face getFace(Direction d) { return faces.get(d); }
	
	static class Builder {
		private Edge edge;
		private int nBlocks;
		
		Builder() { reset(); }
		
		Builder reset() 
		{ edge = new Edge(); nBlocks = 0; return this; }
		
		Builder setEnds(Corner start, Corner end)
		{ edge.start = start; edge.end = end; return this; }
		
		Builder setAxialDirection(Direction normal, Direction d) { 
			edge.normToDir.put(normal, d);
			edge.setNormal(normal);
			// link corners to this edge
			edge.start.putEdge(edge.axialDirection, edge);
			edge.end.putEdge(Direction
				.getReverse(edge.axialDirection), edge); 
			return this;
		}
		
		Builder setLength(int nBlocks)
		{ this.nBlocks = nBlocks; return this; }
		
		Edge build() { 
			edge.allocate(nBlocks);
			Edge edge_ = edge; 
			reset(); return edge_; 
		}
	}
}
