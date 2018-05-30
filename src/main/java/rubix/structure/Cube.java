package rubix.structure;

import java.util.Map;
import java.util.HashMap;

public class Cube {

	public static final int N_FACES   = 6;
	public static final int N_EDGES   = 12;
	public static final int N_CORNERS = 8;
	
	static interface Block {
		void putBlock(Direction d, Block b);
		
		Block getBlock(Direction d);
		
		Color getColor(Direction d);
	}
	
	private Map<Direction, Face> faces;
	private Map<Direction, Edge[]> edges;
	
}