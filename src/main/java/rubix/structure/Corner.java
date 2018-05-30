package rubix.structure;

import java.util.Map;

class Corner extends Edge.Block {
	Map<Direction, Edge> edges;
	
	void putEdge(Direction dir, Edge e) { edges.put(dir, e); }
	
	Edge getEdge(Direction dir) { edges.get(dir); }
}