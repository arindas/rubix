package rubix.structure;

import org.junit.Test;
import static org.junit.Assert.*;

public class EdgeTest {
	
	private static Edge buildEdge(int nBlocks) {
		Edge.Builder builder = new Edge.Builder();
		return builder
			.setEnds(Corner.New(), Corner.New())
			.setAxialDirection(Direction.FRONT, Direction.RIGHT)
			.setAxialDirection(Direction.TOP, Direction.LEFT)
			.setLength(nBlocks)
			.build();
	}
	
	@Test 
	public void testAllocation() {
		buildEdge(5);
	}

	@Test 
	public void testComposition() {
		Direction normal = Direction.FRONT;
		
		Edge edge = buildEdge(5);
		edge.setNormal(normal);
		
		Cube.Block b = edge.getStart(); do {
			System.out.println(b);
			
			assertEquals(b.getColor(Direction.FRONT),
				Color.getColor(Subaxis.Y_1));
			b = b.getBlock(edge
				.getAxialDirection(Direction.FRONT));
				
			
		} while(b != null);
	}
	
	@Test 
	public void testRotation() {
		Edge edge = buildEdge(6);
		Direction normal = Direction.TOP;
		int nTurns = 1;
		edge.setNormal(normal);
		System.out.println("Previous axial direction: "+
			edge.getAxialDirection(normal));
		edge.rotate(normal, nTurns);
		System.out.println("After "+nTurns+
			" turns\nCurrent axial direction: "+
			edge.getAxialDirection(normal));
		
		assertEquals(Direction.REAR, edge.getAxialDirection(normal));
		
		Cube.Block b = edge.getStart(); do {
			System.out.println(b);
			assertEquals(b.getColor(normal),
				Color.getColor(Subaxis.Z_1));
			b = b.getBlock(edge
				.getAxialDirection(normal));
		} while(b != null);
	}
}
