package rubix.structure;

import java.util.Map;
import java.util.HashMap;

import static rubix.structure.Face.*;

class Edge {

	static class Block implements Cube.Block {
		private Edge parent;
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
	}

	Corner start, end;
	Map<Direction, Face> faces;
	
	Direction direction;

	private Map<Direction, Direction> normToDir;
	private Map<Direction, Subaxis> definition = Direction.getDefaultDef();
	
	Direction getDirection(Direction normal) {
		Direction d = normToDir.get(normal);
		if(d == null) {
			throw new RuntimeException(
			"Normal not in the visible sides of this edge"); 
		}
		return d;
	}
	
	void setNormal(Direction normal) {
		direction = getDirection(normal);
		
		if(start.getBlock(direction) == null) {
			// change of orientation: swap ends
			Corner temp = start;
			start = end;
			end = temp;
		}
	}

	void setEnds(Corner start, Corner end)
	{ this.start = start; this.end = end; }

	void allocate(int nBlocks) {
		Direction reverse = Direction.getReverse(direction);

		Cube.Block prev = start, curr = null; // curr moves ahead of q
		for(int i = 0; i < nBlocks; i++) {
			curr = new Edge.Block();
			prev.putBlock(direction, curr);
			curr.putBlock(reverse, curr);
			prev = curr;
		}
		curr.putBlock(direction, end);
		end.putBlock(reverse, curr);
	}

	void rotate(Direction normal, int nTurns) {
		setNormal(normal);
		
		Direction oldDirection = direction;

		// get plane directions
		Direction[] dirs = Direction.getPlane(normal);
		Direction[][] plane = Face.getPlaneDirs(direction);
		Subaxis[] axes = new Subaxis[dirs.length];
		// normalize nTurns
		while(nTurns < 0) nTurns += dirs.length;
		nTurns %= dirs.length;

		// set plane directions
		int i = 0; for(; i < dirs.length; i++) {
			if(direction == dirs[i]) break;
		} direction = dirs[(i + nTurns) % dirs.length];
		
		for(i = 0; i < axes.length; i++)
			axes[(i + nTurns) % axes.length] = definition.get(dirs[i]);

		for(i = 0; i < axes.length; i++)
			definition.put(dirs[i], axes[i]);
		
		normToDir.put(plane[X][NEG], direction);
		normToDir.put(plane[Y][POS], Direction.getReverse(direction));
	}

	void putFace(Face face) {
		Direction faceDirection = face.putEdge(this);
		faces.put(faceDirection, face);
	}
	
	Face getFace(Direction d) { return faces.get(d); }
}
