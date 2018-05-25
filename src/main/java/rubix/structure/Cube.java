package rubix.structure;

import java.util.Map;

public class Cube {

	interface Block {
		void putBlock(Direction d, Block b);
		
		Block getBlock(Direction d);
		
		Color getColor(Direction d);
	}
	
	private Map<Direction, Face> faces;
	private Map<Direction, Edge[]> edges;
}