package rubix.structure;

import java.util.Map;
import java.util.HashMap;

class Corner implements Cube.Block {
	private Map<Direction, Subaxis> definition = Direction.getDefaultDef();
	private Map<Subaxis, Cube.Block> adjBlocks = new HashMap<>();
	private int hash; 
	private Map<Direction, Edge> edges = new HashMap<>();
	
	private static int BLOCKS_CREATED = 0;
	
	public static Corner New() {
		Corner corner = new Corner();
		corner.hash = Corner.BLOCKS_CREATED++;
		return corner;
	}

	@Override public int hashCode() { return hash; }

	void putEdge(Direction dir, Edge e) { edges.put(dir, e); }
	
	Edge getEdge(Direction dir) { return edges.get(dir); }
	
	public Color getColor(Direction d)
	{ return Color.getColor(definition.get(d)); }

	public Cube.Block getBlock(Direction d)
	{ return adjBlocks.get(definition.get(d)); }

	public void putBlock(Direction d, Cube.Block b)
	{ adjBlocks.put(definition.get(d), b); }

	void rotate(Direction normal, int nTurns) {
		// get plane directions
		Direction[] dirs = Direction.getPlane(normal);
		Subaxis[] axes = new Subaxis[dirs.length];
		
		// normalize nTurns
		while(nTurns < 0) nTurns += dirs.length;
		nTurns %= dirs.length;
		
		// rotate definition mapping
		for(int i = 0; i < axes.length; i++)
			axes[(i + nTurns) % axes.length] = definition.get(dirs[i]);

		for(int i = 0; i < axes.length; i++)
			definition.put(dirs[i], axes[i]);
			
		//rotate connected edges
		Map<Direction, Edge> edges_ = new HashMap<>();
		for(int i = 0; i < dirs.length; i++) {
			Edge edge = edges.get(dirs[i]);
			if(edge != null) { edges_.put(dirs[(i + nTurns) % dirs.length], edge); }
		} edges = edges_;
	}
}
