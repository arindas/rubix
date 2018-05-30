package rubix.structure;

import java.util.Map;
import java.util.HashMap;

import static rubix.structure.Face.*;

class Edge {

	static class Block implements Cube.Block {
		private Map<Direction, Subaxis> definition = Direction.getDefaultDef();
		private Map<Subaxis, Cube.Block> adjBlocks = new HashMap<>();

		public Color getColor(Direction d)
		{ return Color.getColor(definition.get(d)); }

		public Cube.Block getBlock(Direction d)
		{ return adjBlocks.get(definition.get(d)); }

		public void putBlock(Direction d, Cube.Block b)
		{ adjBlocks.put(definition.get(d), b); }

		void rotate(Direction normal, int nTurns) {
			Direction[] directions = Direction.getPlane(normal);
			Subaxis[] axes = new Subaxis[directions.length];
			// normalize nTurns
			while(nTurns < 0) {nTurns += axes.length;}
			nTurns %= axes.length;

			for(int i = 0; i < axes.length; i++)
				axes[(i + nTurns) % axes.length] = definition.get(directions[i]);

			for(int i = 0; i < axes.length; i++)
				definition.put(directions[i], axes[i]);
		}
	}

	Corner start, end;
	Map<Direction, Face> faces;
	
	Direction direction;

	Map<Direction, Direction> normToDir;
	
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
		
		// normalize nTurns
		while(nTurns < 0) nTurns += dirs.length;
		nTurns %= dirs.length;

		// set plane directions
		int i = 0; for(; i < dirs.length; i++) {
			if(direction == dirs[i]) break;
		} direction = dirs[(i + nTurns) % dirs.length];

		Edge.Block block = start;
		do {
			Edge.Block next = (Edge.Block) block
				.getBlock(oldDirection);
			block.rotate(normal, nTurns);
			block = next;
		} while(block != end);
		
		normToDir.put(plane[X][NEG], direction);
		normToDir.put(plane[Y][POS], Direction.getReverse(direction));
	}

	void putFace(Face face) {
		Direction faceDirection = face.putEdge(this);
		faces.put(faceDirection, face);
	}
	
	Face getFace(Direction d) { return faces.get(d); }
}
