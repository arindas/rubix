package rubix.structure;

import java.util.Map;

class Edge {
	static class Block implements Cube.Block {
		private Map<Direction, Subaxis> definition;
		private Map<Subaxis, Cube.Block> adjBlocks;
		
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
		Direction oldDirection = direction;
		
		// get plane directions
		Direction[] dirs = Direction.getPlane(normal);
		
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
	}
	
	void putFace(Face face) {
		Direction faceDirection = face.putEdge(this);
		faces.put(faceDirection, face);
	}
}
